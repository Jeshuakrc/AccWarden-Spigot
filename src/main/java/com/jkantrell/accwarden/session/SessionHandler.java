package com.jkantrell.accwarden.session;

import com.jkantrell.accwarden.accoint.AccountRepository;
import org.bukkit.entity.Player;

public abstract class SessionHandler {

    //FIELDS
    protected final AccountRepository accountRepository;
    protected final SessionHolder sessionHolder;

    //CONSTRUCTORS
    public SessionHandler(AccountRepository repository, SessionHolder sessionHolder) {
        this.accountRepository = repository;
        this.sessionHolder = sessionHolder;
    }

    //METHODS
    abstract public void handle(Player player);

}
