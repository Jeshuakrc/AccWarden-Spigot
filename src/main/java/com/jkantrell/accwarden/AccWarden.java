package com.jkantrell.accwarden;

import com.jkantrell.accwarden.accoint.Account;
import com.jkantrell.accwarden.accoint.AccountRepository;
import com.jkantrell.accwarden.io.Config;
import com.jkantrell.accwarden.io.database.DataBase;
import com.jkantrell.accwarden.session.SessionHolder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;

public final class AccWarden extends JavaPlugin {

    //STATIC FIELDS
    private static final String PLUGIN_PATH = "./plugins/Arccarden/";

    //FIELDS
    public final Config CONFIG = new Config("");
    private static AccWarden instance_ = null;
    private final DataBase dataBase_ = new DataBase();
    private AccountRepository accountRepository_ = null;
    private SessionHolder sessionHolder_ = null;
    private boolean bedrockOn_ = false;

    //PLUGIN LOGIC
    @Override
    public void onEnable() {
        //Main instance setup
        AccWarden.instance_ = this;

        //Configuration initialization
        this.CONFIG.setFilePath(this.getDataFolder().getPath() + "/config.yml");
        try {
            this.CONFIG.load();
        } catch (FileNotFoundException e) {
            this.saveResource("config.yml",true);
            this.onEnable();
            return;
        }

        //Database setup
        this.dataBase_.setFilePath(AccWarden.PLUGIN_PATH + "database.db");
            //this.dataBase_.setLogger(this.getLogger());
        this.dataBase_.setUp();
        try {
            this.dataBase_.executeSQL(new String(this.getResource("dbSetup.sql").readAllBytes(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Accounts and sessions setup
        this.dataBase_.addEntity(Account.class, new Account.Parser(), "accounts");
        this.accountRepository_ = new AccountRepository(this.dataBase_);
        this.sessionHolder_ = new SessionHolder(this);
        this.sessionHolder_.setHoldTime(this.CONFIG.sessionHoldTime);
        this.sessionHolder_.setCrossPlatformSessions(this.CONFIG.crossPlatformSessions);

        //Floodgate setup
        if (Bukkit.getServer().getPluginManager().getPlugin("floodgate") != null) {
            this.bedrockOn_ = true;
        }

        //Listener setup
        this.getServer().getPluginManager().registerEvents(new AccWardenListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    //GETTERS
    public DataBase getDataBase() {
        return this.dataBase_;
    }
    public AccountRepository getAccountRepository() {
        return this.accountRepository_;
    }
    public SessionHolder getSessionHolder() {
        return this.sessionHolder_;
    }
    public boolean isBedrockOn() {
        return this.bedrockOn_;
    }
    public static AccWarden getInstance() {
        return AccWarden.instance_;
    }
}
