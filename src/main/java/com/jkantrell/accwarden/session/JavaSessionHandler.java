package com.jkantrell.accwarden.session;

import com.jkantrell.accwarden.AccWarden;
import com.jkantrell.accwarden.accoint.Account;
import com.jkantrell.accwarden.accoint.AccountRepository;
import com.jkantrell.accwarden.accoint.Platform;
import com.jkantrell.accwarden.accoint.exception.PasswordTooLongException;
import com.jkantrell.accwarden.accoint.exception.PasswordTooShortException;
import com.jkantrell.accwarden.io.Config;
import com.jkantrell.accwarden.io.LangProvider;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.LinkedList;

public class JavaSessionHandler extends SessionHandler {

    //STATIC FIELDS
    private static final int REFRESH_RATE = 40;

    //FIELDS
    private final LinkedList<Login> logins_ = new LinkedList<>();
    private Listener listener_;
    private BukkitRunnable informationRefresher_ = null;

    //CONSTRUCTOR
    public JavaSessionHandler(AccountRepository repository, SessionHolder sessionHolder, AccWarden plugin) {
        super(repository, sessionHolder, plugin);
        this.listener_ = null;
    }

    //METHODS
    @Override
    public void handle(Player player) {
        //Checking if there's a session already open
        if (this.sessionHolder.claim(player, Platform.JAVA)) {
            this.log("Bypassing login for " + player.getName() + " as a session was already open.");
            LoginManager.logIn(player);
            player.sendMessage(ChatColor.GREEN + this.plugin.getLangProvider().getEntry(player,"info.logged_in"));
            return;
        }

        //Logging the player out
        LoginManager.reset(player);
        this.addLogin_(new Login(this,player));
    }

    //PRIVATE METHODS
    private void addLogin_(Login login) {
        this.logins_.add(login);
        if (this.informationRefresher_ == null || this.informationRefresher_.isCancelled()) { this.resetRefresher_(); }
        if (this.listener_ == null) {
            this.listener_ = new Listener(this);
            this.plugin.getServer().getPluginManager().registerEvents(this.listener_, this.plugin);
        }

    }
    private void removeLogin_(Login login) {
        this.logins_.remove(login);
        if (this.logins_.isEmpty()) {
            if (!this.informationRefresher_.isCancelled()) {
                this.informationRefresher_.cancel();
            }
            if (this.listener_ != null) {
                HandlerList.unregisterAll(this.listener_);
                this.listener_ = null;
            }
        }
    }
    private void resetRefresher_() {
        if (this.plugin == null) { return; }
        this.informationRefresher_ = new BukkitRunnable() {
            @Override
            public void run() {
                JavaSessionHandler.this.logins_.forEach(Login::refresh);
            }
        };
        this.informationRefresher_.runTaskTimerAsynchronously(this.plugin, JavaSessionHandler.REFRESH_RATE, JavaSessionHandler.REFRESH_RATE);
    }


    private static class Login {

        //FIELDS
        private final JavaSessionHandler handler_;
        private final Player player_;
        private final Account account_;
        private final LoginMode mode_;
        private final String titleMessage_, subtitleMessage_, actionbarMessage_, chatMessage_;
        private int tries_ = 0;

        //CONSTRUCTORS
        Login(JavaSessionHandler handler, Player player) {
            //SETTING HANDLER
            this.handler_ = handler;

            //SETTING PLAYER
            this.player_ = player;

            //SETTING ACCOUNT
            boolean exists = this.handler_.accountRepository.exists(player);
            this.account_ = this.handler_.accountRepository.retrieve(player);

            //SETTING MODE
            LoginMode mode;
            if (this.handler_.accountRepository.exists(player)) {
                if (this.account_.hasJava()) {
                    this.handler_.log(player.getName() + " already has an account.");
                    mode = LoginMode.EXISTING;
                } else {
                    this.handler_.log(player.getName() + " already has an account. Never connected from Java before.");
                    mode = LoginMode.NEW_IN_PLATFORM;
                }
            } else {
                this.handler_.log(player.getName() + " is connecting for the first time.");
                mode = LoginMode.NEW;
            }
            this.mode_ = mode;

            //DEFINING MESSAGES
            String basePath = "login.java." + switch (mode) {
                case NEW -> "new"; case NEW_IN_PLATFORM -> "new_in_java"; case EXISTING -> "existing";
            } + ".";
            LangProvider lp = this.handler_.langProvider;

            this.titleMessage_ = lp.getEntry(player, basePath + "title");
            this.subtitleMessage_ = lp.getEntry(player, basePath + "subtitle");
            this.actionbarMessage_ = lp.getEntry(player,basePath + "actionbar");
            this.chatMessage_ = lp.getEntry(player,basePath + "chat");

            this.refresh();
            this.showChatMessage_();
        }

