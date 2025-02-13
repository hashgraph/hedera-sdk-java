// SPDX-License-Identifier: Apache-2.0
package com.hedera.hashgraph.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import com.hedera.hashgraph.sdk.proto.TokenSupplyType;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TokenSupplyTypeTest {
    private final TokenSupplyType tokenSupplyTypeInfinite = TokenSupplyType.INFINITE;
    private final TokenSupplyType tokenSupplyTypeFinite = TokenSupplyType.FINITE;

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start(Snapshot::asJsonString);
    }

    @AfterAll
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void fromProtobuf() {
        SnapshotMatcher.expect(
                        com.hedera.hashgraph.sdk.TokenSupplyType.valueOf(tokenSupplyTypeInfinite)
                                .toString(),
                        com.hedera.hashgraph.sdk.TokenSupplyType.valueOf(tokenSupplyTypeFinite)
                                .toString())
                .toMatchSnapshot();
    }

    @Test
    void toProtobuf() {
        SnapshotMatcher.expect(
                        com.hedera.hashgraph.sdk.TokenSupplyType.valueOf(tokenSupplyTypeInfinite)
                                .toProtobuf(),
                        com.hedera.hashgraph.sdk.TokenSupplyType.valueOf(tokenSupplyTypeFinite)
                                .toProtobuf())
                .toMatchSnapshot();
    }

    @Test
    void tokenSupplyTestToString() {
        assertThat(com.hedera.hashgraph.sdk.TokenSupplyType.INFINITE).hasToString("INFINITE");
        assertThat(com.hedera.hashgraph.sdk.TokenSupplyType.FINITE).hasToString("FINITE");
    }
}
