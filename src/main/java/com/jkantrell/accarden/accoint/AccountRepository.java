package com.jkantrell.accarden.accoint;

import com.jkantrell.accarden.io.database.DataBase;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class AccountRepository {

    //FIELDS
    private final DataBase dataBase_;

    //CONSTRUCTOR
    public AccountRepository(DataBase dataBase) {
        this.dataBase_ = dataBase;
    }

    //GETTERS
    public DataBase getDataBase_() {
        return this.dataBase_;
    }

    //METHODS
    public Account fromPlayer(Player player) {
        UUID id = player.getUniqueId();
        Optional<Account> optional = this.dataBase_.get(Account.class, id);
        if (optional.isEmpty()) {
            return new Account(id,this);
        }
        Account acc = optional.get();
        acc.setRepository(this);
        return acc;
    }
    public void save (Account toSave) {
        this.dataBase_.write(toSave);
    }

}
