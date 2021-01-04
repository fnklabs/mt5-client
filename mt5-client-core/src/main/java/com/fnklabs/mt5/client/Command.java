package com.fnklabs.mt5.client;

import com.google.common.base.MoreObjects;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

class Command {
    public static final String PARAM_RET_CODE = "RETCODE";
    public static final String PARAM_CLI_RAND_ANSWER = "CLI_RAND_ANSWER";

    public static final String RET_CODE_OK = "0 Done";

    private final String command;
    private final Map<String, String> params;
    private int messageId;

    private String additionalData;

    public Command(String command, Map<String, String> params) {
        this.command = command;
        this.params = params;
    }

    public String getCommand() {
        return command;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getParam(String key) {
        return params.get(key);
    }

    public boolean isOk() {
        return StringUtils.equalsIgnoreCase(RET_CODE_OK, getParam(PARAM_RET_CODE));
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("command", command)
                          .add("params", params)
                          .add("messageId", messageId)
                          .add("additionalData", additionalData)
                          .toString();
    }
}
