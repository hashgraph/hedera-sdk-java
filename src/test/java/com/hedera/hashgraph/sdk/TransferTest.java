package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TransferTest {
    @Test
    @DisplayName("Transfer.toString() prints useful information")
    void transferToString() {
        final Transfer transfer = new Transfer(new AccountId(3), new Hbar(5));

        // we're just testing that `Transfer` doesn't lose its `toString()` override somehow
        // the format may change arbitrarily
        Assertions.assertEquals(transfer.toString(),
            "Transfer{accountId=0.0.3, amount=5 ℏ (500000000 tℏ)}");
    }
}
