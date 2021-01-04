package com.fnklabs.mt5.client;

public class RequestExecutionException extends TradeServerError {
    public RequestExecutionException() {
        super();
    }

    public RequestExecutionException(String message) {
        super(message);
    }

    public RequestExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestExecutionException(Throwable cause) {
        super(cause);
    }

    protected RequestExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
