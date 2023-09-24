package com.fnklabs.mt5.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.math.BigDecimal;

public class Deal {
    @JsonProperty("Deal")
    private String deal;

    @JsonProperty("Profit")
    private BigDecimal profit;

    @JsonProperty("Action")
    private DealAction action;

    @JsonProperty("Symbol")
    private String symbol;

    @JsonProperty("Volume")
    private int volume;

    public String getSymbol() {
        return symbol;
    }

    public Deal setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public int getVolume() {
        return volume;
    }

    public Deal setVolume(int volume) {
        this.volume = volume;
        return this;
    }

    public DealAction getAction() {
        return action;
    }

    public void setAction(DealAction action) {
        this.action = action;
    }

    public String getDeal() {
        return deal;
    }

    public void setDeal(String deal) {
        this.deal = deal;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public String getId() {
        return getDeal();
    }

    public BigDecimal getAmount() {
        return getProfit();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("deal", deal)
                          .add("profit", profit)
                          .add("action", action)
                          .toString();
    }
}
