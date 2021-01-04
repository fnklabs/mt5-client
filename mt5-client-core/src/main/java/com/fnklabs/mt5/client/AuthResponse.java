package com.fnklabs.mt5.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

class AuthResponse extends Mt5Response<Void> {

    @JsonProperty("srv_rand")
    private String srvRand;


    public String getSrvRand() {
        return srvRand;
    }

    public void setSrvRand(String srvRand) {
        this.srvRand = srvRand;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("code", getCode())
                          .add("answer", srvRand)
                          .toString();
    }
}
