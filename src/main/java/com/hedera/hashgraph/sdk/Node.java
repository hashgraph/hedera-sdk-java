package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.account.AccountId;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;

class Node {

    final AccountId accountId;
    final String address;

    // volatile is required for correct double-checked locking
    @Nullable
    private volatile ManagedChannel channel = null;

    Node(AccountId accountId, String address) {
        this.accountId = accountId;
        this.address = address;
    }

    ManagedChannel getChannel() {
        if (channel == null) {
            synchronized (this) {
                if (channel == null) {
                    channel = ManagedChannelBuilder.forTarget(address)
                        .usePlaintext()
                        .build();
                }
            }
        }

        return channel;
    }

    synchronized void healthCheck() throws HederaNetworkException, HederaStatusException {
        //noinspection ConstantConditions
        new Ping().execute(null);
    }

    void healthCheckAsync(Runnable onSuccess, Consumer<HederaThrowable> onError) {
        //noinspection ConstantConditions
        new Ping().executeAsync(null, (v) -> onSuccess.run(), onError);
    }

    void closeChannel() {
        // because `channel` is volatile, we have to explicitly load it so we can null-check it
        // otherwise it could be set to `null` between when we checked it and when we used it
        final ManagedChannel channel = this.channel;

        if (channel != null) {
            channel.shutdown();
        }
    }

    void awaitChannelTermination(long timeout, TimeUnit timeUnit) throws InterruptedException, TimeoutException {
        final ManagedChannel channel = this.channel;

        if (channel != null && channel.shutdown().awaitTermination(timeout, timeUnit)) {
            throw new TimeoutException("Timed out waiting for node channel to shutdown: "
                + accountId + " :: " + address);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node node = (Node) o;
        return accountId.equals(node.accountId) &&
            address.equals(node.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, address);
    }

    private final class Ping extends HederaCall<Query, Response, Void, Ping> {
        private final Query.Builder queryBuilder = Query.newBuilder();

        private Ping() {
            super();
            CryptoGetAccountBalanceQuery.Builder balanceQueryBuilder = queryBuilder
                .getCryptogetAccountBalanceBuilder();

            balanceQueryBuilder.setAccountID(accountId.toProto());

            // transaction doesn't need to be valid, just set
            balanceQueryBuilder.getHeaderBuilder().setPayment(
                new CryptoTransferTransaction()
                .addSender(new AccountId(2), 0)
                .addRecipient(accountId, 0)
                .setTransactionId(new TransactionId(accountId))
                .setNodeAccountId(accountId)
                .build(null)
                .toProto());
        }

        @Override
        protected AccountId getNodeId() {
            return accountId;
        }

        @Override
        protected Channel getChannel(Client client) {
            return Node.this.getChannel();
        }

        @Override
        protected Void mapResponse(Response raw) throws HederaStatusException {
            ResponseCodeEnum code = raw.getCryptogetAccountBalance().getHeader()
                .getNodeTransactionPrecheckCode();

            if (HederaStatusException.isCodeExceptional(code)) {
                throw new HederaStatusException(accountId, code);
            }

            return null;
        }

        @Override
        protected void localValidate() throws LocalValidationException {

        }

        @Override
        protected MethodDescriptor<Query, Response> getMethod() {
            return CryptoServiceGrpc.getCryptoGetBalanceMethod();
        }

        @Override
        public Query toProto() {
            return queryBuilder.build();
        }
    }
}
