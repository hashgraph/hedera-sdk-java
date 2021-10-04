package com.hedera.hashgraph.sdk;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ManagedNodeAddressTest {
    @Test
    void fromString() {
        var ipAddress = ManagedNodeAddress.fromString("35.237.200.180:50211");
        Assertions.assertNull(ipAddress.getName());
        Assertions.assertEquals(ipAddress.getAddress(), "35.237.200.180");
        Assertions.assertEquals(ipAddress.getPort(), 50211);
        Assertions.assertEquals(ipAddress.toString(), "35.237.200.180:50211");

        var ipAddressSecure = ipAddress.toSecure();
        Assertions.assertNull(ipAddressSecure.getName());
        Assertions.assertEquals(ipAddressSecure.getAddress(), "35.237.200.180");
        Assertions.assertEquals(ipAddressSecure.getPort(), 50212);
        Assertions.assertEquals(ipAddressSecure.toString(), "35.237.200.180:50212");

        var ipAddressInsecure = ipAddressSecure.toInsecure();
        Assertions.assertNull(ipAddressInsecure.getName());
        Assertions.assertEquals(ipAddressInsecure.getAddress(), "35.237.200.180");
        Assertions.assertEquals(ipAddressInsecure.getPort(), 50211);
        Assertions.assertEquals(ipAddressInsecure.toString(), "35.237.200.180:50211");

        var urlAddress = ManagedNodeAddress.fromString("0.testnet.hedera.com:50211");
        Assertions.assertNull(urlAddress.getName());
        Assertions.assertEquals(urlAddress.getAddress(), "0.testnet.hedera.com");
        Assertions.assertEquals(urlAddress.getPort(), 50211);
        Assertions.assertEquals(urlAddress.toString(), "0.testnet.hedera.com:50211");

        var urlAddressSecure = urlAddress.toSecure();
        Assertions.assertNull(urlAddressSecure.getName());
        Assertions.assertEquals(urlAddressSecure.getAddress(), "0.testnet.hedera.com");
        Assertions.assertEquals(urlAddressSecure.getPort(), 50212);
        Assertions.assertEquals(urlAddressSecure.toString(), "0.testnet.hedera.com:50212");

        var urlAddressInsecure = urlAddressSecure.toInsecure();
        Assertions.assertNull(urlAddressInsecure.getName());
        Assertions.assertEquals(urlAddressInsecure.getAddress(), "0.testnet.hedera.com");
        Assertions.assertEquals(urlAddressInsecure.getPort(), 50211);
        Assertions.assertEquals(urlAddressInsecure.toString(), "0.testnet.hedera.com:50211");

        var processAddress = ManagedNodeAddress.fromString("in-process:testingProcess");
        Assertions.assertEquals(processAddress.getName(), "testingProcess");
        Assertions.assertNull(processAddress.getAddress());
        Assertions.assertEquals(processAddress.getPort(), 0);
        Assertions.assertEquals(processAddress.toString(), "testingProcess");

        var processAddressSecure = processAddress.toSecure();
        Assertions.assertEquals(processAddressSecure.getName(), "testingProcess");
        Assertions.assertNull(processAddressSecure.getAddress());
        Assertions.assertEquals(processAddressSecure.getPort(), 0);
        Assertions.assertEquals(processAddressSecure.toString(), "testingProcess");

        var processAddressInsecure = processAddressSecure.toInsecure();
        Assertions.assertEquals(processAddressInsecure.getName(), "testingProcess");
        Assertions.assertNull(processAddressInsecure.getAddress());
        Assertions.assertEquals(processAddressInsecure.getPort(), 0);
        Assertions.assertEquals(processAddressInsecure.toString(), "testingProcess");
    }
}
