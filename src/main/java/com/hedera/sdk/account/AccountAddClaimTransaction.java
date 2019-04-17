package com.hedera.sdk.account;

import com.google.protobuf.ByteString;
import com.hedera.sdk.Client;
import com.hedera.sdk.TransactionBuilder;
import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.*;
import com.hedera.sdk.proto.Claim;
import com.hedera.sdk.proto.Transaction;
import io.grpc.MethodDescriptor;

// corresponds to `CryptoAddClaimTransaction`
public final class AccountAddClaimTransaction extends TransactionBuilder<AccountAddClaimTransaction> {
    private final CryptoAddClaimTransactionBody.Builder builder = bodyBuilder.getCryptoAddClaimBuilder();
    private final Claim.Builder claim = builder.getClaimBuilder();
    private final KeyList.Builder keyList = claim.getKeysBuilder();

    public AccountAddClaimTransaction(Client client) {
        super(client);
    }

    AccountAddClaimTransaction() {
        super(null);
    }

    public AccountAddClaimTransaction setAccountId(AccountId id) {
        // fixme: not sure if both need to be used
        var protoId = id.toProto();
        claim.setAccountID(protoId);
        return this;
    }

    public AccountAddClaimTransaction setHash(byte[] hash) {
        claim.setHash(ByteString.copyFrom(hash));
        return this;
    }

    public AccountAddClaimTransaction addKey(Key key) {
        keyList.addKeys(key.toKeyProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(claim.hasAccountID(), ".setAccountId() required");
        require(claim.getHash(), ".setHash() required");
        require(
            claim.getKeysOrBuilder()
                .getKeysOrBuilderList(),
            ".addKey() required"
        );
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return CryptoServiceGrpc.getAddClaimMethod();
    }
}
