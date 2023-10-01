package com.fnklabs.mt5.client.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fnklabs.mt5.client.Deal;
import com.fnklabs.mt5.client.DealAction;
import com.fnklabs.mt5.client.Mt5User;
import com.fnklabs.mt5.client.RequestExecutionException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


class WebMt5ApiTest {
    private String address;
    private String username;
    private String password;

    private String group;

    private WebMt5Api webMt5Client;

    @BeforeEach
    public void setUp() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, JsonProcessingException {
        address = System.getenv("MT5_HTTP_ADDRESS");
        username = System.getenv("MT5_USERNAME");
        password = System.getenv("MT5_PASSWORD");
        group = System.getenv("MT5_GROUP");

        CloseableHttpClient httpClient = getHttpClient();

        ObjectMapper objectMapper = getObjectMapper();

        webMt5Client = new WebMt5Api(
                address,
                username,
                password,
                httpClient,
                objectMapper
        );
    }

    @Test
    public void auth() {
        webMt5Client.auth(username, password);
    }

    @Test
    public void create() {
        Mt5User mt5User = getMt5User();
        webMt5Client.userAdd(mt5User);
    }

    @Test
    public void infoForUknownAccount() {
        Assertions.assertThrows(RequestExecutionException.class, () -> {
            Mt5User info = webMt5Client.info("12333123123");
        });
    }

    @Test
    public void info() {
        Mt5User mt5User = getMt5User();

        String login = webMt5Client.userAdd(mt5User);

        Mt5User info = webMt5Client.info(login);

        assertEquals(mt5User.getGroup(), info.getGroup());
        assertEquals(mt5User.getFirstName(), info.getFirstName());
        assertEquals(mt5User.getLastName(), info.getLastName());
        assertEquals(mt5User.getLeverage(), info.getLeverage());
        assertEquals(mt5User.getEmail(), info.getEmail());
        assertEquals(mt5User.getPhone(), info.getPhone());
        assertEquals(mt5User.getCountry(), info.getCountry());
        assertEquals(mt5User.getCity(), info.getCity());
        assertEquals(mt5User.getState(), info.getCity());
        assertEquals(mt5User.getState(), info.getState());
        assertEquals(mt5User.getZip(), info.getZip());
        assertEquals(mt5User.getZipCode(), info.getZipCode());
        assertEquals(mt5User.getAddress(), info.getAddress());
        assertEquals(mt5User.getComment(), info.getComment());

        assertEquals(BigDecimal.ZERO.doubleValue(), info.getBalance().doubleValue());
        assertEquals(BigDecimal.ZERO.doubleValue(), info.getMargin().doubleValue());
        assertEquals(BigDecimal.ZERO.doubleValue(), info.getMarginFree().doubleValue());
        assertEquals(BigDecimal.ZERO.doubleValue(), info.getEquity().doubleValue());

        webMt5Client.deposit(login, BigDecimal.valueOf(100), "test deposit");

        Mt5User infoAfterDeposit = webMt5Client.info(login);

        assertEquals(100, infoAfterDeposit.getBalance().doubleValue());
        assertEquals(0, infoAfterDeposit.getMargin().doubleValue());
        assertEquals(100, infoAfterDeposit.getMarginFree().doubleValue());
        assertEquals(100, infoAfterDeposit.getEquity().doubleValue());

        webMt5Client.withdraw(login, BigDecimal.valueOf(100), "test deposit");

        Mt5User infoAfterWithdraw = webMt5Client.info(login);

        assertEquals(0, infoAfterWithdraw.getBalance().doubleValue());
        assertEquals(0, infoAfterWithdraw.getMargin().doubleValue());
        assertEquals(0, infoAfterWithdraw.getMarginFree().doubleValue());
        assertEquals(0, infoAfterWithdraw.getEquity().doubleValue());
    }

    @Test
    public void tradeHistory() {
        Mt5User mt5User = getMt5User();

        String login = webMt5Client.userAdd(mt5User);

        List<Deal> tradeHistory = webMt5Client.getTradeHistory(login, DateTime.now().minusDays(1).toDate(), DateTime.now().plusDays(1).toDate());

        assertEquals(0, tradeHistory.size());

        webMt5Client.deposit(login, BigDecimal.valueOf(100), "test deposit");


        List<Deal> tradeHistoryAfterDeposit = webMt5Client.getTradeHistory(login, DateTime.now().minusDays(1).toDate(), DateTime.now().plusDays(1).toDate());

        assertEquals(1, tradeHistoryAfterDeposit.size());


        assertEquals(DealAction.DEAL_BALANCE, tradeHistoryAfterDeposit.get(0).getAction());
        assertEquals(100, tradeHistoryAfterDeposit.get(0).getAmount().doubleValue());
    }

    @NotNull
    private Mt5User getMt5User() {
        Mt5User mt5User = new Mt5User();
        mt5User.setGroup(group);
        mt5User.setFirstName("John");
        mt5User.setLastName("Doe");
        mt5User.setLeverage(100);
        mt5User.setPassword("Password1$");
        mt5User.setEmail("johndoe@example.com");
        mt5User.setPhone("70000000000");
        mt5User.setCountry("Russia");
        mt5User.setCity("Moscow");
        mt5User.setState("Moscow");
        mt5User.setZip("11111");
        mt5User.setAddress("Str 12");
        mt5User.setComment("test");
        return mt5User;
    }

    @AfterEach
    void tearDown() throws Exception {
        webMt5Client.close();
    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

    private CloseableHttpClient getHttpClient() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        SSLContext sslContext = SSLContextBuilder
                .create()
                .loadTrustMaterial(new TrustSelfSignedStrategy())
                .build();


        HostnameVerifier allowAllHosts = new NoopHostnameVerifier();

        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);

        HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                                                                                  .setSSLSocketFactory(connectionFactory)
                                                                                  .build();
        return HttpClients.custom()
                          .setConnectionManager(cm)
                          .build();
    }
}