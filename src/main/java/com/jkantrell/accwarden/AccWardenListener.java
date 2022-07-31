package com.jkantrell.accwarden;

import com.jkantrell.accwarden.accoint.Account;
import com.jkantrell.accwarden.accoint.AccountRepository;
import com.jkantrell.accwarden.accoint.Platform;
import com.jkantrell.accwarden.session.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class AccWardenListener implements Listener {

    //FIELDS
    private final AccWarden plugin_;
    private final AccountRepository accountRepository_;
    private final SessionHolder sessionHolder_;
    private final JavaSessionHandler javaHandler_;
    private final BedrockSessionHandler bedrockHandler_;

    //CONSTRUCTORS
    public AccWardenListener(AccWarden plugin) {
        this.plugin_ = plugin;
        this.accountRepository_ = this.plugin_.getAccountRepository();
        this.sessionHolder_ = this.plugin_.getSessionHolder();;
        this.javaHandler_ = new JavaSessionHandler(this.accountRepository_, this.sessionHolder_);
        this.bedrockHandler_ = (plugin.isBedrockOn()) ? new BedrockSessionHandler(this.accountRepository_, this.sessionHolder_) : null;
    }

    //EVENT HANDLERS
    @EventHandler
    void OnPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        SessionHandler handler = (this.plugin_.isBedrockOn() && this.bedrockHandler_.isBedrock(player)) ?
                this.bedrockHandler_ : this.javaHandler_;
        handler.handle(player);
    }

    @EventHandler
    void OnPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (!this.accountRepository_.exists(player)) { return; }
        Account account = this.accountRepository_.fromPlayer(player);
        Platform platform = (this.plugin_.isBedrockOn() && this.bedrockHandler_.isBedrock(player)) ? Platform.BEDROCK : Platform.JAVA;
        this.sessionHolder_.openNew(account, player, platform);
    }

    @EventHandler
    void OnPlayerMove(PlayerMoveEvent e) {
        if (LoginManager.isLogged(e.getPlayer())) { return; }
        e.setCancelled(true);
    }

    @EventHandler
    void OnPlayerOpenInventory(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player player)) { return; }
        if (LoginManager.isLogged(player)) { return; }
        e.setCancelled(true);
    }
}
