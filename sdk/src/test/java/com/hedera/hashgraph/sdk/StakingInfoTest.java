package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class StakingInfoTest {
    final Instant validStart = Instant.ofEpochSecond(1554158542);

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    StakingInfo spawnStakingInfoAccountExample() {
        return new StakingInfo(
            true,
            validStart,
            Hbar.from(5),
            Hbar.from(10),
            AccountId.fromString("1.2.3"),
            null
        );
    }

    StakingInfo spawnStakingInfoNodeExample() {
        return new StakingInfo(
            true,
            validStart,
            Hbar.from(5),
            Hbar.from(10),
            null,
            3L
        );
    }

    @Test
    void shouldSerializeAccount() throws Exception {
        var originalStakingInfo = spawnStakingInfoAccountExample();
        byte[] stakingInfoBytes = originalStakingInfo.toBytes();
        var copyStakingInfo = StakingInfo.fromBytes(stakingInfoBytes);
        assertThat(copyStakingInfo.toString().replaceAll("@[A-Za-z0-9]+", ""))
            .isEqualTo(originalStakingInfo.toString().replaceAll("@[A-Za-z0-9]+", ""));
        SnapshotMatcher.expect(originalStakingInfo.toString().replaceAll("@[A-Za-z0-9]+", "")).toMatchSnapshot();
    }

    @Test
    void shouldSerializeNode() throws Exception {
        var originalStakingInfo = spawnStakingInfoNodeExample();
        byte[] stakingInfoBytes = originalStakingInfo.toBytes();
        var copyStakingInfo = StakingInfo.fromBytes(stakingInfoBytes);
        assertThat(copyStakingInfo.toString().replaceAll("@[A-Za-z0-9]+", ""))
            .isEqualTo(originalStakingInfo.toString().replaceAll("@[A-Za-z0-9]+", ""));
        SnapshotMatcher.expect(originalStakingInfo.toString().replaceAll("@[A-Za-z0-9]+", "")).toMatchSnapshot();
    }
}
