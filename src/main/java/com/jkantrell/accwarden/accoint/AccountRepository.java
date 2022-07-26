package com.jkantrell.accwarden.accoint;

import com.jkantrell.accwarden.io.database.DataBase;
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
        return this.fromUUID(player.getUniqueId());
    }
    public Account fromUUID(UUID id) {
        Optional<Account> optional = this.dataBase_.get(Account.class, id);
        if (optional.isEmpty()) {
            return new Account(id,this);
        }
        Account acc = optional.get();
        acc.setRepository(this);
        return acc;
    }
    public boolean exists(Player player) {
        return this.exists(player.getUniqueId());
    }
    public boolean exists(UUID id) {
        return this.dataBase_.get(Account.class, id).isPresent();
    }
    public void save (Account toSave) {
        this.dataBase_.write(toSave);
    }

}
