package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    void setNetworkWorks() throws Exception {
        var defaultNetwork = new HashMap<String, AccountId>();
        defaultNetwork.put("0.testnet.hedera.com:50211", new AccountId(3));
        defaultNetwork.put("1.testnet.hedera.com:50211", new AccountId(4));

        Client client = Client.forNetwork(defaultNetwork);
        @Var var network = client.getNetwork();

        Assertions.assertEquals(2, network.size());
        Assertions.assertEquals(network.get("0.testnet.hedera.com:50211"), new AccountId(3));
        Assertions.assertEquals(network.get("1.testnet.hedera.com:50211"), new AccountId(4));

        client.setNetwork(defaultNetwork);
        network = client.getNetwork();

        Assertions.assertEquals(2, network.size());
        Assertions.assertEquals(network.get("0.testnet.hedera.com:50211"), new AccountId(3));
        Assertions.assertEquals(network.get("1.testnet.hedera.com:50211"), new AccountId(4));

        var defaultNetworkWithExtraNode = new HashMap<String, AccountId>();
        defaultNetworkWithExtraNode.put("0.testnet.hedera.com:50211", new AccountId(3));
        defaultNetworkWithExtraNode.put("1.testnet.hedera.com:50211", new AccountId(4));
        defaultNetworkWithExtraNode.put("2.testnet.hedera.com:50211", new AccountId(5));

        client.setNetwork(defaultNetworkWithExtraNode);
        network = client.getNetwork();

        Assertions.assertEquals(3, network.size());
        Assertions.assertEquals(network.get("0.testnet.hedera.com:50211"), new AccountId(3));
        Assertions.assertEquals(network.get("1.testnet.hedera.com:50211"), new AccountId(4));
        Assertions.assertEquals(network.get("2.testnet.hedera.com:50211"), new AccountId(5));

        var singleNodeNetwork = new HashMap<String, AccountId>();
        singleNodeNetwork.put("2.testnet.hedera.com:50211", new AccountId(5));

        client.setNetwork(singleNodeNetwork);
        network = client.getNetwork();

        Assertions.assertEquals(1, network.size());
        Assertions.assertEquals(network.get("2.testnet.hedera.com:50211"), new AccountId(5));

        var singleNodeNetworkWithDifferentAccountId = new HashMap<String, AccountId>();
        singleNodeNetworkWithDifferentAccountId.put("2.testnet.hedera.com:50211", new AccountId(6));

        client.setNetwork(singleNodeNetworkWithDifferentAccountId);
        network = client.getNetwork();

        Assertions.assertEquals(1, network.size());
        Assertions.assertEquals(network.get("2.testnet.hedera.com:50211"), new AccountId(6));

        client.close();
    }

    @Test
    @DisplayName("setNetwork() functions correctly")
    void setMirrorNetworkWorks() throws Exception {
        var defaultNetwork = new ArrayList<String>();
        defaultNetwork.add("hcs.testnet.mirrornode.hedera.com:5600");

        Client client = Client.forNetwork(new HashMap<>()).setMirrorNetwork(defaultNetwork);
        @Var var mirrorNetwork = new HashSet<>(client.getMirrorNetwork());

        Assertions.assertEquals(1, mirrorNetwork.size());
        Assertions.assertTrue(mirrorNetwork.contains("hcs.testnet.mirrornode.hedera.com:5600"));

        client.setMirrorNetwork(defaultNetwork);
        mirrorNetwork = new HashSet<>(client.getMirrorNetwork());

        Assertions.assertEquals(1, mirrorNetwork.size());
        Assertions.assertTrue(mirrorNetwork.contains("hcs.testnet.mirrornode.hedera.com:5600"));

        var defaultNetworkWithExtraNode = new ArrayList<String>();
        defaultNetworkWithExtraNode.add("hcs.testnet.mirrornode.hedera.com:5600");
        defaultNetworkWithExtraNode.add("hcs.testnet1.mirrornode.hedera.com:5600");

        client.setMirrorNetwork(defaultNetworkWithExtraNode);
        mirrorNetwork = new HashSet<>(client.getMirrorNetwork());

        Assertions.assertEquals(2, mirrorNetwork.size());
        Assertions.assertTrue(mirrorNetwork.contains("hcs.testnet.mirrornode.hedera.com:5600"));
        Assertions.assertTrue(mirrorNetwork.contains("hcs.testnet1.mirrornode.hedera.com:5600"));

        var singleNodeNetwork = new ArrayList<String>();
        singleNodeNetwork.add("hcs.testnet1.mirrornode.hedera.com:5600");

        client.setMirrorNetwork(singleNodeNetwork);
        mirrorNetwork = new HashSet<>(client.getMirrorNetwork());

        Assertions.assertEquals(1, mirrorNetwork.size());
        Assertions.assertTrue(mirrorNetwork.contains("hcs.testnet1.mirrornode.hedera.com:5600"));

        var singleNodeNetworkWithDifferentNode = new ArrayList<String>();
        singleNodeNetworkWithDifferentNode.add("hcs.testnet.mirrornode.hedera.com:5600");

        client.setMirrorNetwork(singleNodeNetworkWithDifferentNode);
        mirrorNetwork = new HashSet<>(client.getMirrorNetwork());

        Assertions.assertEquals(1, mirrorNetwork.size());
        Assertions.assertTrue(mirrorNetwork.contains("hcs.testnet.mirrornode.hedera.com:5600"));

        client.close();
    }
}
