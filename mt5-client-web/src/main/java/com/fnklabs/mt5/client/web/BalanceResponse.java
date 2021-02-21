package com.fnklabs.mt5.client.web;

public class BalanceResponse extends Response {
    private Ticket answer;

    public Ticket getAnswer() {
        return answer;
    }

    public void setAnswer(Ticket answer) {
        this.answer = answer;
    }
}
