package com.jkantrell.accwarden.session;

import com.jkantrell.accwarden.AccWarden;
import com.jkantrell.accwarden.accoint.Account;
import com.jkantrell.accwarden.accoint.AccountRepository;
import com.jkantrell.accwarden.accoint.exception.PasswordTooLongException;
import com.jkantrell.accwarden.accoint.exception.PasswordTooShortException;
import com.jkantrell.accwarden.io.Config;
import com.jkantrell.accwarden.io.LangProvider;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.geysermc.cumulus.CustomForm;
import org.geysermc.cumulus.ModalForm;
import org.geysermc.cumulus.response.CustomFormResponse;
import org.geysermc.cumulus.response.ModalFormResponse;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

public class BedrockSessionHandler extends SessionHandler {

    //FIELDS
    private final FloodgateApi floodgateApi_ = FloodgateApi.getInstance();
    private final LinkedList<BedrockSessionHandler.Login> logins_ = new LinkedList<>();
    private BedrockSessionHandler.Listener listener_;

    public BedrockSessionHandler(AccountRepository repository, SessionHolder sessionHolder, AccWarden plugin) {
        super(repository, sessionHolder, plugin);
    }

    //METHODS
    public boolean isBedrock(Player player) {
        return this.isBedrock(player.getUniqueId());
    }
    public boolean isBedrock(UUID uuid) {
        return this.floodgateApi_.isFloodgatePlayer(uuid);
    }
    @Override
    public void handle(Player player) {
        FloodgatePlayer fgPlayer = this.floodgateApi_.getPlayer(player.getUniqueId());
        if (fgPlayer == null) {
            LoginManager.reset(player);
            return;
        }

        Account account = this.accountRepository.retrieve(player);

        LoginMode mode;
        if (account.hasBedrock()) {
            this.log("Bedrock player with UUID '" + account.getId() + "' is already registered with Bedrock access. Logging in.");
            LoginManager.logIn(player);
            player.sendMessage(ChatColor.GREEN + this.plugin.getLangProvider().getEntry(player,"info.logged_in"));
            return;
        } else if (account.hasJava()) {
            mode = LoginMode.NEW_IN_PLATFORM;
        } else {
            mode = LoginMode.NEW;
        }

        LoginManager.reset(player);
        this.addLogin_(new Login(this, player, mode));
    }

    //PRIVATE METHODS
    private void addLogin_(BedrockSessionHandler.Login login) {
        this.logins_.add(login);
        if (this.listener_ == null) {
            this.listener_ = new BedrockSessionHandler.Listener(this);
            this.plugin.getServer().getPluginManager().registerEvents(this.listener_, this.plugin);
        }

    }
    private void removeLogin_(BedrockSessionHandler.Login login) {
        this.logins_.remove(login);
        if (this.logins_.isEmpty() && this.listener_ != null) {
            HandlerList.unregisterAll(this.listener_);
            this.listener_ = null;
        }
    }
    private boolean sendRegisterForm_(Player player, Account account, LoginMode mode) {
        BiConsumer<CustomForm, String> responseHandler = (f, s) -> {
            CustomFormResponse response = (CustomFormResponse) f.parseResponse(s);

            if (!response.isCorrect()) {
                BedrockSessionHandler.this.sendRegisterForm_(player,account,mode);
                this.log("Invalid form", Level.WARNING);
                return;
            }

            String  pw1 = response.getInput(1),
                    pw2 = response.getInput(2);

            boolean match = false;
            try {
                match = account.setPassword(pw1, pw2);
            } catch (PasswordTooShortException ex) {
                BedrockSessionHandler.this.sendErrorForm_(player, "Password too short", r -> BedrockSessionHandler.this.sendRegisterForm_(player,account,mode));
            } catch (PasswordTooLongException ex) {
                BedrockSessionHandler.this.sendErrorForm_(player, "Password too long", r -> BedrockSessionHandler.this.sendRegisterForm_(player,account,mode));
            }

            if (!match) {
                BedrockSessionHandler.this.sendErrorForm_(player, "Passwords don't match.", r -> BedrockSessionHandler.this.sendRegisterForm_(player,account,mode));
            } else {
                account.setBedrock(true);
                account.save();
                LoginManager.logIn(player);
                player.sendMessage(ChatColor.GREEN + BedrockSessionHandler.this.plugin.getLangProvider().getEntry(player,"info.logged_in"));
            }
        };

        return this.floodgateApi_.sendForm(player.getUniqueId(), CustomForm.builder()
                .label("Welcome to the server! To prevent your user name from being stolen in Java, define a password.")
                .input("Password")
                .input("Confirm password")
                .responseHandler(responseHandler)
        );
    }
    private void sendErrorForm_(Player player, String message, Consumer<ModalFormResponse> responseHandler) {
        this.floodgateApi_.sendForm(player.getUniqueId(), ModalForm.builder()
                .content(message)
                .button1("Accept")
                .responseHandler((f,s) -> responseHandler.accept((ModalFormResponse) f.parseResponse(s)))
        );
    }

