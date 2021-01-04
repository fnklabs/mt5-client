package com.fnklabs.mt5.client;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;


public class RawApiMarshallerTest {
    private RawApiMarshaller rawApiMarshaller;

    @BeforeEach
    public void setUp() throws Exception {
        rawApiMarshaller = new RawApiMarshaller();
    }

    @Test
    public void decode() {
    }

    @Test
    public void encodeMsg() {
        Command command = new Command("AUTH_ANSWER", ImmutableMap.of(
                "SRV_RAND_ANSWER", "be9a2d07a2191d0fd2279601c59b5d19",
                "CLI_RAND", "e7de570c734df3896d3819b24da67112"
        ));
        command.setMessageId(2);

        byte[] data = rawApiMarshaller.encode(command);

        byte[] expected = ("00d200020A\u0000U\u0000T\u0000H\u0000_\u0000A\u0000N\u0000S\u0000W\u0000E\u0000R\u0000|\u0000S\u0000R\u0000V\u0000_\u0000R\u0000A\u0000N\u0000D\u0000_\u0000A\u0000N\u0000S\u0000W\u0000E\u0000R\u0000=\u0000b\u0000e\u00009\u0000a\u00002\u0000d\u00000\u00007\u0000a\u00002\u00001\u00009\u00001\u0000d\u00000\u0000f\u0000d\u00002\u00002\u00007\u00009\u00006\u00000\u00001\u0000c\u00005\u00009\u0000b\u00005\u0000d\u00001\u00009\u0000|\u0000C\u0000L\u0000I\u0000_\u0000R\u0000A\u0000N\u0000D\u0000=\u0000e\u00007\u0000d\u0000e\u00005\u00007\u00000\u0000c\u00007\u00003\u00004\u0000d\u0000f\u00003\u00008\u00009\u00006\u0000d\u00003\u00008\u00001\u00009\u0000b\u00002\u00004\u0000d\u0000a\u00006\u00007\u00001\u00001\u00002\u0000|\u0000\r" +
                "\u0000\n\u0000").getBytes();

        assertArrayEquals(
                expected,
                data,
                () -> String.format("%s\n != \n%s", Arrays.toString(expected), Arrays.toString(data))
        );
    }

    @Test
    public void encode() {
    }
}