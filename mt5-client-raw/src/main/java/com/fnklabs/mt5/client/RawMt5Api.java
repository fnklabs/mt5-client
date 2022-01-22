package com.fnklabs.mt5.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.HostAndPort;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RawMt5Api implements Mt5Api {

    public static final int HISTORY_GET_PAGE_LIMIT = 100;

    private static final Logger log = LoggerFactory.getLogger(RawMt5Api.class);
    private static final String AGENT = "fnklabs";
    private static final int VERSION = 2560;
    private final HostAndPort address;
    private final String username;
    private final String password;
    private final GenericObjectPool<Session> sessionPool;
    private final int color;
    private final Marshaller marshaller = new RawApiMarshaller();
    private final ObjectMapper objectMapper;

    public RawMt5Api(String address, String username, String password, int color, ObjectMapper objectMapper) {
        this.address = HostAndPort.fromString(address);
        this.username = username;
        this.password = password;
        this.color = color;
        this.objectMapper = objectMapper;

        GenericObjectPoolConfig<Session> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(6);
        poolConfig.setMaxIdle(2);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTestOnBorrow(true);

        sessionPool = new GenericObjectPool<>(new BasePooledObjectFactory<Session>() {
            @Override
            public Session create() throws Exception {

                log.debug("create session: {}", RawMt5Api.this.address);
                Session session = new Session(RawMt5Api.this.address.getHost(), RawMt5Api.this.address.getPort());

                authenticate(session);

                return session;
            }

            @Override
            public PooledObject<Session> wrap(Session obj) {
                return new DefaultPooledObject<>(obj);
            }
        }, poolConfig);
    }

    @Override
    public void close() throws Exception {}

    @Override
    public String userAdd(Mt5User tradeAccount) throws TradeServerError {
        Map<String, String> params = ImmutableMap.<String, String>builder()
                                                 .put("login", "0")
                                                 .put("pass_main", tradeAccount.getPassword())
                                                 .put("pass_investor", tradeAccount.getPassword())
                                                 .put("pass_phone", "")
                                                 .put("right", "483")
                                                 .put("group", tradeAccount.getGroup())
                                                 .put("name", tradeAccount.getFullname())
                                                 .put("country", Optional.ofNullable(tradeAccount.getCountry()).orElse(StringUtils.EMPTY))
                                                 .put("city", Optional.ofNullable(tradeAccount.getCity()).orElse(StringUtils.EMPTY))
                                                 .put("state", Optional.ofNullable(tradeAccount.getState()).orElse(StringUtils.EMPTY))
                                                 .put("zipcode", Optional.ofNullable(tradeAccount.getZipCode()).orElse(StringUtils.EMPTY))
                                                 .put("address", Optional.ofNullable(tradeAccount.getAddress()).orElse(StringUtils.EMPTY))
                                                 .put("id", "")
                                                 .put("status", "")
                                                 .put("phone", Optional.ofNullable(tradeAccount.getPhone()).orElse(StringUtils.EMPTY))
                                                 .put("email", Optional.ofNullable(tradeAccount.getEmail()).orElse(StringUtils.EMPTY))
                                                 .put("comment", Optional.ofNullable(tradeAccount.getComment()).orElse(StringUtils.EMPTY))
                                                 .put("color", String.valueOf(color))
                                                 .put("leverage", String.valueOf(tradeAccount.getLeverage()))
                                                 .put("agent", "0")
                                                 .put("balance", "0")
                                                 .build();

        return evaluate(session -> {
            byte[] responseData = execute("user_add", params);

            Command cmd = marshaller.decode(responseData);

            if (!cmd.isOk()) {
                throw new TradeServerError(String.format("Can't execute request: `%s`", cmd));
            }

            return cmd.getParam("LOGIN");
        });
    }

    @Override
    public Mt5User info(String login) throws TradeServerError {
        Mt5User user = evaluate(session -> {
            Command userGetRequest = new Command("user_get", ImmutableMap.of("login", login));
            userGetRequest.setMessageId(session.nextId());

            session.write(marshaller.encode(userGetRequest));

            Command response = marshaller.decode(session.read());

            if (!response.isOk()) {
                throw new TradeServerError(String.format("Can't get account info %s", login));
            }

            return objectMapper.readValue(response.getAdditionalData(), Mt5User.class);
        });

        Mt5User accountInfo = evaluate(session -> {
            Command userGetRequest = new Command("user_account_get", ImmutableMap.of("login", login));
            userGetRequest.setMessageId(session.nextId());

            session.write(marshaller.encode(userGetRequest));

            Command userDataResponse = marshaller.decode(session.read());

            if (!userDataResponse.isOk()) {
                throw new TradeServerError(String.format("Can't get account info %s", login));
            }

            return objectMapper.readValue(userDataResponse.getAdditionalData(), Mt5User.class);
        });

        user.setMargin(accountInfo.getMargin());
        user.setMarginFree(accountInfo.getMarginFree());
        user.setEquity(accountInfo.getEquity());
        user.setBalance(accountInfo.getBalance());

        return user;
    }

    @Override
    public int deposit(String login, BigDecimal amount, String comment) throws TradeServerError {
        return balance(login, amount.abs(), comment);
    }

    @Override
    public int withdraw(String login, BigDecimal amount, String comment) throws TradeServerError {
        return balance(login, amount.abs().negate(), comment);
    }

    /**
     * Get closed orders for trade account for provided login in date range
     *
     * @param login {@link Mt5User#getLogin()}
     * @param from  Date from
     * @param to    Date to
     *
     * @return Total closed orders in range for trade account
     */
    @Override
    public List<Deal> getTradeHistory(String login, Date from, Date to) {
        int tradeHistoryCount = getTradeHistoryCount(login, from, to);

        if (tradeHistoryCount == 0) {
            log.debug("trade history for {} is empty in range {}-{}", login, from, to);

            return Collections.emptyList();
        }


        return evaluate(session -> {
            List<Deal> orders = new ArrayList<>();

            for (int offset = 0; offset < tradeHistoryCount; offset += HISTORY_GET_PAGE_LIMIT) {

                try {

                    Command command = new Command("DEAL_GET_PAGE", ImmutableMap.of(
                            "login", login,
                            "from", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(from.getTime())),
                            "to", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(to.getTime())),
                            "offset", String.valueOf(offset),
                            "total", String.valueOf(HISTORY_GET_PAGE_LIMIT)
                    ));
                    command.setMessageId(session.nextId());

                    byte[] requestPacket = marshaller.encode(command);

                    session.write(requestPacket);

                    byte[] responseData = session.read();

                    Command cmdResponse = marshaller.decode(responseData);

                    if (!cmdResponse.isOk()) {
                        throw new TradeServerError("can't execute correct operation");
                    }

                    String data = cmdResponse.getAdditionalData();

                    if (StringUtils.isNotEmpty(data)) {
                        try {
                            List<Deal> dealList = objectMapper.readValue(data, new TypeReference<List<Deal>>() {});

                            log.debug("deal list: {}", dealList);

                            orders.addAll(dealList);
                        } catch (IOException e) {
                            log.warn("can't deserialize data", e);

                            throw new TradeServerError(String.format("Can't get trade history for %s", login), e);
                        }
                    }

                } catch (RequestExecutionException e) {
                    log.warn("can't get trade history for: {}", login, e);

                    throw new TradeServerError(String.format("Can't get trade history for %s", login), e);
                }
            }

            return orders;

        });
    }

    @Override
    public <T> T cmd(String name, Map<String, String> params) throws TradeServerError {
        byte[] bytes = execute(name, params);
        return null;
    }

    /**
     * Send MT5 command and read response data
     *
     * @param command Mt5 command
     * @param params  Command parameters
     *
     * @return Packet data without header
     */
    protected byte[] execute(String command, Map<String, String> params) {
        return evaluate(session -> {
            Command request = new Command(command, params);
            request.setMessageId(session.nextId());

            byte[] packet = marshaller.encode(request);

            session.write(packet);

            return session.read();
        });
    }


    /**
     * Get closed orders count for trade account for provided login in date range
     *
     * @param login {@link Mt5User#getLogin()}
     * @param from  Date from
     * @param to    Date to
     *
     * @return Total closed orders in range for trade account
     */
    protected int getTradeHistoryCount(String login, Date from, Date to) {
        return evaluate(session -> {
            Command requestCmd = new Command("DEAL_GET_TOTAL", ImmutableMap.of(
                    "login", login,
                    "from", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(from.getTime())),
                    "to", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(to.getTime()))
            ));
            requestCmd.setMessageId(session.nextId());

            session.write(marshaller.encode(requestCmd));

            byte[] responseData = session.read();

            Command cmdResponse = marshaller.decode(responseData);

            if (!cmdResponse.isOk()) {
                throw new TradeServerError("can't execute trade history count operation");
            }

            return Integer.valueOf(cmdResponse.getParam("TOTAL"));
        });
    }

    @VisibleForTesting
    void authenticate(Session session) throws TradeServerError {
        try {

            Command authStartCmd = new Command("AUTH_START", ImmutableMap.of(
                    "VERSION", String.valueOf(VERSION),
                    "AGENT", AGENT,
                    "LOGIN", username,
                    "TYPE", "manager",
                    "CRYPT_METHOD", "NONE"
            ));
            authStartCmd.setMessageId(session.nextId());

            session.write(marshaller.encode(authStartCmd));

            byte[] authStartResponse = session.read();

            Command authStartResponseCommand = marshaller.decode(authStartResponse);

            if (!authStartResponseCommand.isOk()) {
                throw new TradeServerError(String.format("Can't auth %s", authStartResponseCommand.getParam(Command.PARAM_RET_CODE)));
            }

            AuthResponse authResponse = new AuthResponse();
            authResponse.setSrvRand(authStartResponseCommand.getParam("SRV_RAND"));
            authResponse.setCode(authStartResponseCommand.getParam("RETCODE"));

            log.info("auth_start response: {}", authResponse);

            byte[] srvRandAnswer = createSrvRandAnswer(password, authResponse.getSrvRand());

            Command authAnswerCmd = new Command("auth_answer", ImmutableMap.of(
                    "SRV_RAND_ANSWER", Hex.encodeHexString(srvRandAnswer),
                    "CLI_RAND", Hex.encodeHexString(DigestUtils.md5(UUID.randomUUID().toString().replace("-", "")))
            ));

            authAnswerCmd.setMessageId(session.nextId());

            session.write(marshaller.encode(authAnswerCmd));

            byte[] authAnswerResponse = session.read();

            Command authAnswerResponseCmd = marshaller.decode(authAnswerResponse);

            if (!authAnswerResponseCmd.isOk()) {
                throw new TradeServerError("can't auth");
            }

            AuthAnswerResponse auth = new AuthAnswerResponse();
            auth.setCliRandAnswer(authAnswerResponseCmd.getParam(Command.PARAM_CLI_RAND_ANSWER));
            log.info("auth answer response: {}", auth);

        } catch (RequestExecutionException e) {
            log.warn("can't auth", e);

            throw new TradeServerError(e);
        } catch (DecoderException e) {
            log.warn("can't read srv_rand from response", e);

            throw new TradeServerError(e);
        }

    }

    /**
     * Execute balance operation
     *
     * @param login   TradeAccount login
     * @param amount  Balance amount (positive to deposit, negative to withdraw)
     * @param comment Balance operation comment
     *
     * @return Ticket id
     */
    @VisibleForTesting
    protected int balance(String login, BigDecimal amount, String comment) {
        return evaluate(session -> {
            ImmutableMap<String, String> params = ImmutableMap.of("login", login,
                                                                  "type", "2",
                                                                  "balance", amount.toString(),
                                                                  "check_margin", "1",
                                                                  "comment", comment
            );


            Command requestCommand = new Command("trade_balance", params);
            requestCommand.setMessageId(session.nextId());

            session.write(marshaller.encode(requestCommand));

            byte[] responseData = session.read();

            Command cmdResponse = marshaller.decode(responseData);

            if (!cmdResponse.isOk()) {
                throw new TradeServerError(String.format("Can't process balance request for %s. %s", login, cmdResponse));
            }

            return Integer.parseInt(cmdResponse.getParam("TICKET"));
        });

    }

    private <T> T evaluate(F<Session, T> userFunc) throws TradeServerError {
        Session session = getSession();

        try {
            return userFunc.apply(session);
        } catch (TradeServerError e) {
            throw e;
        } catch (Exception e) {
            throw new TradeServerError(e);
        } finally {
            try {
                sessionPool.invalidateObject(session); // close session after each command execution because retrieve errors
            } catch (Exception e) {
                log.warn("Can't invalidate session {} from pool", session, e);
            }
        }
    }

    /**
     * Get session to TradeServer from pool
     *
     * @return Session
     *
     * @throws TradeServerError if can't get session from pool
     */
    private Session getSession() throws TradeServerError {
        try {
            return sessionPool.borrowObject();
        } catch (Exception e) {
            throw new TradeServerError(e);
        }
    }


}
