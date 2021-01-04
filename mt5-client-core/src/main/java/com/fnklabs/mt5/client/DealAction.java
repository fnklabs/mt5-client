package com.fnklabs.mt5.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = DealActionDeserializer.class)
public enum DealAction {
    DEAL_BUY(true, false, 0),
    DEAL_SELL(true, false, 1),
    DEAL_BALANCE(false, true, 2),
    DEAL_CREDIT(false, true, 3),
    DEAL_CHARGE(false, false, 4),
    DEAL_CORRECTION(false, false, 5),
    DEAL_BONUS(false, false, 6),
    DEAL_COMMISSION(false, false, 7),
    DEAL_COMMISSION_DAILY(false, false, 8),
    DEAL_COMMISSION_MONTHLY(false, false, 9),
    DEAL_AGENT_DAILY(false, false, 10),
    DEAL_AGENT_MONTHLY(false, false, 11),
    DEAL_INTERESTRATE(false, false, 12),
    DEAL_BUY_CANCELED(false, false, 13),
    DEAL_SELL_CANCELED(false, false, 14),
    DEAL_DIVIDEND(false, false, 15),
    DEAL_DIVIDEND_FRANKED(false, false, 16),
    DEAL_TAX(false, false, 17),
    DEAL_AGENT(false, false, 18),
    DEAL_SO_COMPENSATION(false, false, 19),
    DEAL_FIRST(false, false, 0),
    DEAL_LAST(false, false, 19),
    ;

    private final boolean isTradeOperation;
    private final boolean isBalanceOperation;

    private final int code;

    DealAction(boolean isTradeOperation, boolean isBalanceOperation, int code) {
        this.isTradeOperation = isTradeOperation;
        this.isBalanceOperation = isBalanceOperation;
        this.code = code;
    }

    public boolean isTradeOperation() {
        return isTradeOperation;
    }

    public boolean isBalanceOperation() {
        return isBalanceOperation;
    }

    @JsonValue
    public int getCode() {
        return code;
    }
}
