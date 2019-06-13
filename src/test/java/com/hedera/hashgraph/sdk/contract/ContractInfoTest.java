package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.proto.ContractGetInfoResponse;
import com.hedera.hashgraph.sdk.proto.Response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContractInfoTest {

    @Test
    @DisplayName("won't deserialize from the wrong kind of response")
    void incorrectResponse() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new ContractInfo(Response.getDefaultInstance())
        );
    }

    @Test
    @DisplayName("doesn't require a key")
    void doesntRequireKey() {
        final var response = Response.newBuilder()
            .setContractGetInfo(ContractGetInfoResponse.getDefaultInstance())
            .build();

        final var contractInfo = new ContractInfo(response);

        assertNull(contractInfo.getAdminKey());
    }

    @Test
    @DisplayName("deserializes from a correct response")
    void correct() {
        final var response = Response.newBuilder()
            .setContractGetInfo(
                ContractGetInfoResponse.newBuilder()
                    .setContractInfo(
                        ContractGetInfoResponse.ContractInfo.newBuilder()
                            .setStorage(1234)))
            .build();

        final var contractInfo = new ContractInfo(response);

        assertEquals(contractInfo.getAccountId(), new AccountId(0));
        assertEquals(contractInfo.getContractAccountId(), "");
        assertEquals(contractInfo.getContractId(), new ContractId(0, 0, 0));
        assertNull(contractInfo.getAdminKey());
        assertEquals(contractInfo.getExpirationTime(), Instant.EPOCH);
        assertEquals(contractInfo.getAutoRenewPeriod(), Duration.ZERO);
        assertEquals(contractInfo.getStorage(), 1234);
    }
}