    private static class Login {

        //FIELDS
        private final BedrockSessionHandler handler_;
        private final Player player_;
        private final Account account_;
        private final LoginMode mode_;
        private final String    disclosure_, passwordField_, passwordConfirmField_, placeholder_,
                                loggedMessage_, notRegisteredMessage_, notLoggedMessage_, notMatchMessage_,
                                wrongPasswordMessage1_, wrongPasswordMessage2_, wrongPasswordMessage3_,
                                retryButton_, quitButton_;
        private int tries_ = 0;

        //CONSTRUCTORS
        Login(BedrockSessionHandler handler, Player player, LoginMode mode) {
            //SETTING HANDLER
            this.handler_ = handler;

            //SETTING PLAYER
            this.player_ = player;

            //SETTING ACCOUNT
            this.account_ = this.handler_.accountRepository.retrieve(player);

            //SETTING MODE
            this.mode_ = mode;

            //DEFINING MESSAGES
            String basePath = "login.bedrock." + switch (mode) {
                case NEW -> "new"; case NEW_IN_PLATFORM -> "new_in_bedrock"; case EXISTING -> "existing";
            } + ".";
            LangProvider langProvider = this.handler_.plugin.getLangProvider();
            Function<String,String> getEntry = s -> langProvider.getEntry(player,basePath + s);

            this.disclosure_ = getEntry.apply("disclosure");
            this.passwordField_ = getEntry.apply("fields.password");
            this.passwordConfirmField_ = (this.mode_.equals(LoginMode.NEW)) ? getEntry.apply("fields.confirm_password") : null;
            this.placeholder_ = getEntry.apply("fields.placeholder");

            String basePath1 = "error.kicked.";
            this.notLoggedMessage_ = langProvider.getEntry(player, basePath1 + "not_logged");
            this.notRegisteredMessage_ = langProvider.getEntry(player, basePath1 + "not_registered");

            this.loggedMessage_ = langProvider.getEntry(player, "info.logged_in");
            this.retryButton_ = langProvider.getEntry(player, "login.bedrock.error_form.buttonRetry");
            this.quitButton_ = langProvider.getEntry(player, "login.bedrock.error_form.buttonQuit");
            this.notMatchMessage_ = langProvider.getEntry(player, "error.invalid_input.no_match");

            basePath1 = "error.incorrect_password.";
            this.wrongPasswordMessage1_ = langProvider.getEntry(player,  basePath1 + "fine");
            this.wrongPasswordMessage2_ = langProvider.getEntry(player,  basePath1 + "odd");
            this.wrongPasswordMessage3_ = langProvider.getEntry(player,  basePath1 + "warn");
        }

