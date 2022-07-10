package com.jkantrell.accarden.accoint;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.jkantrell.accarden.io.database.DataBaseParser;
import com.jkantrell.accarden.io.database.Enitty;
import org.bukkit.Bukkit;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Account implements Enitty {

    //FIELDS
    private final UUID id_;
    private AccountRepository repository_;
    private boolean javaLogged_ = false;
    private boolean bedrockLogged_ = false;
    private LocalDateTime joined_ = LocalDateTime.now();
    private LocalDateTime lastLogged_ = LocalDateTime.now();
    private byte[] hash_ = new byte[0];
    private byte[] salt_ = new byte[16];

    //CONSTRUCTORS
    Account(UUID id, AccountRepository repository) {
        this.id_ = id;
        this.repository_ = repository;

        Random random = new Random();
        for (int i = 0; i < this.salt_.length; i++) {
            this.salt_[i] = (byte) random.nextInt(97, 123);
        }
    }
    Account(String id, AccountRepository repository){
        this(UUID.fromString(id), repository);
    }

    //Setters
    public void setJava(boolean b) {
        this.javaLogged_ = b;
    }
    public void setBedrock(boolean b) {
        this.bedrockLogged_ = b;
    }
    private void setSalt(String salt) {
        this.salt_ = salt.getBytes(StandardCharsets.UTF_8);
    }
    private void setHash(String hash) {
        this.hash_ = hash.getBytes(StandardCharsets.UTF_8);
    }
    private void setJoined(LocalDateTime dateTime) {
        this.joined_ = dateTime;
    }
    void setRepository(AccountRepository repository) {
        this.repository_ = repository;
    }

    //GETTERS
    public UUID getId() {
        return this.id_;
    }
    public LocalDateTime whenJoined() {
        return this.joined_;
    }
    public boolean hasJava() {
        return this.javaLogged_;
    }
    public boolean hasBedrock() {
        return this.bedrockLogged_;
    }
    public AccountRepository getRepository() {
        return this.repository_;
    }

    //METHODS
    public boolean changePassword(String newPassword, String newPasswordConfirm) {
        BCrypt.HashData hash = BCrypt.withDefaults().hashRaw(6, this.salt_, newPassword.getBytes(StandardCharsets.UTF_8));
        boolean confirm = BCrypt.verifyer().verify(newPasswordConfirm.getBytes(StandardCharsets.UTF_8),hash).verified;
        if (!confirm) { return false; }
        this.hash_ = BCrypt.Version.VERSION_2A.formatter.createHashMessage(hash);
        return true;
    }
    public boolean checkPassword(String toCheck) {
        return BCrypt.verifyer().verify(toCheck.getBytes(StandardCharsets.UTF_8),this.hash_).verified;
    }
    public void save() {
        this.repository_.save(this);
    }

    @Override
    public String toString() {
        return
                "UUID: " + this.id_.toString()
                + "\nSalt: " + new String(this.salt_,StandardCharsets.UTF_8)
                + "\nJava: " + this.javaLogged_
                + "\nBedrock:" + this.bedrockLogged_
                + "\nJoined on: " + this.joined_.toString()
                + "\nLast joined on: " + this.lastLogged_.toString();
    }

    //CLASSES
    public static class Parser implements DataBaseParser<Account> {

        @Override
        public Account toEntity(ResultSet src) throws SQLException {
            Account acc = new Account(src.getString("uuid"),null);
            acc.setHash(src.getString("hashed_password"));
            acc.setSalt(src.getString("salt"));
            acc.setJava(src.getBoolean("java"));
            acc.setBedrock(src.getBoolean("bedrock"));
            acc.setJoined(src.getTimestamp("joined").toLocalDateTime());
            return acc;
        }

        @Override
        public Map<String,Object> toRow(Account src) {
            return Map.ofEntries(
                Map.entry("salt", new String(src.salt_,StandardCharsets.UTF_8)),
                Map.entry("hashed_password", new String(src.hash_,StandardCharsets.UTF_8)),
                Map.entry("java", src.hasJava()),
                Map.entry("bedrock", src.hasBedrock()),
                Map.entry("last_login", src.lastLogged_)
            );
        }
    }
}
