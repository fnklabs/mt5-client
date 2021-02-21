package com.fnklabs.mt5.client;

public interface Marshaller {
    Command decode(byte[] data);

    byte[] encode(Command command);
}