        //METHODS
        public void sendForm() {
            CustomForm.Builder formBuilder = CustomForm.builder();
            formBuilder
                    .label(this.disclosure_)
                    .input(this.passwordField_, this.placeholder_);
            if (this.mode_.equals(LoginMode.NEW)) {
                formBuilder.input(this.passwordConfirmField_, this.placeholder_);
            }
            formBuilder.responseHandler((this.mode_.equals(LoginMode.NEW)) ?
                (f, s) -> {
                    CustomFormResponse response = f.parseResponse(s);
                    if (!response.isCorrect()) { this.kick_(); return; }

                    String  pw1 = response.getInput(1),
                            pw2 = response.getInput(2),
                            error = null;

                    boolean match = false;
                    try {
                        match = this.account_.setPassword(pw1, pw2);
                    } catch (PasswordTooShortException ex) {
                        error = this.handler_.plugin.getLangProvider().getEntry(this.player_, "error.invalid_input.too_short", ex.getMinLength());
                    } catch (PasswordTooLongException ex) {
                        error = this.handler_.plugin.getLangProvider().getEntry(this.player_, "error.invalid_input.too_long", ex.getMaxLength());
                    }

                    if (error != null) {
                        this.sendErrorMessage_(error);
                        return;
                    }

                    if (!match) {
                        this.sendErrorMessage_(this.notMatchMessage_);
                        return;
                    }

                    this.login_();
                } : (f, s) -> {
                    CustomFormResponse response = f.parseResponse(s);
                    if (!response.isCorrect()) {
                        this.kick_();
                        return;
                    }

                    String pw = response.getInput(1);
                    boolean match = this.account_.checkPassword(pw);
                    if (match) {
                        this.login_();
                        return;
                    }

                    this.tries_++;
                    Config conf = this.handler_.plugin.CONFIG;
                    String errorMessage = this.wrongPasswordMessage1_;
                    if (this.tries_ >= conf.failLoginOdd) {
                        int next = conf.failLoginOdd + conf.failLoginWarn;
                        if (this.tries_ >= next && conf.failLoginAccountLock) {
                            next += conf.failLoginLock;
                            if (this.tries_ >= next && conf.failLoginLock > 0) {
                                this.account_.lock();
                            } else { errorMessage = this.wrongPasswordMessage3_; }
                        } else { errorMessage = this.wrongPasswordMessage2_; }
                    }

                    this.sendErrorMessage_(errorMessage);
                }
            );

            this.handler_.floodgateApi_.sendForm(this.player_.getUniqueId(), formBuilder);
            this.handler_.removeLogin_(this);
        }

        //PRIVATE METHODS
        private void sendErrorMessage_(String... errors) {
            ModalForm.Builder formBuilder = ModalForm.builder();

            StringBuilder errorBuilder = new StringBuilder();
            Arrays.stream(errors).forEach(s -> errorBuilder.append(s).append("\n"));

            formBuilder
                    .content(errorBuilder.toString())
                    .button1(this.retryButton_)
                    .button2(this.quitButton_);
            formBuilder.responseHandler((f, s) -> {
                ModalFormResponse response = f.parseResponse(s);
                if (!(response.isCorrect() && response.getResult())) { this.kick_(); return; }
                this.sendForm();
            });

            this.handler_.floodgateApi_.sendForm(this.player_.getUniqueId(), formBuilder);
        }
        private void kick_() {
            this.player_.kickPlayer((this.mode_.equals(LoginMode.NEW)) ? this.notRegisteredMessage_ : this.notLoggedMessage_);
        }
        private void login_() {
            this.account_.setBedrock(true);
            this.account_.save();
            LoginManager.logIn(this.player_);
            this.player_.sendMessage(ChatColor.GREEN + this.loggedMessage_);
        }
    }

    private static class Listener implements org.bukkit.event.Listener {

        private final BedrockSessionHandler handler_;

        Listener(BedrockSessionHandler handler) {
            this.handler_ = handler;
        }

        @EventHandler(priority = EventPriority.NORMAL)
        void onPlayerMove(PlayerMoveEvent e) {
            Iterator<BedrockSessionHandler.Login> i = this.handler_.logins_.iterator();
            while (i.hasNext()) {
                BedrockSessionHandler.Login l = i.next();
                if (l.player_.equals(e.getPlayer())) {
                    l.sendForm();
                }
            }
        }
        @EventHandler
        void onPlayerQuit(PlayerQuitEvent e) {
            Iterator<BedrockSessionHandler.Login> i = this.handler_.logins_.iterator();
            while (i.hasNext()) {
                BedrockSessionHandler.Login l = i.next();
                if (l.player_.equals(e.getPlayer())) { i.remove(); }
            }
        }
    }
}
