package com.hedera.hashgraph.sdk.account;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.crypto.Key;
import com.hederahashgraph.api.proto.java.Claim;
import com.hederahashgraph.api.proto.java.CryptoAddClaimTransactionBody;
import com.hederahashgraph.api.proto.java.KeyList;
import com.hederahashgraph.api.proto.java.Transaction;
import com.hederahashgraph.api.proto.java.TransactionResponse;
import com.hederahashgraph.service.proto.java.CryptoServiceGrpc;

import javax.annotation.Nullable;

import io.grpc.MethodDescriptor;

// corresponds to `CryptoAddClaimTransaction`
public final class AccountAddClaimTransaction extends TransactionBuilder<AccountAddClaimTransaction> {
    private final CryptoAddClaimTransactionBody.Builder builder = bodyBuilder.getCryptoAddClaimBuilder();
    private final Claim.Builder claim = builder.getClaimBuilder();
    private final KeyList.Builder keyList = claim.getKeysBuilder();

    public AccountAddClaimTransaction(@Nullable Client client) {
        super(client);
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
            ".addKey() required");
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return CryptoServiceGrpc.getAddClaimMethod();
    }
}
