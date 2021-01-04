package com.fnklabs.mt5.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HostAndPort;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class Mt5ClientTest {

    private String address = System.getenv("MT5_ADDRESS");
    private String username = System.getenv("MT5_USERNAME");
    private String password = System.getenv("MT5_PASSWORD");
    private String group = System.getenv("MT5_GROUP");

    private Mt5Client mt5Client;

    @Mock
    private Mt5User tradeAccount;


    @BeforeEach
    public void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        mt5Client = new Mt5Client(address, username, password, 0, objectMapper);

        doReturn(group).when(tradeAccount).getGroup();
        doReturn("John").when(tradeAccount).getFullname();
        doReturn(new DateTime()).when(tradeAccount).registrationDate();
        doReturn(100).when(tradeAccount).getLeverage();
        doReturn("Password1").when(tradeAccount).getPassword();
        doReturn("Russia").when(tradeAccount).getCountry();
        doReturn("Moscow").when(tradeAccount).getCity();
        doReturn("Moscow").when(tradeAccount).getState();
        doReturn("11111").when(tradeAccount).getZipCode();
        doReturn("Str 12").when(tradeAccount).getAddress();
        doReturn("70000000000").when(tradeAccount).getPhone();
        doReturn("johndoe@example.com").when(tradeAccount).getEmail();
        doReturn("test").when(tradeAccount).getComment();

    }


    @Test
    public void authenticate() throws Exception {
        HostAndPort hostAndPort = HostAndPort.fromString(address);

        Session session = new Session(hostAndPort.getHost(), hostAndPort.getPort());

        mt5Client.authenticate(session);
    }

    @Test
    public void createSrvRandAnswer() throws DecoderException {

        Assertions.assertAll(
                () -> {
                    byte[] passwords = mt5Client.createSrvRandAnswer("Password1", "73007dc7184747ce0f7c98516ef1c851");

                    assertEquals("77fe51827f7fa69dd80fbec9aa33f1bb", Hex.encodeHexString(passwords));
                }
        );
    }

    @Test
    public void getTradeHistory() {
        String login = mt5Client.create(tradeAccount);

        mt5Client.deposit(login, BigDecimal.TEN, "Test Deposit");

        List<Deal> tradeHistory = mt5Client.getTradeHistory(login, new Date(0), DateUtils.addDays(new Date(), 1));

        assertEquals(1, tradeHistory.size());
        assertEquals(BigDecimal.TEN.doubleValue(), tradeHistory.get(0).getAmount().doubleValue(), 0);
        assertTrue(tradeHistory.get(0).getAction().isBalanceOperation());
        assertFalse(tradeHistory.get(0).getAction().isTradeOperation());
    }

    @Test
    public void create() {
        String login = mt5Client.create(tradeAccount);

        assertNotNull(login);
        assertNotEquals("0", login);
        assertNotEquals("", login);
    }

    @Test
    public void balance() {
        String login = mt5Client.create(tradeAccount);

        mt5Client.balance(login, BigDecimal.valueOf(100), "Order #1");

        Mt5User tradeAccount = mt5Client.info(login);

        assertEquals(BigDecimal.valueOf(100).doubleValue(), tradeAccount.getEquity().doubleValue(), 0);
        assertEquals(BigDecimal.valueOf(100).doubleValue(), tradeAccount.getFreeMargin().doubleValue(), 0);
        assertEquals(BigDecimal.valueOf(100).doubleValue(), tradeAccount.getBalance().doubleValue(), 0);
    }

    @Test
    public void deposit() {
        String login = mt5Client.create(tradeAccount);

        mt5Client.deposit(login, BigDecimal.valueOf(100), "Order #1");

        Mt5User tradeAccount = mt5Client.info(login);

        assertEquals(BigDecimal.valueOf(100).doubleValue(), tradeAccount.getEquity().doubleValue(), 0);
        assertEquals(BigDecimal.valueOf(100).doubleValue(), tradeAccount.getFreeMargin().doubleValue(), 0);
        assertEquals(BigDecimal.valueOf(100).doubleValue(), tradeAccount.getBalance().doubleValue(), 0);
    }

    @Test
    public void withdraw() {
        String login = mt5Client.create(tradeAccount);

        mt5Client.deposit(login, BigDecimal.valueOf(100), "Order #1");
        mt5Client.withdraw(login, BigDecimal.valueOf(50), "Order #1");

        Mt5User tradeAccount = mt5Client.info(login);

        assertEquals(BigDecimal.valueOf(50).doubleValue(), tradeAccount.getEquity().doubleValue(), 0);
        assertEquals(BigDecimal.valueOf(50).doubleValue(), tradeAccount.getFreeMargin().doubleValue(), 0);
        assertEquals(BigDecimal.valueOf(50).doubleValue(), tradeAccount.getBalance().doubleValue(), 0);
    }

    @Test
    public void userAdd() throws Exception {
        int login = mt5Client.userAdd(tradeAccount);

        assertNotEquals(0, login);
    }

    @Test
    public void getInfo() {

        int login = mt5Client.userAdd(tradeAccount);

        for (int i = 0; i < Integer.MAX_VALUE; i++) {

            assertDoesNotThrow(() -> {
                Mt5User info = mt5Client.info(String.valueOf(login));

                assertEquals(String.valueOf(login), info.getLogin());
                assertEquals(tradeAccount.getGroup(), info.getGroup());
                assertEquals(tradeAccount.getLeverage(), info.getLeverage());
                assertEquals(tradeAccount.getEmail(), info.getEmail());
                assertEquals(tradeAccount.getFullname(), info.getFullname());
                assertEquals(tradeAccount.getCountry(), info.getCountry());
                assertEquals(tradeAccount.getState(), info.getState());
                assertEquals(tradeAccount.getCity(), info.getCity());
                assertEquals(tradeAccount.getZipCode(), info.getZipCode());
            }, String.format("Failed on %d attempt", i));

        }
    }

    @AfterEach
    public void tearDown() throws Exception {
    }
}