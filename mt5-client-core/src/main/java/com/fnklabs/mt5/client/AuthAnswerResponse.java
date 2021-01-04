package com.fnklabs.mt5.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

class AuthAnswerResponse extends Mt5Response<Void> {
    @JsonProperty("cli_rand_answer")
    private String cliRandAnswer;


    public String getCliRandAnswer() {
        return cliRandAnswer;
    }

    public void setCliRandAnswer(String cliRandAnswer) {
        this.cliRandAnswer = cliRandAnswer;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("code", getCode())
                          .add("cliRandAnswer", cliRandAnswer)
                          .toString();
    }
}
