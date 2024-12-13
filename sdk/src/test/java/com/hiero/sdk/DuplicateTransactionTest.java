package com.hiero.sdk;

import com.hiero.sdk.AccountId;
import com.hiero.sdk.TransactionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DuplicateTransactionTest {

    @Test
    @DisplayName("Should generate unique transaction ids")
    void generateTransactionIds() {
        TransactionId[] ids = new TransactionId[1000000];
        AccountId accountId = AccountId.fromString("0.0.1000");
        for (int i = 0; i < ids.length; ++i) {
            ids[i] = TransactionId.generate(accountId);
        }
        HashSet<TransactionId> set = new HashSet<>(ids.length);
        for (int i = 0; i < ids.length; ++i) {
            assertThat(set.add(ids[i])).as("ids[%d] is not unique", i).isTrue();
        }
    }
}
