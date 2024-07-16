package com.hedera.hashgraph.tck.methods.sdk.param;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Test;

class AccountCreateParamsTest {

    @Test
    void testParseWithAllFields() {
        Map<String, Object> jrpcParams = new HashMap<>();
        jrpcParams.put("key", "someKey");
        jrpcParams.put("initialBalance", 1000L);
        jrpcParams.put("receiverSignatureRequired", true);
        jrpcParams.put("autoRenewPeriod", 7890000L);
        jrpcParams.put("memo", "test memo");
        jrpcParams.put("maxAutoTokenAssociations", 10L);
        jrpcParams.put("stakedAccountId", "stakedAccountId");
        jrpcParams.put("stakedNodeId", 5L);
        jrpcParams.put("declineStakingReward", true);
        jrpcParams.put("alias", "alias");
        jrpcParams.put("signerKey", "signerKey");

        JSONObject commonParamsJson = new JSONObject();
        commonParamsJson.put("transactionId", "txId");
        commonParamsJson.put("maxTransactionFee", 100L);
        commonParamsJson.put("validTransactionDuration", 120L);
        commonParamsJson.put("memo", "commonMemo");
        commonParamsJson.put("regenerateTransactionId", true);
        commonParamsJson.put("signers", List.of("signer1", "signer2"));

        jrpcParams.put("commonTransactionParams", commonParamsJson);

        AccountCreateParams params = new AccountCreateParams().parse(jrpcParams);

        assertEquals(Optional.of("someKey"), params.getKey());
        assertEquals(Optional.of(1000L), params.getInitialBalance());
        assertEquals(Optional.of(true), params.getReceiverSignatureRequired());
        assertEquals(Optional.of(7890000L), params.getAutoRenewPeriod());
        assertEquals(Optional.of("test memo"), params.getMemo());
        assertEquals(Optional.of(10L), params.getMaxAutoTokenAssociations());
        assertEquals(Optional.of("stakedAccountId"), params.getStakedAccountId());
        assertEquals(Optional.of(5L), params.getStakedNodeId());
        assertEquals(Optional.of(true), params.getDeclineStakingReward());
        assertEquals(Optional.of("alias"), params.getAlias());
        assertEquals(Optional.of("signerKey"), params.getSignerKey());

        assertTrue(params.getCommonTransactionParams().isPresent());
        CommonTransactionParams commonParams =
                params.getCommonTransactionParams().get();
        assertEquals(Optional.of("txId"), commonParams.getTransactionId());
        assertEquals(Optional.of(100L), commonParams.getMaxTransactionFee());
        assertEquals(Optional.of(120L), commonParams.getValidTransactionDuration());
        assertEquals(Optional.of("commonMemo"), commonParams.getMemo());
        assertEquals(Optional.of(true), commonParams.getRegenerateTransactionId());
        assertEquals(Optional.of(List.of("signer1", "signer2")), commonParams.getSigners());
    }

    @Test
    void testParseWithOptionalFieldsAbsent() {
        Map<String, Object> jrpcParams = new HashMap<>();
        jrpcParams.put("key", "someKey");

        AccountCreateParams params = new AccountCreateParams().parse(jrpcParams);

        assertEquals(Optional.of("someKey"), params.getKey());
        assertEquals(Optional.empty(), params.getInitialBalance());
        assertEquals(Optional.empty(), params.getReceiverSignatureRequired());
        assertEquals(Optional.empty(), params.getAutoRenewPeriod());
        assertEquals(Optional.empty(), params.getMemo());
        assertEquals(Optional.empty(), params.getMaxAutoTokenAssociations());
        assertEquals(Optional.empty(), params.getStakedAccountId());
        assertEquals(Optional.empty(), params.getStakedNodeId());
        assertEquals(Optional.empty(), params.getDeclineStakingReward());
        assertEquals(Optional.empty(), params.getAlias());
        assertEquals(Optional.empty(), params.getSignerKey());
        assertEquals(Optional.empty(), params.getCommonTransactionParams());
    }

    @Test
    void testParseWithInvalidFieldTypes() {
        Map<String, Object> jrpcParams = new HashMap<>();
        jrpcParams.put("key", 123); // Invalid type

        assertThrows(ClassCastException.class, () -> {
            new AccountCreateParams().parse(jrpcParams);
        });
    }

    @Test
    void testParseWithEmptyParams() {
        Map<String, Object> jrpcParams = new HashMap<>();

        AccountCreateParams params = new AccountCreateParams().parse(jrpcParams);

        assertEquals(Optional.empty(), params.getKey());
        assertEquals(Optional.empty(), params.getInitialBalance());
        assertEquals(Optional.empty(), params.getReceiverSignatureRequired());
        assertEquals(Optional.empty(), params.getAutoRenewPeriod());
        assertEquals(Optional.empty(), params.getMemo());
        assertEquals(Optional.empty(), params.getMaxAutoTokenAssociations());
        assertEquals(Optional.empty(), params.getStakedAccountId());
        assertEquals(Optional.empty(), params.getStakedNodeId());
        assertEquals(Optional.empty(), params.getDeclineStakingReward());
        assertEquals(Optional.empty(), params.getAlias());
        assertEquals(Optional.empty(), params.getSignerKey());
        assertEquals(Optional.empty(), params.getCommonTransactionParams());
    }

    @Test
    void testCommonTransactionParamsParse() {
        Map<String, Object> jrpcParams = new HashMap<>();
        jrpcParams.put("transactionId", "txId");
        jrpcParams.put("maxTransactionFee", 100L);
        jrpcParams.put("validTransactionDuration", 120L);
        jrpcParams.put("memo", "commonMemo");
        jrpcParams.put("regenerateTransactionId", true);
        jrpcParams.put("signers", List.of("signer1", "signer2"));

        CommonTransactionParams params = CommonTransactionParams.parse(jrpcParams);

        assertEquals(Optional.of("txId"), params.getTransactionId());
        assertEquals(Optional.of(100L), params.getMaxTransactionFee());
        assertEquals(Optional.of(120L), params.getValidTransactionDuration());
        assertEquals(Optional.of("commonMemo"), params.getMemo());
        assertEquals(Optional.of(true), params.getRegenerateTransactionId());
        assertEquals(Optional.of(List.of("signer1", "signer2")), params.getSigners());
    }
}
