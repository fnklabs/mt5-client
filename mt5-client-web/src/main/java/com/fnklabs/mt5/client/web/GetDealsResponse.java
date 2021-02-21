package com.fnklabs.mt5.client.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fnklabs.mt5.client.Deal;

import java.util.List;

public class GetDealsResponse extends Response {
    @JsonProperty("answer")
    private List<Deal> deals;

    public List<Deal> getDeals() {
        return deals;
    }

    public void setDeals(List<Deal> deals) {
        this.deals = deals;
    }
}
