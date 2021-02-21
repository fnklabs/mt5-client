package com.fnklabs.mt5.client.web;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetDealsCountResponse extends Response {
    @JsonProperty("answer")
    private TotalDeals totalDeals;

    public TotalDeals getTotalDeals() {
        return totalDeals;
    }

    public void setTotalDeals(TotalDeals totalDeals) {
        this.totalDeals = totalDeals;
    }

    public static class TotalDeals {
        private long total;

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }
    }
}
