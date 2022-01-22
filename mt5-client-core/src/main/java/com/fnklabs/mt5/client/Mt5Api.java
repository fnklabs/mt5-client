package com.fnklabs.mt5.client;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * API for MT5 server
 */
public interface Mt5Api extends AutoCloseable {
    /**
     * Add user
     *
     * @param user User instance
     *
     * @return User login
     *
     * @throws TradeServerError if can't add user
     */
    String userAdd(Mt5User user) throws TradeServerError;

    /**
     * Get user info and actual equity, margin, free margin, balance
     *
     * @param login User login
     *
     * @return Mt5User instance
     *
     * @throws TradeServerError if can't get user info
     */
    Mt5User info(String login) throws TradeServerError;

    /**
     * Deposit funds on user account by provided login
     *
     * @param login   User login
     * @param amount  Deposit amount. Must be positive
     * @param comment Deposit comment
     *
     * @return order ID
     *
     * @throws TradeServerError on request execute error
     */
    int deposit(String login, BigDecimal amount, @Nullable String comment) throws TradeServerError;


    /**
     * Withdraw funds from user account by provided login
     *
     * @param login   User login
     * @param amount  Withdraw amount
     * @param comment Withdraw comment
     *
     * @return order ID
     *
     * @throws TradeServerError on request execute error
     */
    int withdraw(String login, BigDecimal amount, @Nullable String comment) throws TradeServerError;

    /**
     * Get user trade history (deals)
     *
     * @param login User login
     * @param from  Date from
     * @param to    Date to
     *
     * @return List of deals
     *
     * @throws TradeServerError on request execute error
     */
    List<Deal> getTradeHistory(String login, Date from, Date to) throws TradeServerError;

    /**
     * Execute user command
     *
     * @param name   command name
     * @param params Command parameters
     *
     * @return Deserialized result from response
     *
     * @throws TradeServerError on request execute error
     */
    <T> T cmd(String name, Map<String, String> params) throws TradeServerError;

    /**
     * Create srv rand
     *
     * @param password MT5 Password
     * @param srvRand  srvRand from auth_answer
     *
     * @return srv_rand bytes
     *
     * @throws DecoderException on encoding error
     */
    @VisibleForTesting
    default byte[] createSrvRandAnswer(String password, String srvRand) throws DecoderException {
        byte[] passwordHash = ArrayUtils.addAll(DigestUtils.md5(password.getBytes(StandardCharsets.UTF_16LE)), "WebAPI".getBytes(StandardCharsets.UTF_8));

        return DigestUtils.md5(ArrayUtils.addAll(DigestUtils.md5(passwordHash), Hex.decodeHex(srvRand)));
    }
}
