package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientTest {
    @Test
    @DisplayName("Can construct mainnet client")
    void forMainnet() {
        Client.forMainnet();
    }


    @Test
    @DisplayName("Client.setMaxQueryPayment() negative")
    void setMaxQueryPaymentNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            Client.forTestnet()
                .setMaxQueryPayment(Hbar.MIN);
        });
    }

    @Test
    @DisplayName("Client.setMaxTransactionFee() negative")
    void setMaxTransactionFeeNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            Client.forTestnet()
                .setMaxTransactionFee(Hbar.MIN);
        });
    }

    @Test
    @DisplayName("fromJsonFile() functions correctly")
    void fromJsonFile() throws IOException {
        Client.fromJsonFile(new File("./src/test/resources/client-config.json"));
        Client.fromJsonFile(new File("./src/test/resources/client-config-with-operator.json"));
        Client.fromJsonFile("./src/test/resources/client-config.json");
        Client.fromJsonFile("./src/test/resources/client-config-with-operator.json");
    }

    @Test
    @DisplayName("fromJson() functions correctly")
    void testFromJson() {
        // Copied content of `client-config-with-operator.json`
        Client.fromJson("{\n" +
            "    \"network\": {\n" +
            "        \"35.237.200.180:50211\": \"0.0.3\",\n" +
            "        \"35.186.191.247:50211\": \"0.0.4\",\n" +
            "        \"35.192.2.25:50211\": \"0.0.5\",\n" +
            "        \"35.199.161.108:50211\": \"0.0.6\",\n" +
            "        \"35.203.82.240:50211\": \"0.0.7\",\n" +
            "        \"35.236.5.219:50211\": \"0.0.8\",\n" +
            "        \"35.197.192.225:50211\": \"0.0.9\",\n" +
            "        \"35.242.233.154:50211\": \"0.0.10\",\n" +
            "        \"35.240.118.96:50211\": \"0.0.11\",\n" +
            "        \"35.204.86.32:50211\": \"0.0.12\"\n" +
            "    },\n" +
            "    \"operator\": {\n" +
            "        \"accountId\": \"0.0.3636\",\n" +
            "        \"privateKey\": \"302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10\"\n" +
            "    }\n" +
            "}\n");

        // put it in a file for nicer formatting
        InputStream clientConfig = ClientTest.class.getClassLoader()
            .getResourceAsStream("client-config.json");

        Assertions.assertNotNull(clientConfig);

        Client.fromJson(new InputStreamReader(clientConfig, StandardCharsets.UTF_8));

        // put it in a file for nicer formatting
        InputStream clientConfigWithOperator = ClientTest.class.getClassLoader()
            .getResourceAsStream("client-config-with-operator.json");

        Assertions.assertNotNull(clientConfigWithOperator);
    }

    @Test
    @DisplayName("setNetwork() functions correctly")
    void testReplaceNodes() {
        @Var Map<AccountId, String> nodes = new HashMap<>();
        nodes.put(new AccountId(3), "0.testnet.hedera.com:50211");
        nodes.put(new AccountId(4), "1.testnet.hedera.com:50211");

        Client client = new Client(nodes);


        nodes = new HashMap<>();
        nodes.put(new AccountId(5), "2.testnet.hedera.com:50211");
        nodes.put(new AccountId(6), "3.testnet.hedera.com:50211");

        client.setNetwork(nodes);

        Assertions.assertEquals(client.getChannel(new AccountId(5)).authority(), "2.testnet.hedera.com:50211");
        Assertions.assertThrows(IllegalArgumentException.class , () -> client.getChannel(new AccountId(3)));
    }
}
