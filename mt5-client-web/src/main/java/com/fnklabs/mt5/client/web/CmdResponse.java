package com.fnklabs.mt5.client.web;

public class CmdResponse<T> extends Response {
    private T answer;

    public T getAnswer() {
        return answer;
    }

    public CmdResponse<T> setAnswer(T answer) {
        this.answer = answer;
        return this;
    }
}
