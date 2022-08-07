package com.jkantrell.accwarden.command;

import com.jkantrell.accwarden.AccWarden;
import com.jkantrell.accwarden.accoint.Account;
import com.jkantrell.accwarden.accoint.AccountRepository;
import com.jkantrell.accwarden.accoint.exception.PasswordTooLongException;
import com.jkantrell.accwarden.accoint.exception.PasswordTooShortException;
import com.jkantrell.accwarden.io.LangProvider;
import com.jkantrell.commander.command.CommandHolder;
import com.jkantrell.commander.command.annotations.Command;
import com.jkantrell.commander.command.annotations.Requires;
import com.jkantrell.commander.command.provider.identify.Sender;
import com.jkantrell.commander.exception.CommandUnrunnableException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command(label = "account")
public class AccountCommand extends CommandHolder {

    //FIELDS
    private final AccWarden plugin_;
    private final AccountRepository accountRepository_;
    private final LangProvider langProvider_;

    //CONSTRUCTOR
    public AccountCommand(AccWarden accWardenInstance) {
        this.plugin_ = accWardenInstance;
        this.accountRepository_ = this.plugin_.getAccountRepository();
        this.langProvider_ = this.plugin_.getLangProvider();
    }

    //COMMANDS
    @Command(label = "reset")
    public boolean reset(@Sender Player player) throws CommandUnrunnableException {
        Account acc = this.getAccount_(player);
        acc.delete();
        return true;
    }

    @Command(label = "reset")
    @Requires(permission = "AccWarden.command.reset")
    public boolean resetOf(CommandSender sender, Player player) throws CommandUnrunnableException {
        this.reset(player);
        sender.sendMessage(this.langProvider_.getEntry(player,"command.reset.success"), player.getName());
        return true;
    }

    @Command(label = "changePassword")
    public boolean changePassword(@Sender Player player, String password, String confirmPassword) throws CommandUnrunnableException {
        Account acc = this.getAccount_(player);
        boolean match = false;
        try {
            match = acc.setPassword(password, confirmPassword);
        } catch (PasswordTooLongException ex) {
            throw new CommandUnrunnableException(this.langProvider_.getEntry(player,"error.invalid_input.too_long", ex.getMaxLength()));
        } catch (PasswordTooShortException ex) {
            throw  new CommandUnrunnableException(this.langProvider_.getEntry(player,"error.invalid_input.too_short", ex.getMinLength()));
        }

        if (!match) {
            throw new CommandUnrunnableException(this.langProvider_.getEntry(player,"error.invalid_input.no_match"));
        }

        player.sendMessage(this.langProvider_.getEntry(player,"command.changePassword.success"));
        return false;
    }


    //PRIVATE METHODS
    private Account getAccount_(Player player) throws CommandUnrunnableException {
        if (!this.accountRepository_.exists(player)) {
            throw new CommandUnrunnableException(
                    this.langProvider_.getEntry(player,"command.unexisting_account", player.getName())
            );
        }
        return this.accountRepository_.retrieve(player);
    }
}
