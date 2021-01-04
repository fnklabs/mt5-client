package com.fnklabs.mt5.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

class DealActionDeserializer extends JsonDeserializer<DealAction> {
    @Override
    public DealAction deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String intValue = p.getText();

        for (DealAction value : DealAction.values()) {
            if (value.getCode() == Integer.parseInt(intValue)) {
                return value;
            }
        }

        return null;
    }
}
