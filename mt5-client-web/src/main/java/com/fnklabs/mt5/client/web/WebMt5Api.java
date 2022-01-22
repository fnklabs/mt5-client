package com.fnklabs.mt5.client.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fnklabs.mt5.client.*;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class WebMt5Api implements Mt5Api {
    private static final Logger log = LoggerFactory.getLogger(WebMt5Api.class);

    private static final int VERSION = 484;
    private static final String AGENT = "fnklabs";
    private static final int HISTORY_GET_PAGE_LIMIT = 100;
    private final String serverAddress;
    private final String login;
    private final String password;
    private final CloseableHttpClient client;
    private final ObjectMapper objectMapper;

    public WebMt5Api(String serverAddress, String login, String password, CloseableHttpClient httpClient, ObjectMapper objectMapper) {
        this.serverAddress = serverAddress;
        this.login = login;
        this.password = password;
        this.client = httpClient;
        this.objectMapper = objectMapper;
    }

    public void auth(String login, String password) {

        try {
            URI authStartUri = new URIBuilder().setHttpHost(HttpHost.create(serverAddress))
                                               .setPath("/auth_start")
                                               .addParameter("version", String.valueOf(VERSION))
                                               .setParameter("agent", AGENT)
                                               .addParameter("login", login)
                                               .addParameter("type", "manager")
                                               .build();

            AuthStartResponse authStartResponse = executeRequest(new HttpGet(authStartUri), new TypeReference<AuthStartResponse>() {});

            byte[] srvRandAnswer = createSrvRandAnswer(password, authStartResponse.getSrvRand());

            byte[] clientRandAnswer = DigestUtils.md5(DigestUtils.md5(UUID.randomUUID().toString()));

            URI answerUri = new URIBuilder().setHttpHost(HttpHost.create(serverAddress))
                                            .setPath("/auth_answer")
                                            .addParameter("srv_rand_answer", Hex.encodeHexString(srvRandAnswer))
                                            .addParameter("cli_rand", Hex.encodeHexString(clientRandAnswer))
                                            .build();

            HttpGet authAnswerRequest = new HttpGet(answerUri);

            AuthAnswerResponse authAnswerResponse = executeRequest(authAnswerRequest, new TypeReference<AuthAnswerResponse>() {});


        } catch (Exception e) {
            throw new TradeServerError(e);
        }
    }

    @Override
    public void close() throws Exception {
        client.close();
    }

    @Override
    public String userAdd(Mt5User user) throws TradeServerError {
        try {
            HttpPost request = new HttpPost(new URIBuilder().setHttpHost(HttpHost.create(serverAddress))
                                                            .setPath("/user_add")
                                                            .addParameter("pass_main", user.getPassword())
                                                            .addParameter("pass_investor", user.getPassword())
                                                            .addParameter("group", user.getGroup())
                                                            .addParameter("name", user.getFirstName())
                                                            .addParameter("leverage", String.valueOf(user.getLeverage()))
                                                            .build());
            request.setEntity(new StringEntity(objectMapper.writeValueAsString(user), ContentType.APPLICATION_JSON));

            AddUserResponse response = authAndExecuteRequest(request, new TypeReference<AddUserResponse>() {});

            return response.getAnswer().getLogin();
        } catch (Exception e) {
            throw new RequestExecutionException(e);
        }
    }

    @Override
    public Mt5User info(String login) throws TradeServerError {
        try {
            HttpGet userGetRequest = new HttpGet(new URIBuilder().setHttpHost(HttpHost.create(serverAddress))
                                                                 .setPath("/user_get")
                                                                 .addParameter("login", login)
                                                                 .build());

            UserGetResponse userGetResponse = authAndExecuteRequest(userGetRequest, new TypeReference<UserGetResponse>() {});

            Mt5User mt5User = userGetResponse.getUser();

            HttpGet userAccountGetRequest = new HttpGet(new URIBuilder().setHttpHost(HttpHost.create(serverAddress))
                                                                        .setPath("/user_account_get")
                                                                        .addParameter("login", login)
                                                                        .build());

            UserGetResponse userAccountGetResponse = authAndExecuteRequest(userAccountGetRequest, new TypeReference<UserGetResponse>() {});

            mt5User.setBalance(userAccountGetResponse.getUser().getBalance());
            mt5User.setMargin(userAccountGetResponse.getUser().getMargin());
            mt5User.setMarginFree(userAccountGetResponse.getUser().getMarginFree());
            mt5User.setEquity(userAccountGetResponse.getUser().getEquity());

            return mt5User;
        } catch (Exception e) {
            throw new RequestExecutionException(e);
        }
    }

    @Override
    public int deposit(String login, BigDecimal amount, @Nullable String comment) throws TradeServerError {

        return balance(login, amount.abs(), comment);
    }

    @Override
    public int withdraw(String login, BigDecimal amount, @Nullable String comment) throws TradeServerError {
        return balance(login, amount.abs().negate(), comment);
    }

    @Override
    public List<Deal> getTradeHistory(String login, Date from, Date to) {
        long totalDeals = getTradeHistoryTotal(login, from, to);

        List<Deal> orders = new ArrayList<>();

        for (int offset = 0; offset < totalDeals; offset += HISTORY_GET_PAGE_LIMIT) {
            try {
                HttpGet httpGet = new HttpGet(new URIBuilder().setHttpHost(HttpHost.create(serverAddress))
                                                              .setPath("/deal_get_page")
                                                              .addParameter("login", login)
                                                              .addParameter("from", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(from.getTime())))
                                                              .addParameter("to", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(to.getTime())))
                                                              .addParameter("offset", String.valueOf(offset))
                                                              .addParameter("total", String.valueOf(HISTORY_GET_PAGE_LIMIT))
                                                              .build());

                GetDealsResponse dealsResponse = authAndExecuteRequest(httpGet, new TypeReference<GetDealsResponse>() {});

                orders.addAll(dealsResponse.getDeals());

            } catch (Exception e) {
                log.warn("can't get trade history for: {}", login, e);

                throw new TradeServerError(String.format("Can't get trade history for %s", login), e);
            }

        }

        return orders;
    }

    @Override
    public <T> T cmd(String name, Map<String, String> params) throws TradeServerError {


        try {
            HttpGet request = new HttpGet(new URIBuilder().setHttpHost(HttpHost.create(serverAddress))
                                                          .setPath(String.format("/%s", name))
                                                          .addParameters(
                                                                  params.entrySet()
                                                                        .stream()
                                                                        .map(e -> new BasicNameValuePair(e.getKey(), e.getValue()))
                                                                        .collect(Collectors.toList())
                                                          )
                                                          .build());

            CmdResponse<T> response = authAndExecuteRequest(request, new TypeReference<CmdResponse<T>>() {});

            return response.getAnswer();

        } catch (Exception e) {
            throw new RequestExecutionException(e);
        }
    }

    protected long getTradeHistoryTotal(String login, Date from, Date to) {
        try {
            HttpGet request = new HttpGet(new URIBuilder().setHttpHost(HttpHost.create(serverAddress))
                                                          .setPath("/deal_get_total")
                                                          .addParameter("login", login)
                                                          .addParameter("from", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(from.getTime())))
                                                          .addParameter("to", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(to.getTime())))
                                                          .build());

            GetDealsCountResponse response = authAndExecuteRequest(request, new TypeReference<GetDealsCountResponse>() {});

            return response.getTotalDeals().getTotal();

        } catch (Exception e) {
            throw new RequestExecutionException(e);
        }
    }

    protected int balance(String login, BigDecimal amount, @Nullable String comment) throws TradeServerError {
        try {
            URI uri = new URIBuilder().setHttpHost(HttpHost.create(serverAddress))
                                      .setPath("/trade_balance")
                                      .setParameter("login", login)
                                      .setParameter("type", String.valueOf(DealAction.DEAL_BALANCE.getCode()))
                                      .setParameter("balance", amount.toString())
                                      .setParameter("comment", comment)
                                      .setParameter("check_margin", "1")
                                      .build();

            HttpGet request = new HttpGet(uri);

            BalanceResponse response = authAndExecuteRequest(request, new TypeReference<BalanceResponse>() {});

            return Integer.parseInt(response.getAnswer().getId());

        } catch (Exception e) {
            throw new RequestExecutionException(e);
        }
    }

    private <T extends Response> T authAndExecuteRequest(ClassicHttpRequest request, TypeReference<T> typeReference) {
        try {
            URI authStartUri = new URIBuilder().setHttpHost(HttpHost.create(serverAddress))
                                               .setPath("/test_access")
                                               .build();

            try {
                Response authStartResponse = executeRequest(new HttpGet(authStartUri), new TypeReference<Response>() {});
            } catch (RequestExecutionException e) {
                auth(login, password);
            }

            return executeRequest(request, typeReference);
        } catch (Exception e) {
            throw new TradeServerError(e);
        }
    }

    private <T extends Response> T executeRequest(ClassicHttpRequest request, TypeReference<T> clazz) {
        log.debug("request: {}", request);

        try (CloseableHttpResponse httpResponse = client.execute(request)) {
            if (httpResponse.getCode() != HttpStatus.SC_OK) {
                throw new RequestExecutionException(IOUtils.toString(httpResponse.getEntity().getContent()));
            }

            String content = IOUtils.toString(httpResponse.getEntity().getContent());

            log.debug("response data: {}", content);

            T resp = objectMapper.readValue(content, clazz);

            if (!resp.isOk()) {
                throw new RequestExecutionException("retrieve error");
            }

            return resp;
        } catch (Exception e) {
            throw new RequestExecutionException(e);
        }
    }
}
