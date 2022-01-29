package com.hedera.hashgraph.sdk;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.hedera.hashgraph.sdk.ManagedNodeAddress.*;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ManagedNodeAddressTest {
    @Test
    void fromString() {
        var ipAddress = ManagedNodeAddress.fromString("35.237.200.180:50211");
        Assertions.assertNull(ipAddress.getName());
        Assertions.assertEquals(ipAddress.getAddress(), "35.237.200.180");
        Assertions.assertEquals(ipAddress.getPort(), PORT_NODE_PLAIN);
        Assertions.assertEquals(ipAddress.toString(), "35.237.200.180:50211");

        var ipAddressSecure = ipAddress.toSecure();
        Assertions.assertNull(ipAddressSecure.getName());
        Assertions.assertEquals(ipAddressSecure.getAddress(), "35.237.200.180");
        Assertions.assertEquals(ipAddressSecure.getPort(), PORT_NODE_TLS);
        Assertions.assertEquals(ipAddressSecure.toString(), "35.237.200.180:50212");

        var ipAddressInsecure = ipAddressSecure.toInsecure();
        Assertions.assertNull(ipAddressInsecure.getName());
        Assertions.assertEquals(ipAddressInsecure.getAddress(), "35.237.200.180");
        Assertions.assertEquals(ipAddressInsecure.getPort(), PORT_NODE_PLAIN);
        Assertions.assertEquals(ipAddressInsecure.toString(), "35.237.200.180:50211");

        var urlAddress = ManagedNodeAddress.fromString("0.testnet.hedera.com:50211");
        Assertions.assertNull(urlAddress.getName());
        Assertions.assertEquals(urlAddress.getAddress(), "0.testnet.hedera.com");
        Assertions.assertEquals(urlAddress.getPort(), PORT_NODE_PLAIN);
        Assertions.assertEquals(urlAddress.toString(), "0.testnet.hedera.com:50211");

        var urlAddressSecure = urlAddress.toSecure();
        Assertions.assertNull(urlAddressSecure.getName());
        Assertions.assertEquals(urlAddressSecure.getAddress(), "0.testnet.hedera.com");
        Assertions.assertEquals(urlAddressSecure.getPort(), PORT_NODE_TLS);
        Assertions.assertEquals(urlAddressSecure.toString(), "0.testnet.hedera.com:50212");

        var urlAddressInsecure = urlAddressSecure.toInsecure();
        Assertions.assertNull(urlAddressInsecure.getName());
        Assertions.assertEquals(urlAddressInsecure.getAddress(), "0.testnet.hedera.com");
        Assertions.assertEquals(urlAddressInsecure.getPort(), PORT_NODE_PLAIN);
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

        var mirrorNodeAddress = ManagedNodeAddress.fromString("hcs.mainnet.mirrornode.hedera.com:5600");
        Assertions.assertNull(mirrorNodeAddress.getName());
        Assertions.assertEquals(mirrorNodeAddress.getAddress(), "hcs.mainnet.mirrornode.hedera.com");
        Assertions.assertEquals(mirrorNodeAddress.getPort(), PORT_MIRROR_PLAIN);
        Assertions.assertEquals(mirrorNodeAddress.toString(), "hcs.mainnet.mirrornode.hedera.com:5600");

        var mirrorNodeAddressSecure = mirrorNodeAddress.toSecure();
        Assertions.assertNull(mirrorNodeAddressSecure.getName());
        Assertions.assertEquals(mirrorNodeAddressSecure.getAddress(), "hcs.mainnet.mirrornode.hedera.com");
        Assertions.assertEquals(mirrorNodeAddressSecure.getPort(), PORT_MIRROR_TLS);
        Assertions.assertEquals(mirrorNodeAddressSecure.toString(), "hcs.mainnet.mirrornode.hedera.com:443");

        var mirrorNodeAddressInsecure = mirrorNodeAddressSecure.toInsecure();
        Assertions.assertNull(mirrorNodeAddressInsecure.getName());
        Assertions.assertEquals(mirrorNodeAddressInsecure.getAddress(), "hcs.mainnet.mirrornode.hedera.com");
        Assertions.assertEquals(mirrorNodeAddressInsecure.getPort(), PORT_MIRROR_PLAIN);
        Assertions.assertEquals(mirrorNodeAddressInsecure.toString(), "hcs.mainnet.mirrornode.hedera.com:5600");

        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> ManagedNodeAddress.fromString("this is a random string with spaces:443"));
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> ManagedNodeAddress.fromString("hcs.mainnet.mirrornode.hedera.com:notarealport"));
    }
}
