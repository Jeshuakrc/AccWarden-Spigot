package com.jkantrell.accwarden.accoint.exception;

import com.jkantrell.accwarden.accoint.Account;

public abstract class AccountException extends RuntimeException {

    //FIELDS
    private final Account account_;

    //CONSTRUCTOR
    public AccountException(Account account) {
        this.account_ = account;
    }

    //GETTERS
    public Account getAccount() {
        return this.account_;
    }
}
