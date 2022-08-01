package com.jkantrell.accwarden.accoint.exception;

import com.jkantrell.accwarden.accoint.Account;

public class PasswordTooShortException extends InvalidPasswordException{

    //FIELDS
    private final int minLength_;

    public PasswordTooShortException(Account account, String inputPassword, int minLength) {
        super(account, inputPassword);
        this.minLength_ = minLength;
    }

    //GETTERS
    public int getMinLength() {
        return this.minLength_;
    }
}
