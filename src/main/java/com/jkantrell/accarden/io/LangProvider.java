package com.jkantrell.accarden.io;

import com.jkantrell.yamlizer.yaml.YamlElementType;
import com.jkantrell.yamlizer.yaml.YamlMap;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.spec.ECField;
import java.util.HashMap;
import java.util.Map;

public class LangProvider {
    //FIELDS
    private String defaultLang_ = null;
    private final String langsPath_;
    private final JavaPlugin plugin_;
    private final HashMap<String, YamlMap> langs_ = new HashMap<>();

    //CONSTRUCTORS
    public LangProvider(JavaPlugin plugin, String langsPath) {
        this.langsPath_ = langsPath;
        this.plugin_ = plugin;

        File langFolder = new File("./plugins/" + langsPath);
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

        //Checking if there's an already saved lang file
        URI uri;
        try {
            uri = new URI(this.plugin_.getDataFolder().getPath() + "/" + locale);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
        File file = new File(uri);
        if (file.exists()) {
            YamlMap lang = new YamlMap(new FileInputStream(file));
            this.addLanguage(locale, lang);
            return lang;
        }

        //Checking if there's such resource in the JAR
        String langFilePath = langsPath_ + "/" + locale;
        InputStream langFIle = this.plugin_.getResource(langFilePath);
        if (langFIle == null) { return null; }

        this.plugin_.saveResource(langFilePath, true);
        file = new File(uri);
        YamlMap lang = new YamlMap(new FileInputStream(file));
        this.addLanguage(locale, lang);
        return lang;
    }
}

