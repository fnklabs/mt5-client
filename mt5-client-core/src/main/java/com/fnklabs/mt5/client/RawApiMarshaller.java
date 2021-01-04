package com.fnklabs.mt5.client;

import com.google.common.base.Splitter;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RawApiMarshaller implements Marshaller {
    public static final String SEPARATOR = "|";
    public static final int HEADER_SIZE = 9;
    private static final Charset MSG_BODY_CHARSET = StandardCharsets.UTF_16LE;
    private static final Splitter SPLITTER = Splitter.on(SEPARATOR);
    private static final Logger log = LoggerFactory.getLogger(RawApiMarshaller.class);
    private static final Splitter PARAMETER_VALUE_SPLITTER = Splitter.on("=");

    @Override
    public Command decode(byte[] msgData) {
        String response = new String(msgData, StandardCharsets.UTF_16LE);

        log.debug("decoded data: {}", response);

        List<String> responseParts = Splitter.on("\r\n")
                                             .limit(2)
                                             .splitToList(response);

        log.debug("response part: {}", responseParts);

        if (responseParts.isEmpty()) {
            throw new TradeServerError(String.format("Invalid response format %s", response));
        }

        List<String> parts = SPLITTER.splitToList(responseParts.get(0));

        String command = parts.get(0);

        Map<String, String> params = parts.subList(1, parts.size())
                                          .stream()
                                          .filter(v -> StringUtils.isNotEmpty(StringUtils.trim(v)))
                                          .map(line -> PARAMETER_VALUE_SPLITTER.splitToList(StringUtils.trim(line)))
                                          .filter(v -> v.size() == 2)
                                          .collect(Collectors.toMap(
                                                  k -> StringUtils.trim(k.get(0)),
                                                  k -> StringUtils.trim(k.get(1)),
                                                  (a, b) -> a
                                          ));

        Command cmd = new Command(command, params);

        if (responseParts.size() == 2) {
            cmd.setAdditionalData(responseParts.get(1));
        }

        return cmd;
    }

    @Override
    public byte[] encode(Command command) {
        StringBuilder requestBuilder = new StringBuilder(StringUtils.upperCase(command.getCommand())).append(SEPARATOR);

        command.getParams()
               .forEach((name, value) -> {
                   requestBuilder.append(StringUtils.upperCase(name))
                                 .append("=")
                                 .append(value)
                                 .append(SEPARATOR);
               });

        requestBuilder.append("\r\n");

        String request = requestBuilder.toString();

        String requestUtf16le = new String(request.getBytes(MSG_BODY_CHARSET), MSG_BODY_CHARSET);

        int msgSize = requestUtf16le.getBytes(MSG_BODY_CHARSET).length;

        log.debug("Request body: {} length {}", requestUtf16le, msgSize);


        String headerFormat = StringUtils.equalsIgnoreCase("AUTH_START", command.getCommand()) ? "MT5WEBAPI%04x%04x0" : "%04x%04x0";
        String msgHeader = String.format(headerFormat, msgSize, command.getMessageId());

        byte[] data = ArrayUtils.addAll(msgHeader.getBytes(StandardCharsets.UTF_8), requestUtf16le.getBytes(MSG_BODY_CHARSET));

        log.info(
                "Request: `{}{}` Hex encoded request: `{}`",
                msgHeader,
                request,
                Hex.encodeHexString(data)
        );

        return data;
    }
}
