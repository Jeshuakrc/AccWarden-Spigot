package com.jkantrell.accwarden.io;

import com.jkantrell.yamlizer.yaml.YamlElementType;
import com.jkantrell.yamlizer.yaml.YamlMap;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class LangProvider {
    //FIELDS
    private String defaultLang_ = null;
    private Level loggingLevel_ = Level.FINEST;
    private final String langsPath_;
    private final JavaPlugin plugin_;
    private final HashMap<String, YamlMap> langs_ = new HashMap<>();

    //CONSTRUCTORS
    public LangProvider(JavaPlugin plugin, String langsPath) {
        this.langsPath_ = langsPath;
        this.plugin_ = plugin;

        File langFolder = new File(this.plugin_.getDataFolder().getPath() + "/" + langsPath);
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        } else if (!langFolder.isDirectory()) {
            throw new NoSuchFieldError("The 'langPath' parameter must be a directory.");
        }
    }

    //SETTERS
    public void setDefaultLanguage(String key) {
        this.defaultLang_ = key;
    }
    public void setLoggingLevel(Level level) {
        this.loggingLevel_ = level;
    }

    //METHODS
    public String getEntry(Player player, String path, Object... params) {
        return this.getEntry(player.getLocale(),path, params);
    }
    public String getEntry(String locale, String path, Object... params) {
        return String.format(this.getNonFormattedEntry(locale,path), params);
    }
    public String getNonFormattedEntry(Player player, String path) {
        return this.getNonFormattedEntry(player.getLocale(), path);
    }
    public String getNonFormattedEntry(String locale, String path) {
        YamlMap lang = null;
        try {
            lang = this.pickLanguage_(locale);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (lang == null) { return null; }

        String r = lang.gerFromPath(path).get(YamlElementType.STRING);
        if (r == null) { r = lang.gerFromPath(this.defaultLang_).get(YamlElementType.STRING); }
        if (r == null) { r = ""; }

        return r;
    }
    public void addLanguage(String locale, InputStream inputStream) {
        this.addLanguage(locale,new YamlMap(inputStream));
    }
    public void addLanguage(String locale, YamlMap yamlMap) {
        if (this.langs_.isEmpty()) { this.defaultLang_ = locale; }
        this.langs_.put(locale,yamlMap);
    }

    //PRIVATE METHODS
    private YamlMap pickLanguage_(String locale) throws FileNotFoundException {

        //Attempting lo pick a perfect match
        YamlMap m = this.loadLanguage_(locale);
        if (m != null) { return m; }

        //Attempting lo pick a general language
        String k = StringUtils.split(locale,'_')[0];
        m = this.loadLanguage_(k);
        if (m != null) { return m; }

        //Attempting to pick any (already loaded) language with the same language code
        m = this.langs_.entrySet().stream()
                .filter(e -> e.getKey().startsWith(k))
                .findFirst().map(Map.Entry::getValue)
                .orElse(null);
        if (m != null) { return m; }

        //Picking the default language
        return this.langs_.get(this.defaultLang_);
    }

    private YamlMap loadLanguage_(String locale) throws FileNotFoundException {
        //Checking if already loaded
        if (this.langs_.containsKey(locale)) { return this.langs_.get(locale); }
        this.log_("Language file '" + locale + "' not loaded.");

        //Checking if there's an already saved lang file
        String filePath = this.plugin_.getDataFolder().getPath() + "/" + this.langsPath_ + "/" + locale + ".yml";
        this.log_("Looking for language file '" + locale + "' not found in plugin's date directory. (" + filePath + ") ");
        File file = new File(filePath);
        if (file.exists()) {
            this.log_("Found language file '" + locale + "' in plugin's data directory.");
            YamlMap lang = new YamlMap(new FileInputStream(file));
            this.addLanguage(locale, lang);
            return lang;
        }
        this.log_("Language file '" + locale + "' not found in plugin's date directory.");

        //Checking if there's such resource in the JAR
        String langFilePath = this.langsPath_ + "/" + locale + ".yml";
        this.log_("Looking for language file '" + locale + "' in internal resources (" + langFilePath + ").");
        InputStream langFIle = this.plugin_.getResource(langFilePath);
        if (langFIle == null) {
            this.log_("Language file '" + locale + "' not found in internal resources either. Non-existing language.");
            this.langs_.put(locale,null);
            return null;
        }
        this.log_("Found language file '" + locale + "' in internal resources. Saving it to plugin's data directory.");

        this.plugin_.saveResource(langFilePath, true);
        file = new File(filePath);
        YamlMap lang = new YamlMap(new FileInputStream(file));
        this.addLanguage(locale, lang);
        return lang;
    }

    private void log_(String message, Level level) {
        this.plugin_.getLogger().log(level, "[Language provider] " + message);
    }
    private void log_(String message) {
        this.log_(message,this.loggingLevel_);
    }
}

