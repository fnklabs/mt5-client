package com.fnklabs.mt5.client;

@FunctionalInterface
public interface F<I, O> {
    O apply(I t) throws Exception;
}
