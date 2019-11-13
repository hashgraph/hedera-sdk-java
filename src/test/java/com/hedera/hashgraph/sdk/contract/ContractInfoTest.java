package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hederahashgraph.api.proto.java.ContractGetInfoResponse;
import com.hederahashgraph.api.proto.java.Response;

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
            () -> ContractInfo.fromResponse(Response.getDefaultInstance())
        );
    }

    @Test
    @DisplayName("doesn't require a key")
    void doesntRequireKey() {
        final Response response = Response.newBuilder()
            .setContractGetInfo(ContractGetInfoResponse.getDefaultInstance())
            .build();

        final ContractInfo contractInfo = ContractInfo.fromResponse(response);

        assertNull(contractInfo.adminKey);
    }

    @Test
    @DisplayName("deserializes from a correct response")
    void correct() {
        final Response response = Response.newBuilder()
            .setContractGetInfo(
                ContractGetInfoResponse.newBuilder()
                    .setContractInfo(
                        ContractGetInfoResponse.ContractInfo.newBuilder()
                            .setStorage(1234)))
            .build();

        final ContractInfo contractInfo = ContractInfo.fromResponse(response);

        assertEquals(contractInfo.accountId, new AccountId(0));
        assertEquals(contractInfo.contractAccountId, "");
        assertEquals(contractInfo.contractId, new ContractId(0, 0, 0));
        assertNull(contractInfo.adminKey);
        assertEquals(contractInfo.expirationTime, Instant.EPOCH);
        assertEquals(contractInfo.autoRenewPeriod, Duration.ZERO);
        assertEquals(contractInfo.storage, 1234);
    }
}
