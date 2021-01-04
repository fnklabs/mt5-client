package com.fnklabs.mt5.client;

public class TradeServerError extends RuntimeException {
    public TradeServerError(String msg) {
        super(msg);
    }

    public TradeServerError() {
        super();
    }

    public TradeServerError(String message, Throwable cause) {
        super(message, cause);
    }

    public TradeServerError(Throwable cause) {
        super(cause);
    }

    protected TradeServerError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
