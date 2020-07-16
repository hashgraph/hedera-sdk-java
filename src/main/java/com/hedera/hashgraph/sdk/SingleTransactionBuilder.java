package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.TransactionResponse;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.function.Consumer;

@Internal
public abstract class SingleTransactionBuilder<T extends SingleTransactionBuilder<T>> extends TransactionBuilder<TransactionId, Transaction, T> {
    @Override
    public Transaction build(@Nullable Client client) throws LocalValidationException {
        if (client != null && bodyBuilder.getTransactionFee() == 0) {
            setMaxTransactionFee(client.getMaxTransactionFee());
        }

        if (!bodyBuilder.hasNodeAccountID()) {
            Node channel = client != null ? client.pickNode() : null;
            if (channel != null) {
                bodyBuilder.setNodeAccountID(channel.accountId.toProto());
            }
        }

        if (!bodyBuilder.hasTransactionID() && client != null
            && client.getOperatorId() != null)
        {
            bodyBuilder.setTransactionID(new TransactionId(client.getOperatorId()).toProto());
        }

        localValidate();

        inner.setBodyBytes(bodyBuilder.build().toByteString());

        return new Transaction(inner, bodyBuilder, getMethod());
    }

    @Override
    public TransactionId execute(Client client, Duration retryTimeout) throws HederaStatusException, HederaNetworkException {
        return build(client).execute(client, retryTimeout);
    }

    @Override
    public void executeAsync(Client client, Duration retryTimeout, Consumer<TransactionId> onSuccess, Consumer<HederaThrowable> onError) {
        build(client).executeAsync(client, retryTimeout, onSuccess, onError);
    }

    @Override
    public final com.hedera.hashgraph.proto.Transaction toProto() {
        return build(null).toProto();
    }

    @Internal
    public final com.hedera.hashgraph.proto.Transaction toProto(boolean requireSignature) {
        return build(null).toProto(requireSignature);
    }

    @Override
    protected TransactionId mapResponse(TransactionResponse response) throws HederaStatusException {
        TransactionId transactionId = new TransactionId(
            bodyBuilder.getTransactionIDOrBuilder());

        HederaPrecheckStatusException.throwIfExceptional(response.getNodeTransactionPrecheckCode(),
            transactionId);

        return transactionId;
    }
}