        //METHODS
        void refresh() {

            if (!(this.titleMessage_.equals("") && this.subtitleMessage_.equals(""))) {
                this.player_.sendTitle(this.titleMessage_, this.subtitleMessage_, 0, JavaSessionHandler.REFRESH_RATE, 40);
            }

            if (this.actionbarMessage_.equals("")) {
                this.player_.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(this.actionbarMessage_));
            }
        }
        void readMessage(AsyncPlayerChatEvent e, Listener listener) {
            String[] words = e.getMessage().split(" ");
            LangProvider lp = this.handler_.langProvider;

            switch (this.mode_) {
                case NEW -> {
                    if (words.length != 2) { return; }
                    e.setCancelled(true);
                    boolean match = false;
                    try {
                        match = this.account_.setPassword(words[0], words[1]);
                    } catch (PasswordTooLongException ex) {
                        this.player_.sendMessage(ChatColor.RED + lp.getEntry(this.player_,"error.invalid_input.too_long", ex.getMaxLength()));
                        return;
                    } catch (PasswordTooShortException ex) {
                        this.player_.sendMessage(ChatColor.RED + lp.getEntry(this.player_,"error.invalid_input.too_short", ex.getMinLength()));
                        return;
                    }

                    if (!match) {
                        this.player_.sendMessage(ChatColor.RED + lp.getEntry(this.player_,"error.invalid_input.no_match"));
                        this.showChatMessage_();
                        return;
                    }
                    this.account_.setJava(true);
                    this.account_.save();
                }
                case NEW_IN_PLATFORM, EXISTING -> {
                    if (words.length != 1) { return; }
                    e.setCancelled(true);
                    boolean match = this.account_.checkPassword(words[0]);
                    if (match) { break; }

                    this.tries_++;
                    Config conf = this.handler_.plugin.CONFIG;
                    String endPath = "fine";
                    if (this.tries_ >= conf.failLoginOdd) {
                        int next = conf.failLoginOdd + conf.failLoginWarn;
                        if (this.tries_ >= next && conf.failLoginAccountLock) {
                            next += conf.failLoginLock;
                            if (this.tries_ >= next && conf.failLoginLock > 0) {
                                this.account_.lock();
                                break;
                            } else { endPath = "warn"; }
                        } else { endPath = "odd"; }
                    }

                    this.player_.sendMessage(ChatColor.RED + lp.getEntry(this.player_,"error.incorrect_password." + endPath ));
                    this.showChatMessage_();
                    return;
                }
            }

            if (!this.account_.hasJava()) {
                this.account_.setJava(true);
                this.account_.save();
            }
            Bukkit.getScheduler().runTask(this.handler_.plugin, () -> {
                LoginManager.logIn(this.player_);   //Cannot change gameMode from async
                this.handler_.removeLogin_(this);
                this.player_.sendMessage(ChatColor.GREEN + lp.getEntry(this.player_,"info.logged_in"));
            });
        }
        private void showChatMessage_() {
            if (!this.chatMessage_.equals("")) {
                this.player_.sendMessage(this.chatMessage_);
            }
        }
    }

    private static class Listener implements org.bukkit.event.Listener {

        private final JavaSessionHandler handler_;

        Listener(JavaSessionHandler handler) {
            this.handler_ = handler;
        }

        @EventHandler(priority = EventPriority.NORMAL)
        void onPlayerMessage(AsyncPlayerChatEvent e) {
            Iterator<Login> i = this.handler_.logins_.iterator();
            while (i.hasNext()) {
                Login l = i.next();
                if (l.player_.equals(e.getPlayer())) {
                    l.readMessage(e,this);
                }
            }
        }
        @EventHandler
        void onPlayerQuit(PlayerQuitEvent e) {
            Iterator<Login> i = this.handler_.logins_.iterator();
            while (i.hasNext()) {
                Login l = i.next();
                if (l.player_.equals(e.getPlayer())) { i.remove(); }
            }
        }
    }
}
