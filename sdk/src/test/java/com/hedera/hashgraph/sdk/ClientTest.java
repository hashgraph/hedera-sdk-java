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
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
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
        var defaultNetwork = Map.of(
                "0.testnet.hedera.com:50211", new AccountId(3),
                "1.testnet.hedera.com:50211", new AccountId(4)
        );

        Client client = Client.forNetwork(defaultNetwork);
        assertThat(client.getNetwork()).containsExactlyInAnyOrderEntriesOf(defaultNetwork);

        client.setNetwork(defaultNetwork);
        assertThat(client.getNetwork()).containsExactlyInAnyOrderEntriesOf(defaultNetwork);

        var defaultNetworkWithExtraNode = Map.of(
                "0.testnet.hedera.com:50211", new AccountId(3),
                "1.testnet.hedera.com:50211", new AccountId(4),
                "2.testnet.hedera.com:50211", new AccountId(5)
        );

        client.setNetwork(defaultNetworkWithExtraNode);
        assertThat(client.getNetwork()).containsExactlyInAnyOrderEntriesOf(defaultNetworkWithExtraNode);

        var singleNodeNetwork = Map.of(
                "2.testnet.hedera.com:50211", new AccountId(5)
        );

        client.setNetwork(singleNodeNetwork);
        assertThat(client.getNetwork()).containsExactlyInAnyOrderEntriesOf(singleNodeNetwork);

        var singleNodeNetworkWithDifferentAccountId = Map.of(
                "2.testnet.hedera.com:50211", new AccountId(6)
        );

        client.setNetwork(singleNodeNetworkWithDifferentAccountId);
        assertThat(client.getNetwork()).containsExactlyInAnyOrderEntriesOf(singleNodeNetworkWithDifferentAccountId);

        var multiAddressNetwork = Map.of(
                "0.testnet.hedera.com:50211", new AccountId(3),
                "34.94.106.61:50211", new AccountId(3),
                "50.18.132.211:50211", new AccountId(3),
                "138.91.142.219:50211", new AccountId(3),

                "1.testnet.hedera.com:50211", new AccountId(4),
                "35.237.119.55:50211", new AccountId(4),
                "3.212.6.13:50211", new AccountId(4),
                "52.168.76.241:50211", new AccountId(4)
        );

        client.setNetwork(multiAddressNetwork);
        assertThat(client.getNetwork()).containsExactlyInAnyOrderEntriesOf(multiAddressNetwork);

        client.close();
    }

    @Test
    @DisplayName("setMirrorNetwork() functions correctly")
    void setMirrorNetworkWorks() throws Exception {
        var defaultNetwork = List.of("hcs.testnet.mirrornode.hedera.com:5600");

        Client client = Client.forNetwork(new HashMap<>()).setMirrorNetwork(defaultNetwork);
        assertThat(client.getMirrorNetwork()).containsExactlyInAnyOrderElementsOf(defaultNetwork);

        client.setMirrorNetwork(defaultNetwork);
        assertThat(client.getMirrorNetwork()).containsExactlyInAnyOrderElementsOf(defaultNetwork);

        var defaultNetworkWithExtraNode = List.of(
                "hcs.testnet.mirrornode.hedera.com:5600",
                "hcs.testnet1.mirrornode.hedera.com:5600"
        );

        client.setMirrorNetwork(defaultNetworkWithExtraNode);
        assertThat(client.getMirrorNetwork()).containsExactlyInAnyOrderElementsOf(defaultNetworkWithExtraNode);

        var singleNodeNetwork = List.of("hcs.testnet1.mirrornode.hedera.com:5600");

        client.setMirrorNetwork(singleNodeNetwork);
        assertThat(client.getMirrorNetwork()).containsExactlyInAnyOrderElementsOf(singleNodeNetwork);

        var singleNodeNetworkWithDifferentNode = List.of("hcs.testnet.mirrornode.hedera.com:5600");

        client.setMirrorNetwork(singleNodeNetworkWithDifferentNode);
        assertThat(client.getMirrorNetwork()).containsExactlyInAnyOrderElementsOf(singleNodeNetworkWithDifferentNode);

        client.close();
    }

    @Test
    void testExecuteAsyncTimeout() throws Exception {
        AccountId accountId = AccountId.fromString("0.0.1");
        Duration timeout = Duration.ofSeconds(5);

        Client client = Client.forNetwork(Map.of("127.0.0.1:50211", accountId))
            .setRequestTimeout(timeout)
            .setNodeMinBackoff(Duration.ofMillis(0))
            .setNodeMaxBackoff(Duration.ofMillis(0))
            .setMinNodeReadmitTime(Duration.ofMillis(0))
            .setMaxNodeReadmitTime(Duration.ofMillis(0));
        AccountBalanceQuery query = new AccountBalanceQuery()
                .setAccountId(accountId)
                .setMaxAttempts(3);
        Instant start = Instant.now();

        try {
            query.executeAsync(client).get();
        } catch (ExecutionException e) {
            // fine...
        }
        long secondsTaken = java.time.Duration.between(start, Instant.now()).toSeconds();

        // 20 seconds would indicate we tried 2 times to connect
        assertThat(secondsTaken).isLessThan(7);
    }

    @Test
    void testExecuteSyncTimeout() throws Exception {
        AccountId accountId = AccountId.fromString("0.0.1");
        // Executing requests in sync mode will require at most 10 seconds to connect
        // to a gRPC node. If we're not able to connect to a gRPC node within 10 seconds
        // we fail that request attempt. This means setting at timeout on a request
        // which hits non-connecting gRPC nodes will fail within ~10s of the set timeout
        // e.g. setting a timeout of 15 seconds, the request could fail within the range
        // of [5 seconds, 25 seconds]. The 10 second timeout for connecting to gRPC nodes
        // is not configurable.
        Duration timeout = Duration.ofSeconds(5);

        Client client = Client.forNetwork(Map.of("127.0.0.1:50211", accountId))
                .setRequestTimeout(timeout);
        AccountBalanceQuery query = new AccountBalanceQuery()
                .setAccountId(accountId)
                .setMaxAttempts(3);
        Instant start = Instant.now();

        try {
            query.execute(client);
        } catch (TimeoutException e) {
            // fine...
        }
        long secondsTaken = java.time.Duration.between(start, Instant.now()).toSeconds();

        // 20 seconds would indicate we tried 2 times to connect
        assertThat(secondsTaken).isLessThan(20);
    }
}
