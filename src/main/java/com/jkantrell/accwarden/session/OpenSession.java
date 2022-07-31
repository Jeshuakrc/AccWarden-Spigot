package com.jkantrell.accwarden.session;

import com.jkantrell.accwarden.accoint.Account;
import com.jkantrell.accwarden.accoint.Platform;

import java.net.InetSocketAddress;

public record OpenSession(Account account, InetSocketAddress address, Platform platform) {

    //CUSTOM GETTERS
    public boolean isJava() {
        return this.platform.equals(Platform.JAVA);
    }
    public boolean isBedrock() {
        return this.platform.equals(Platform.BEDROCK);
    }
}
