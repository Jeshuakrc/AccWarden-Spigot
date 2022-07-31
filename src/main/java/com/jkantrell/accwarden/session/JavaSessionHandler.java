package com.jkantrell.accwarden.session;

import com.jkantrell.accwarden.accoint.AccountRepository;
import com.jkantrell.accwarden.accoint.Platform;
import org.bukkit.entity.Player;

public class JavaSessionHandler extends SessionHandler {

    //CONSTRUCTOR
    public JavaSessionHandler(AccountRepository repository, SessionHolder sessionHolder) {
        super(repository, sessionHolder);
    }

    //METHODS
    @Override
    public void handle(Player player) {
        if (this.sessionHolder.claim(player, Platform.JAVA)) {
            LoginManager.logIn(player);
        } else {
            LoginManager.reset(player);
        }
    }
}
