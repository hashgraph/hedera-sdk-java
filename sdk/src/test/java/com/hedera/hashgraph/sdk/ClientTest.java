package com.hedera.hashgraph.sdk;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientTest {
    @Test
    @DisplayName("Can construct mainnet client")
    void forMainnet() throws TimeoutException {
        Client.forMainnet().close();
    }


    @Test
    @DisplayName("Client.setMaxQueryPayment() negative")
    void setMaxQueryPaymentNegative() throws TimeoutException {
        var client = Client.forTestnet();
        assertThrows(IllegalArgumentException.class, () -> {
            client.setMaxQueryPayment(Hbar.MIN);
        });
        client.close();
    }

    @ValueSource(ints = {-1, 0})
    @ParameterizedTest(name = "Invalid maxAttempts {0}")
    void setMaxAttempts(int maxAttempts) throws TimeoutException {
        var client = Client.forNetwork(Map.of());
        assertThrows(IllegalArgumentException.class, () -> {
            client.setMaxAttempts(maxAttempts);
        });
        client.close();
    }

    @NullSource
    @ValueSource(longs = {-1, 0, 249})
    @ParameterizedTest(name = "Invalid maxBackoff {0}")
    @SuppressWarnings("NullAway")
    void setMaxBackoffInvalid(@Nullable Long maxBackoffMillis) throws TimeoutException {
        @Nullable Duration maxBackoff = maxBackoffMillis != null ? Duration.ofMillis(maxBackoffMillis) : null;
        var client = Client.forNetwork(Map.of());
        assertThrows(IllegalArgumentException.class, () -> {
            client.setMaxBackoff(maxBackoff);
        });
        client.close();
    }

    @ValueSource(longs = {250, 8000})
    @ParameterizedTest(name = "Valid maxBackoff {0}")
    void setMaxBackoffValid(long maxBackoff) throws TimeoutException {
        Client.forNetwork(Map.of()).setMaxBackoff(Duration.ofMillis(maxBackoff)).close();
    }

    @NullSource
    @ValueSource(longs = {-1, 8001})
    @ParameterizedTest(name = "Invalid minBackoff {0}")
    @SuppressWarnings("NullAway")
    void setMinBackoffInvalid(@Nullable Long minBackoffMillis) throws TimeoutException {
        @Nullable Duration minBackoff = minBackoffMillis != null ? Duration.ofMillis(minBackoffMillis) : null;
        var client = Client.forNetwork(Map.of());
        assertThrows(IllegalArgumentException.class, () -> {
            client.setMinBackoff(minBackoff);
        });
        client.close();
    }

    @ValueSource(longs = {0, 250, 8000})
    @ParameterizedTest(name = "Valid minBackoff {0}")
    void setMinBackoffValid(long minBackoff) throws TimeoutException {
        Client.forNetwork(Map.of()).setMinBackoff(Duration.ofMillis(minBackoff)).close();
    }

    @Test
    @DisplayName("Client.setMaxTransactionFee() negative")
    void setMaxTransactionFeeNegative() throws TimeoutException {
        var client = Client.forTestnet();
        assertThrows(IllegalArgumentException.class, () -> {
            client.setDefaultMaxTransactionFee(Hbar.MIN);
        });
        client.close();
    }

    @Test
    @DisplayName("fromJsonFile() functions correctly")
    void fromJsonFile() throws Exception {
        Client.fromConfigFile(new File("./src/test/resources/client-config.json")).close();
        Client.fromConfigFile(new File("./src/test/resources/client-config-with-operator.json")).close();
        Client.fromConfigFile("./src/test/resources/client-config.json").close();
        Client.fromConfigFile("./src/test/resources/client-config-with-operator.json").close();
    }

    @Test
    @DisplayName("fromJson() functions correctly")
    void testFromJson() throws Exception {
        // Copied content of `client-config-with-operator.json`
        Client.fromConfig("{\n" +
            "    \"network\":\"mainnet\",\n" +
            "    \"operator\": {\n" +
            "        \"accountId\": \"0.0.36\",\n" +
            "        \"privateKey\": \"302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10\"\n" +
            "    }\n" +
            "}\n");

        // put it in a file for nicer formatting
        InputStream clientConfig = ClientTest.class.getClassLoader()
            .getResourceAsStream("client-config.json");

        Assertions.assertNotNull(clientConfig);

        Client.fromConfig(new InputStreamReader(clientConfig, StandardCharsets.UTF_8)).close();

        // put it in a file for nicer formatting
        InputStream clientConfigWithOperator = ClientTest.class.getClassLoader()
            .getResourceAsStream("client-config-with-operator.json");

        Assertions.assertNotNull(clientConfigWithOperator);
    }

    @Test
    @DisplayName("setNetwork() functions correctly")
    void testReplaceNodes() throws Exception {
        Map<String, AccountId> nodes = new HashMap<>();
        nodes.put("0.testnet.hedera.com:50211", new AccountId(3));
        nodes.put("1.testnet.hedera.com:50211", new AccountId(4));

        Client client = Client.forNetwork(nodes);

        Map<String, AccountId> setNetworkNodes = new HashMap<>();
        setNetworkNodes.put("2.testnet.hedera.com:50211", new AccountId(5));
        setNetworkNodes.put("3.testnet.hedera.com:50211", new AccountId(6));

        client.setNetwork(setNetworkNodes);

        var network = client.getNetwork();
        Assertions.assertEquals(2, network.size());
        Assertions.assertEquals(network.get("2.testnet.hedera.com:50211"), new AccountId(5));
        Assertions.assertEquals(network.get("3.testnet.hedera.com:50211"), new AccountId(6));
        client.close();
    }
}
