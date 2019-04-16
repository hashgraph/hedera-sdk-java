package com.hedera.sdk;

import com.google.protobuf.ByteString;
import com.hedera.sdk.account.AccountId;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.sdk.crypto.ed25519.Ed25519Signature;
import com.hedera.sdk.proto.*;
import io.grpc.*;

import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;

public final class Transaction extends HederaCall<com.hedera.sdk.proto.Transaction, TransactionResponse, TransactionId> {

    private final io.grpc.MethodDescriptor<com.hedera.sdk.proto.Transaction, com.hedera.sdk.proto.TransactionResponse> methodDescriptor;
    private final com.hedera.sdk.proto.Transaction.Builder inner;

    @Nullable
    private final Client client;

    @Nullable
    private byte[] bodyBytes;

    Transaction(
        @Nullable Client client,
        com.hedera.sdk.proto.Transaction.Builder inner,
        MethodDescriptor<com.hedera.sdk.proto.Transaction, TransactionResponse> methodDescriptor
    ) {
        super();
        this.inner = inner;
        this.client = client;
        this.methodDescriptor = methodDescriptor;
    }

    public Transaction sign(Ed25519PrivateKey privateKey) {
        var signature = Ed25519Signature.forMessage(privateKey, getBodyBytes())
            .toBytes();

        inner.getSigMapBuilder()
            .addSigPair(
                SignaturePair.newBuilder()
                    .setPubKeyPrefix(
                        ByteString.copyFrom(
                            privateKey.getPublicKey()
                                .toBytes()
                        )
                    )
                    .setEd25519(ByteString.copyFrom(signature))
                    .build()
            );

        return this;
    }

    @Override
    public com.hedera.sdk.proto.Transaction toProto() {
        return inner.build();
    }

    private byte[] getBodyBytes() {
        if (bodyBytes == null) {
            bodyBytes = inner.getBody()
                .toByteArray();
        }

        return bodyBytes;
    }

    @Override
    protected MethodDescriptor<com.hedera.sdk.proto.Transaction, TransactionResponse> getMethod() {
        return methodDescriptor;
    }

    private AccountId getNodeAccountId() {
        return new AccountId(
                inner.getBody()
                    .getNodeAccountID()
        );
    }

    @Override
    protected Channel getChannel() {
        Objects.requireNonNull(client, "Transaction.client must be non-null in regular use");

        var channel = client.getNodeForId(getNodeAccountId());
        Objects.requireNonNull(channel, "Transaction.nodeAccountId not found on Client");

        return channel.getChannel();
    }

    @Override
    protected TransactionId mapResponse(TransactionResponse response) throws HederaException {
        HederaException.throwIfExceptional(response.getNodeTransactionPrecheckCode());
        return new TransactionId(
                inner.getBody()
                    .getTransactionIDOrBuilder()
        );
    }

    private <T> T executeAndWaitFor(CheckedFunction<TransactionId, T, HederaException> execute, Function<T, TransactionReceipt> mapReceipt)
            throws HederaException {
        var id = execute();

        T response = null;
        ResponseCodeEnum receiptStatus;
        final int MAX_ATTEMPTS = 10;

        for (int attempt = 1; attempt < MAX_ATTEMPTS; attempt++) {
            response = execute.apply(id);
            receiptStatus = mapReceipt.apply(response)
                .getStatus();

            if (receiptStatus == ResponseCodeEnum.UNKNOWN) {
                // If the receipt is UNKNOWN this means that the server has not finished
                // processing the transaction

                try {
                    Thread.sleep(500 * attempt);
                } catch (InterruptedException e) {
                    break;
                }
            } else {
                // Otherwise either the receipt is SUCCESS or there is something _exceptional_ wrong

                HederaException.throwIfExceptional(receiptStatus);
                break;
            }
        }

        return Objects.requireNonNull(response);
    }

    public final TransactionReceipt executeForReceipt() throws HederaException {
        return executeAndWaitFor(
            id -> new TransactionReceiptQuery(client).setTransactionId(id)
                .execute(),
            res -> res
        );
    }
}
