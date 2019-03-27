package com.hedera.sdk;

import com.google.protobuf.ByteString;
import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.Claim;
import com.hedera.sdk.proto.CryptoAddClaimTransactionBody;
import com.hedera.sdk.proto.KeyList;

public final class CryptoAddClaimTransaction extends TransactionBuilder<CryptoAddClaimTransaction> {
    private final CryptoAddClaimTransactionBody.Builder builder;
    private final Claim.Builder claim;
    private final KeyList.Builder keyList;

    public CryptoAddClaimTransaction() {
        builder = inner.getBodyBuilder().getCryptoAddClaimBuilder();
        claim = builder.getClaimBuilder();
        keyList = claim.getKeysBuilder();
    }

    public CryptoAddClaimTransaction setAccount(AccountId id) {
        // fixme: not sure if both need to be used
        builder.setAccountID(id.inner);
        claim.setAccountID(id.inner);
        return this;
    }

    public CryptoAddClaimTransaction setHash(byte[] hash) {
        claim.setHash(ByteString.copyFrom(hash));
        return this;
    }

    public CryptoAddClaimTransaction addKey(Key key) {
        keyList.addKeys(key.toProtoKey());
        return this;
    }
}
