package com.hedera.sdk;

import com.google.protobuf.ByteString;
import com.hedera.sdk.proto.CryptoDeleteClaimTransactionBody;

public class CryptoDeleteClaimTransaction extends TransactionBuilder<CryptoDeleteClaimTransaction> {
    private final CryptoDeleteClaimTransactionBody.Builder builder;

    public CryptoDeleteClaimTransaction() {
        builder = inner.getBodyBuilder().getCryptoDeleteClaimBuilder();
    }

    public CryptoDeleteClaimTransaction setAccountToDeleteFrom(AccountId accountId) {
        builder.setAccountIDToDeleteFrom(accountId.inner);
        return this;
    }

    public CryptoDeleteClaimTransaction setHashToDelete(byte[] hashToDelete) {
        builder.setHashToDelete(ByteString.copyFrom(hashToDelete));
        return this;
    }
}
