/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import org.junit.jupiter.api.Test;

import static com.hedera.hashgraph.sdk.BaseNodeAddress.PORT_MIRROR_PLAIN;
import static com.hedera.hashgraph.sdk.BaseNodeAddress.PORT_MIRROR_TLS;
import static com.hedera.hashgraph.sdk.BaseNodeAddress.PORT_NODE_PLAIN;
import static com.hedera.hashgraph.sdk.BaseNodeAddress.PORT_NODE_TLS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class BaseNodeAddressTest {
    @Test
    void fromString() {
        var ipAddress = BaseNodeAddress.fromString("35.237.200.180:50211");
        assertThat(ipAddress.getName()).isNull();
        assertThat(ipAddress.getAddress()).isEqualTo("35.237.200.180");
        assertThat(ipAddress.getPort()).isEqualTo(PORT_NODE_PLAIN);
        assertThat(ipAddress.toString()).isEqualTo("35.237.200.180:50211");

        var ipAddressSecure = ipAddress.toSecure();
        assertThat(ipAddressSecure.getName()).isNull();
        assertThat(ipAddressSecure.getAddress()).isEqualTo("35.237.200.180");
        assertThat(ipAddressSecure.getPort()).isEqualTo(PORT_NODE_TLS);
        assertThat(ipAddressSecure.toString()).isEqualTo("35.237.200.180:50212");

        var ipAddressInsecure = ipAddressSecure.toInsecure();
        assertThat(ipAddressInsecure.getName()).isNull();
        assertThat(ipAddressInsecure.getAddress()).isEqualTo("35.237.200.180");
        assertThat(ipAddressInsecure.getPort()).isEqualTo(PORT_NODE_PLAIN);
        assertThat(ipAddressInsecure.toString()).isEqualTo("35.237.200.180:50211");

        var urlAddress = BaseNodeAddress.fromString("0.testnet.hedera.com:50211");
        assertThat(urlAddress.getName()).isNull();
        assertThat(urlAddress.getAddress()).isEqualTo("0.testnet.hedera.com");
        assertThat(urlAddress.getPort()).isEqualTo(PORT_NODE_PLAIN);
        assertThat(urlAddress.toString()).isEqualTo("0.testnet.hedera.com:50211");

        var urlAddressSecure = urlAddress.toSecure();
        assertThat(urlAddressSecure.getName()).isNull();
        assertThat(urlAddressSecure.getAddress()).isEqualTo("0.testnet.hedera.com");
        assertThat(urlAddressSecure.getPort()).isEqualTo(PORT_NODE_TLS);
        assertThat(urlAddressSecure.toString()).isEqualTo("0.testnet.hedera.com:50212");

        var urlAddressInsecure = urlAddressSecure.toInsecure();
        assertThat(urlAddressInsecure.getName()).isNull();
        assertThat(urlAddressInsecure.getAddress()).isEqualTo("0.testnet.hedera.com");
        assertThat(urlAddressInsecure.getPort()).isEqualTo(PORT_NODE_PLAIN);
        assertThat(urlAddressInsecure.toString()).isEqualTo("0.testnet.hedera.com:50211");

        var processAddress = BaseNodeAddress.fromString("in-process:testingProcess");
        assertThat(processAddress.getName()).isEqualTo("testingProcess");
        assertThat(processAddress.getAddress()).isNull();
        assertThat(processAddress.getPort()).isEqualTo(0);
        assertThat(processAddress.toString()).isEqualTo("testingProcess");

        var processAddressSecure = processAddress.toSecure();
        assertThat(processAddressSecure.getName()).isEqualTo("testingProcess");
        assertThat(processAddressSecure.getAddress()).isNull();
        assertThat(processAddressSecure.getPort()).isEqualTo(0);
        assertThat(processAddressSecure.toString()).isEqualTo("testingProcess");

        var processAddressInsecure = processAddressSecure.toInsecure();
        assertThat(processAddressInsecure.getName()).isEqualTo("testingProcess");
        assertThat(processAddressInsecure.getAddress()).isNull();
        assertThat(processAddressInsecure.getPort()).isEqualTo(0);
        assertThat(processAddressInsecure.toString()).isEqualTo("testingProcess");

        var mirrorNodeAddress = BaseNodeAddress.fromString("mainnet-public.mirrornode.hedera.com:5600");
        assertThat(mirrorNodeAddress.getName()).isNull();
        assertThat(mirrorNodeAddress.getAddress()).isEqualTo("mainnet-public.mirrornode.hedera.com");
        assertThat(mirrorNodeAddress.getPort()).isEqualTo(PORT_MIRROR_PLAIN);
        assertThat(mirrorNodeAddress.toString()).isEqualTo("mainnet-public.mirrornode.hedera.com:5600");

        var mirrorNodeAddressSecure = mirrorNodeAddress.toSecure();
        assertThat(mirrorNodeAddressSecure.getName()).isNull();
        assertThat(mirrorNodeAddressSecure.getAddress()).isEqualTo("mainnet-public.mirrornode.hedera.com");
        assertThat(mirrorNodeAddressSecure.getPort()).isEqualTo(PORT_MIRROR_TLS);
        assertThat(mirrorNodeAddressSecure.toString()).isEqualTo("mainnet-public.mirrornode.hedera.com:443");

        var mirrorNodeAddressInsecure = mirrorNodeAddressSecure.toInsecure();
        assertThat(mirrorNodeAddressInsecure.getName()).isNull();
        assertThat(mirrorNodeAddressInsecure.getAddress()).isEqualTo("mainnet-public.mirrornode.hedera.com");
        assertThat(mirrorNodeAddressInsecure.getPort()).isEqualTo(PORT_MIRROR_PLAIN);
        assertThat(mirrorNodeAddressInsecure.toString()).isEqualTo("mainnet-public.mirrornode.hedera.com:5600");

        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> BaseNodeAddress.fromString("this is a random string with spaces:443"));
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> BaseNodeAddress.fromString("mainnet-public.mirrornode.hedera.com:notarealport"));
    }
}
