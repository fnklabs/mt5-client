package com.fnklabs.mt5.client.web;

import com.fnklabs.mt5.client.Mt5User;

public class AddUserResponse extends Response {

    private Mt5User answer;

    public Mt5User getAnswer() {
        return answer;
    }

    public void setAnswer(Mt5User answer) {
        this.answer = answer;
    }
}
