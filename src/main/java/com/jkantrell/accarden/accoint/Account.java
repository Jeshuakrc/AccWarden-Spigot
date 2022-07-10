package com.jkantrell.accarden.accoint;

import com.jkantrell.accarden.io.database.DataBaseParser;
import com.jkantrell.accarden.io.database.DataBaseRow;
import com.jkantrell.accarden.io.database.Enitty;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

public class Account implements Enitty {

    //FIELDS
    private final UUID id_;
    private AccountRepository repository_;
    private String salt_ = "";
    private boolean javaLogged_ = false;
    private boolean bedrockLogged_ = false;
    private LocalDateTime joined_ = LocalDateTime.now();
    private LocalDateTime lastLogged_ = LocalDateTime.now();
    private char[] hash_ = new char[0];

    //CONSTRUCTORS
    Account(UUID id, AccountRepository repository) {
        this.id_ = id;
        this.repository_ = repository;

        byte[] saltBytes = new byte[64];
        new Random().nextBytes(saltBytes);
        this.salt_ = new String(saltBytes);
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
        this.salt_ = salt;
    }
    private void setHash(String hash) {
        this.hash_ = hash.toCharArray();
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
    public void changePassword(String newPassword) {

    }
    public boolean checkPassword(String toCheck) {
        return true;
    }
    public void save() {
        this.repository_.save(this);
    }

    @Override
    public String toString() {
        return
                "UUID: " + this.id_.toString()
                + "\nSalt: " + this.salt_
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
        public DataBaseRow toRow(Account src) {
            DataBaseRow row = new DataBaseRow("uuid", src.getId().toString());
            row.addColumn("salt", src.salt_);
            row.addColumn("hashed_password", new String(src.hash_));
            row.addColumn("java", src.hasJava());
            row.addColumn("bedrock", src.hasBedrock());
            row.addColumn("last_login", src.lastLogged_);

            return row;
        }
    }
}
