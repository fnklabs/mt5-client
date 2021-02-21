package com.fnklabs.mt5.client.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

public class Response {
    @JsonProperty("retcode")
    private String retCode;

    public boolean isOk() {
        return StringUtils.equalsAny("0 Done", retCode);
    }

    public String getRetCode() {
        return retCode;
    }

    public void setRetCode(String retCode) {
        this.retCode = retCode;
    }
}
