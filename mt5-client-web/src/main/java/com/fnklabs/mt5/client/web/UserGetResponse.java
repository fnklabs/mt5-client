package com.fnklabs.mt5.client.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fnklabs.mt5.client.Mt5User;

public class UserGetResponse extends Response {
    @JsonProperty("answer")
    private Mt5User user;

    public Mt5User getUser() {
        return user;
    }

    public void setUser(Mt5User user) {
        this.user = user;
    }
}
