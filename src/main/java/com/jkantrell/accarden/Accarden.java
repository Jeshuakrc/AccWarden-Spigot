package com.jkantrell.accarden;

import com.jkantrell.accarden.accoint.Account;
import com.jkantrell.accarden.accoint.AccountRepository;
import com.jkantrell.accarden.io.database.DataBase;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.UUID;

public final class Accarden extends JavaPlugin {

    //STATIC FIELDS
    private static final String PLUGIN_PATH = "./plugins/Arccarden/";

    //FIELDS
    private final DataBase dataBase_ = new DataBase();
    private AccountRepository accountRepository_ = null;

    //PLUGIN LOGIC
    @Override
    public void onEnable() {
        this.dataBase_.setFilePath(Accarden.PLUGIN_PATH + "database.db");
        this.dataBase_.setLogger(this.getLogger());
        this.dataBase_.setUp();
        try {
            this.dataBase_.executeSQL(new String(this.getResource("dbSetup.sql").readAllBytes(), StandardCharsets.UTF_8));
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        this.dataBase_.addEntity(Account.class, new Account.Parser(), "accounts");

        this.accountRepository_ = new AccountRepository(this.dataBase_);
        Account acc = this.accountRepository_.fromUUID(UUID.randomUUID());
        acc.setBedrock(true);
        acc.save();

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
