package com.fnklabs.mt5.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

class Mt5Response<T> {
    public static final String OK = "0 Done";
    @JsonProperty("retcode")
    private String code;
    @JsonProperty("answer")
    private T answer;

    public boolean isOk() {
        return StringUtils.equals(OK, getCode());
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public T getAnswer() {
        return answer;
    }

    public void setAnswer(T answer) {
        this.answer = answer;
    }
}
