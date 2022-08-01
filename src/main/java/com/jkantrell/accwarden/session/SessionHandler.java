package com.jkantrell.accwarden.session;

import com.jkantrell.accwarden.AccWarden;
import com.jkantrell.accwarden.accoint.AccountRepository;
import com.jkantrell.accwarden.io.LangProvider;
import org.bukkit.entity.Player;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class SessionHandler {

    //ASSETS
    protected enum LoginMode { NEW, NEW_IN_PLATFORM, EXISTING }

    //FIELDS
    protected final AccountRepository accountRepository;
    protected final SessionHolder sessionHolder;
    protected final AccWarden plugin;
    protected final LangProvider langProvider;
    private Level loggingLevel_ = Level.FINEST;

    //CONSTRUCTORS
    public SessionHandler(AccountRepository repository, SessionHolder sessionHolder, AccWarden plugin) {
        this.accountRepository = repository;
        this.sessionHolder = sessionHolder;
        this.plugin = plugin;
        this.langProvider = this.plugin.getLangProvider();
    }

    //SETTERS
    public void setLoggingLevel(Level loggingLevel) {
        this.loggingLevel_ = loggingLevel;
    }

    //GETTERS
    public AccountRepository getAccountRepository() {
        return this.accountRepository;
    }
    public SessionHolder getSessionHolder() {
        return this.sessionHolder;
    }

    //METHODS
    protected void log(String message, Level level) {
        this.plugin.getLogger().log(level, message);
    }
    protected void log(String message) {
        this.log(message, this.loggingLevel_);
    }

    //ABSTRACT METHODS
    abstract public void handle(Player player);

}
