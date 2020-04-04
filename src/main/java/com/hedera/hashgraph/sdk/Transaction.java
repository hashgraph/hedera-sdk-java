package com.hedera.hashgraph.sdk;

import static com.hedera.hashgraph.sdk.FutureConverter.toCompletableFuture;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.SignatureMap;
import com.hedera.hashgraph.sdk.proto.SignaturePair;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.CallOptions;
import io.grpc.stub.ClientCalls;
import java.util.ArrayList;
import java.util.List;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.BiConsumer;
import java8.util.function.Function;

// TODO: Handle multiple raw transactions

public final class Transaction {
    // A SDK [Transaction] is composed of multiple, raw protobuf transactions. These should be
    // functionally identical, with the exception of pointing to different nodes. When retrying a
    // transaction after a network error or retry-able status response, we try a
    // different transaction and thus a different node.
    private final List<com.hedera.hashgraph.sdk.proto.Transaction.Builder> transactions;

    // The parsed transaction body for the corresponding transaction.
    private final List<com.hedera.hashgraph.sdk.proto.TransactionBody> transactionBodies;

    // The signature builder for the corresponding transaction.
    private final List<SignatureMap.Builder> signatureBuilders;

    Transaction(List<com.hedera.hashgraph.sdk.proto.Transaction.Builder> transactions) {
        this.transactions = transactions;
        this.signatureBuilders = new ArrayList<>(transactions.size());
        this.transactionBodies = new ArrayList<>(transactions.size());

        for (var tx : transactions) {
            var bodyBytes = tx.getBodyBytes();
            TransactionBody body;

            try {
                body = TransactionBody.parseFrom(bodyBytes);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }

            transactionBodies.add(body);
            signatureBuilders.add(SignatureMap.newBuilder());
        }
    }

    public Transaction sign(PrivateKey privateKey) {
        return signWith(privateKey.getPublicKey(), privateKey::sign);
    }

    public Transaction signWith(PublicKey publicKey, Function<byte[], byte[]> transactionSigner) {
        var bodyBytes = transactions.get(0).getBodyBytes().toByteArray();
        var signatureBytes = transactionSigner.apply(bodyBytes);

        SignaturePair signature = publicKey.toSignaturePairProtobuf(signatureBytes);

        signatureBuilders.get(0).addSigPair(signature);

        return this;
    }

    // TODO: Return <TransactionId>
    public TransactionResponse execute(Client client) {
        return executeAsync(client).join();
    }

    // TODO: Return <TransactionId>
    public CompletableFuture<TransactionResponse> executeAsync(Client client) {
        var nodeId = AccountId.fromProtobuf(transactionBodies.get(0).getNodeAccountID());
        var method = CryptoServiceGrpc.getCreateAccountMethod();

        var channel = client.getChannel(nodeId);
        var call = channel.newCall(method, CallOptions.DEFAULT);

        return toCompletableFuture(ClientCalls.futureUnaryCall(call, toProtobuf()));
    }

    // TODO: Return <TransactionId>
    @SuppressWarnings("FutureReturnValueIgnored")
    public void executeAsync(Client client, BiConsumer<TransactionResponse, Throwable> callback) {
        executeAsync(client).whenComplete(callback);
    }

    private com.hedera.hashgraph.sdk.proto.Transaction toProtobuf() {
        transactions.get(0).setSigMap(signatureBuilders.get(0));
        return transactions.get(0).build();
    }
}
