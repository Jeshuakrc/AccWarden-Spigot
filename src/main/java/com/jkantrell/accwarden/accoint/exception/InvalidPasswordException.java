package com.jkantrell.accwarden.accoint.exception;

import com.jkantrell.accwarden.accoint.Account;

public abstract class InvalidPasswordException extends AccountException {

    //FIELDS
    private final String password_;

    public InvalidPasswordException(Account account, String inputPassword) {
        super(account);
        this.password_ = inputPassword;
    }

    //GETTERS
    public String getInputPassword() {
        return this.password_;
    }
}
