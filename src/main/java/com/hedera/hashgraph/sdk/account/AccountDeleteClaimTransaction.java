package com.hedera.hashgraph.sdk.account;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.proto.CryptoDeleteClaimTransactionBody;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;

// `CryptoDeleteClaimTransaction`
public class AccountDeleteClaimTransaction extends TransactionBuilder<AccountDeleteClaimTransaction> {
    private final CryptoDeleteClaimTransactionBody.Builder builder = bodyBuilder.getCryptoDeleteClaimBuilder();

    public AccountDeleteClaimTransaction(@Nullable Client client) {
        super(client);
    }

    public AccountDeleteClaimTransaction setAccountToDeleteFrom(AccountId accountId) {
        builder.setAccountIDToDeleteFrom(accountId.toProto());
        return this;
    }

    public AccountDeleteClaimTransaction setHashToDelete(byte[] hashToDelete) {
        builder.setHashToDelete(ByteString.copyFrom(hashToDelete));
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasAccountIDToDeleteFrom(), ".setAccountToDeleteFrom() required");
        require(builder.getHashToDelete(), ".setHashToDelete() required");
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return CryptoServiceGrpc.getDeleteClaimMethod();
    }
}
