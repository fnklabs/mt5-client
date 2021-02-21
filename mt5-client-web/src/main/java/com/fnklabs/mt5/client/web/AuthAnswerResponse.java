package com.fnklabs.mt5.client.web;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthAnswerResponse extends Response {
    @JsonProperty("version_access")
    private String versionAccess;

    @JsonProperty("version_trade")
    private String versionTrade;

    @JsonProperty("cli_rand_answer")
    private String cliRandAnswer;

    public String getVersionAccess() {
        return versionAccess;
    }

    public void setVersionAccess(String versionAccess) {
        this.versionAccess = versionAccess;
    }

    public String getVersionTrade() {
        return versionTrade;
    }

    public void setVersionTrade(String versionTrade) {
        this.versionTrade = versionTrade;
    }

    public String getCliRandAnswer() {
        return cliRandAnswer;
    }

    public void setCliRandAnswer(String cliRandAnswer) {
        this.cliRandAnswer = cliRandAnswer;
    }
}
