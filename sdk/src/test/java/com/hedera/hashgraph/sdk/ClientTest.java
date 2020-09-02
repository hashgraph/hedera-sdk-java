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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

        Client.fromJson(new InputStreamReader(clientConfig, StandardCharsets.UTF_8));

        // put it in a file for nicer formatting
        InputStream clientConfigWithOperator = ClientTest.class.getClassLoader()
            .getResourceAsStream("client-config-with-operator.json");

        Assertions.assertNotNull(clientConfigWithOperator);
    }

    @Test
    @DisplayName("setNetwork() functions correctly")
    void testReplaceNodes() {
        assertDoesNotThrow(() -> {
            @Var Map<String, AccountId> nodes = new HashMap<>();
            nodes.put("0.testnet.hedera.com:50211", new AccountId(3));
            nodes.put("1.testnet.hedera.com:50211", new AccountId(4));

            Client client = new Client(nodes);


            @Var Map<String, AccountId> setNetworkNodes = new HashMap<>();
            setNetworkNodes.put("2.testnet.hedera.com:50211", new AccountId(5));
            setNetworkNodes.put("3.testnet.hedera.com:50211", new AccountId(6));

            client.setNetwork(setNetworkNodes);

            Assertions.assertEquals(client.getNetworkChannel(new AccountId(5)).authority(), "2.testnet.hedera.com:50211");
            Assertions.assertThrows(IllegalArgumentException.class , () -> client.getNetworkChannel(new AccountId(3)));
        });
    }
}
