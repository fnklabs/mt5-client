package com.fnklabs.mt5.client.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class AuthStartResponse extends Response {
    @JsonProperty("version_access")
    private String versionAccess;
    @JsonProperty("srv_rand")
    private String srvRand;

    public String getVersionAccess() {
        return versionAccess;
    }

    public void setVersionAccess(String versionAccess) {
        this.versionAccess = versionAccess;
    }

    public String getSrvRand() {
        return srvRand;
    }

    public void setSrvRand(String srvRand) {
        this.srvRand = srvRand;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("versionAccess", versionAccess)
                          .add("srvRand", srvRand)
                          .toString();
    }
}
