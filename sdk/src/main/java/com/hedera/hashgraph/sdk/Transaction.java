package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.FileServiceGrpc;
import com.hedera.hashgraph.sdk.proto.SignatureMap;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import com.hedera.hashgraph.sdk.proto.SystemDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.SystemUndeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hederahashgraph.service.proto.java.FreezeServiceGrpc;
import io.grpc.MethodDescriptor;
import java8.util.Lists;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Transaction extends HederaExecutable<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse, TransactionId> {
    public final TransactionId id;

    // A SDK [Transaction] is composed of multiple, raw protobuf transactions. These should be
    // functionally identical, with the exception of pointing to different nodes. When retrying a
    // transaction after a network error or retry-able status response, we try a
    // different transaction and thus a different node.
    private final List<com.hedera.hashgraph.sdk.proto.Transaction.Builder> transactions;

    // The parsed transaction body for the corresponding transaction.
    private final List<com.hedera.hashgraph.sdk.proto.TransactionBody> transactionBodies;

    // The signature builder for the corresponding transaction.
    private final List<SignatureMap.Builder> signatureBuilders;

    // The index of the _next_ transaction to be built and executed.
    // Each time `buildNext` is invoked, this should be incremented by 1 and wrapped around with the
    // size
    // of the transaction array.
    private int nextIndex = 0;

    Transaction(List<com.hedera.hashgraph.sdk.proto.Transaction.Builder> transactions) {
        this.transactions = transactions;
        this.signatureBuilders = new ArrayList<>(transactions.size());
        this.transactionBodies = new ArrayList<>(transactions.size());

        if (transactions.isEmpty()) {
            throw new IllegalArgumentException(
                "SDK transaction must have at least 1 protobuf transaction");
        }

        // For each transaction constructed from the builder we need to
        // prepare a builder for its signature map and parse its transaction body
        @Var TransactionId transactionId = null;

        for (var tx : transactions) {
            var bodyBytes = tx.getBodyBytes();
            TransactionBody body;

            try {
                body = TransactionBody.parseFrom(bodyBytes);
            } catch (InvalidProtocolBufferException e) {
                // this should be unreachable from downstream as all methods that hit here are
                // private
                throw new RuntimeException(e);
            }

            transactionBodies.add(body);

            if (tx.hasSigMap()) {
                signatureBuilders.add(tx.getSigMap().toBuilder());
            } else {
                signatureBuilders.add(SignatureMap.newBuilder());
            }

            if (transactionId == null) {
                transactionId = TransactionId.fromProtobuf(body.getTransactionID());
            }
        }

        // There should only be one, shared transaction ID between each transaction
        this.id = Objects.requireNonNull(transactionId);
    }

    public static Transaction fromBytes(byte[] bytes) throws Exception {
        com.hedera.hashgraph.sdk.proto.Transaction inner = com.hedera.hashgraph.sdk.proto.Transaction.parseFrom(bytes);

        return new Transaction(Lists.of(inner.toBuilder()));
    }

    private static MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptorForSystemDelete(SystemDeleteTransactionBody.IdCase idCase) {
        switch (idCase) {
            case FILEID:
                return FileServiceGrpc.getSystemUndeleteMethod();

            case CONTRACTID:
                return SmartContractServiceGrpc.getSystemUndeleteMethod();

            case ID_NOT_SET:
                // Should be caught elsewhere
                throw new IllegalStateException("SystemDeleteTransaction requires an ID to be set");
        }

        // Should be impossible as Error Prone does enum exhaustiveness checks
        throw new IllegalStateException("(unreachable) id case unhandled");
    }

    private static MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptorForSystemUndelete(SystemUndeleteTransactionBody.IdCase idCase) {
        switch (idCase) {
            case FILEID:
                return FileServiceGrpc.getSystemUndeleteMethod();

            case CONTRACTID:
                return SmartContractServiceGrpc.getSystemUndeleteMethod();

            case ID_NOT_SET:
                // Should be caught elsewhere
                throw new IllegalStateException(
                    "SystemUndeleteTransaction requires an ID to be set");
        }

        // Should be impossible as Error Prone does enum exhaustiveness checks
        throw new IllegalStateException("(unreachable) id case unhandled");
    }

    public final Transaction sign(PrivateKey privateKey) {
        return signWith(privateKey.getPublicKey(), privateKey::sign);
    }

    public final Transaction signWith(PublicKey publicKey, Function<byte[], byte[]> transactionSigner) {
        for (var index = 0; index < transactions.size(); ++index) {
            var bodyBytes = transactions.get(index).getBodyBytes().toByteArray();

            // NOTE: Yes the transactionSigner is invoked N times
            //  However for a verified/pin signature system it is reasonable to allow it to sign
            // multiple
            //  transactions with identical details apart from the node ID
            var signatureBytes = transactionSigner.apply(bodyBytes);

            signatureBuilders
                .get(index)
                .addSigPair(publicKey.toSignaturePairProtobuf(signatureBytes));
        }

        return this;
    }

    @Override
    protected CompletableFuture<Void> onExecuteAsync(Client client) {
        // On execute, sign each transaction with the operator, if present
        var operator = client.getOperator();
        if (operator != null) signWith(operator.publicKey, operator.transactionSigner);

        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected final AccountId getNodeId(Client client) {
        return AccountId.fromProtobuf(transactionBodies.get(nextIndex).getNodeAccountID());
    }

    @Override
    protected final TransactionId getTransactionId() {
        return id;
    }

    public byte[] toBytes() {
        return transactions.get(0).clone().setSigMap(signatureBuilders.get(0)).build().toByteArray();
    }

    @Override
    protected final MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        var transactionBody = transactionBodies.get(nextIndex);
        switch (transactionBody.getDataCase()) {
            case CONTRACTCALL:
                return SmartContractServiceGrpc.getContractCallMethodMethod();

            case CONTRACTCREATEINSTANCE:
                return SmartContractServiceGrpc.getCreateContractMethod();

            case CONTRACTUPDATEINSTANCE:
                return SmartContractServiceGrpc.getUpdateContractMethod();

            case CONTRACTDELETEINSTANCE:
                return SmartContractServiceGrpc.getDeleteContractMethod();

            case CRYPTOADDCLAIM:
                return CryptoServiceGrpc.getAddClaimMethod();

            case CRYPTOCREATEACCOUNT:
                return CryptoServiceGrpc.getCreateAccountMethod();

            case CRYPTODELETE:
                return CryptoServiceGrpc.getCryptoDeleteMethod();

            case CRYPTODELETECLAIM:
                return CryptoServiceGrpc.getDeleteClaimMethod();

            case CRYPTOTRANSFER:
                return CryptoServiceGrpc.getCryptoTransferMethod();

            case CRYPTOUPDATEACCOUNT:
                return CryptoServiceGrpc.getUpdateAccountMethod();

            case FILEAPPEND:
                return FileServiceGrpc.getAppendContentMethod();

            case FILECREATE:
                return FileServiceGrpc.getCreateFileMethod();

            case FILEDELETE:
                return FileServiceGrpc.getDeleteFileMethod();

            case FILEUPDATE:
                return FileServiceGrpc.getUpdateFileMethod();

            case SYSTEMDELETE:
                return getMethodDescriptorForSystemDelete(
                    transactionBody.getSystemDelete().getIdCase());

            case SYSTEMUNDELETE:
                return getMethodDescriptorForSystemUndelete(
                    transactionBody.getSystemUndelete().getIdCase());

            case FREEZE:
                return FreezeServiceGrpc.getFreezeMethod();

            case CONSENSUSCREATETOPIC:
                return ConsensusServiceGrpc.getCreateTopicMethod();

            case CONSENSUSUPDATETOPIC:
                return ConsensusServiceGrpc.getUpdateTopicMethod();

            case CONSENSUSDELETETOPIC:
                return ConsensusServiceGrpc.getDeleteTopicMethod();

            case CONSENSUSSUBMITMESSAGE:
                return ConsensusServiceGrpc.getSubmitMessageMethod();

            case DATA_NOT_SET:
                // Should be impossible to happen
                throw new IllegalStateException("transaction does not have any data");
        }

        // Should be impossible as Error Prone does enum exhaustiveness checks
        throw new IllegalStateException("(unreachable) transaction type unhandled");
    }

    @Override
    protected final com.hedera.hashgraph.sdk.proto.Transaction makeRequest() {
        // emplace the signatures on the transaction and build the transaction
        var tx = transactions.get(nextIndex).setSigMap(signatureBuilders.get(nextIndex)).build();

        // each time buildNext is called we move our cursor to the next transaction
        // wrapping around to ensure we are cycling
        nextIndex = (nextIndex + 1) % transactions.size();

        return tx;
    }

    @Override
    protected final TransactionId mapResponse(TransactionResponse transactionResponse) {
        return id;
    }

    @Override
    protected final Status mapResponseStatus(TransactionResponse transactionResponse) {
        return Status.valueOf(transactionResponse.getNodeTransactionPrecheckCode());
    }

    @Override
    @SuppressWarnings("LiteProtoToString")
    protected String debugToString(com.hedera.hashgraph.sdk.proto.Transaction request) {
        try {
            return TransactionBody.parseFrom(request.getBodyBytes()).toString();
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
