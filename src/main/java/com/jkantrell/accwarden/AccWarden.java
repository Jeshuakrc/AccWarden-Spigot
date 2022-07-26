package com.jkantrell.accwarden;

import com.jkantrell.accwarden.accoint.Account;
import com.jkantrell.accwarden.accoint.AccountRepository;
import com.jkantrell.accwarden.io.database.DataBase;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public final class AccWarden extends JavaPlugin {

    //STATIC FIELDS
    private static final String PLUGIN_PATH = "./plugins/Arccarden/";

    //FIELDS
    private final DataBase dataBase_ = new DataBase();
    private AccountRepository accountRepository_ = null;

    //PLUGIN LOGIC
    @Override
    public void onEnable() {
        this.dataBase_.setFilePath(AccWarden.PLUGIN_PATH + "database.db");
        this.dataBase_.setLogger(this.getLogger());
        this.dataBase_.setUp();
        try {
            this.dataBase_.executeSQL(new String(this.getResource("dbSetup.sql").readAllBytes(), StandardCharsets.UTF_8));
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        this.dataBase_.addEntity(Account.class, new Account.Parser(), "accounts");

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
}
