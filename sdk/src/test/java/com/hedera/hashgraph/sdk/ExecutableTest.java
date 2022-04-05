package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import java8.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExecutableTest {
    Client client;
    Network network;
    Node node3, node4, node5;
    List<AccountId> nodeAccountIds;

    @BeforeEach
    void setup() {
        client = Client.forMainnet();
        network = mock(Network.class);
        client.network = network;

        node3 = mock(Node.class);
        node4 = mock(Node.class);
        node5 = mock(Node.class);

        when(node3.getAccountId()).thenReturn(new AccountId(3));
        when(node4.getAccountId()).thenReturn(new AccountId(4));
        when(node5.getAccountId()).thenReturn(new AccountId(5));
        when(network.getNode(new AccountId(3))).thenReturn(node3);
        when(network.getNode(new AccountId(4))).thenReturn(node4);
        when(network.getNode(new AccountId(5))).thenReturn(node5);

        nodeAccountIds = new ArrayList<AccountId>() {{
            add(new AccountId(3));
            add(new AccountId(4));
            add(new AccountId(5));
        }};
    }

    @Test
    void firstNodeHealthy() {
        when(node3.isHealthy()).thenReturn(true);

        var tx = new DummyTransaction();
        tx.setNodeAccountIds(nodeAccountIds);
        tx.setNodesFromNodeAccountIds(client);
        tx.setMinBackoff(Duration.ofMillis(10));
        tx.setMaxBackoff(Duration.ofMillis(1000));

        var node = tx.getNodeForExecute(1);
        assertEquals(node3, node);
    }


    @Test
    void calloptionsShouldRespectGrpcDeadline() {
        when(node3.isHealthy()).thenReturn(true);

        var tx = new DummyTransaction();
        tx.setNodeAccountIds(nodeAccountIds);
        tx.setNodesFromNodeAccountIds(client);
        tx.setMinBackoff(Duration.ofMillis(10));
        tx.setMaxBackoff(Duration.ofMillis(1000));
        tx.setGrpcDeadline(Duration.ofSeconds(10));

        var grpcRequest = tx.getGrpcRequest(1);

        var timeRemaining = grpcRequest.getCallOptions().getDeadline().timeRemaining(TimeUnit.MILLISECONDS);
        Assertions.assertTrue(timeRemaining < 10000);
        Assertions.assertTrue(timeRemaining > 9000);
    }

    @Test
    void multipleNodesUnhealthy() {
        when(node3.isHealthy()).thenReturn(false);
        when(node4.isHealthy()).thenReturn(true);

        when(node3.getRemainingTimeForBackoff()).thenReturn(1000L);

        var tx = new DummyTransaction();
        tx.setNodeAccountIds(nodeAccountIds);
        tx.setNodesFromNodeAccountIds(client);
        tx.setMinBackoff(Duration.ofMillis(10));
        tx.setMaxBackoff(Duration.ofMillis(1000));

        var node = tx.getNodeForExecute(1);
        assertEquals(node4, node);
    }

    @Test
    void allNodesUnhealthy() {
        when(node3.isHealthy()).thenReturn(false);
        when(node4.isHealthy()).thenReturn(false);
        when(node5.isHealthy()).thenReturn(false);

        when(node3.getRemainingTimeForBackoff()).thenReturn(4000L);
        when(node4.getRemainingTimeForBackoff()).thenReturn(3000L);
        when(node5.getRemainingTimeForBackoff()).thenReturn(5000L);

        var tx = new DummyTransaction();
        tx.setNodeAccountIds(nodeAccountIds);
        tx.setNodesFromNodeAccountIds(client);
        tx.setMinBackoff(Duration.ofMillis(10));
        tx.setMaxBackoff(Duration.ofMillis(1000));
        tx.nodeAccountIds.setIndex(1);

        var node = tx.getNodeForExecute(1);
        assertEquals(node4, node);
    }

    @Test
    void multipleRequestsWithSingleHealthyNode() {
        when(node3.isHealthy()).thenReturn(true);
        when(node4.isHealthy()).thenReturn(false);
        when(node5.isHealthy()).thenReturn(false);

        when(node4.getRemainingTimeForBackoff()).thenReturn(4000L);
        when(node5.getRemainingTimeForBackoff()).thenReturn(3000L);

        var tx = new DummyTransaction();
        tx.setNodeAccountIds(nodeAccountIds);
        tx.setNodesFromNodeAccountIds(client);
        tx.setMinBackoff(Duration.ofMillis(10));
        tx.setMaxBackoff(Duration.ofMillis(1000));

        var node = tx.getNodeForExecute(1);
        assertEquals(node3, node);
        tx.nodeAccountIds.advance();

        node = tx.getNodeForExecute(2);
        assertEquals(node3, node);
        verify(node4).getRemainingTimeForBackoff();
        verify(node5).getRemainingTimeForBackoff();
    }

    @Test
    void multipleRequestsWithNoHealthyNodes() {
        AtomicInteger i = new AtomicInteger();

        when(node3.isHealthy()).thenReturn(false);
        when(node4.isHealthy()).thenReturn(false);
        when(node5.isHealthy()).thenReturn(false);

        long[] node3Times = {4000, 3000, 1000};
        long[] node4Times = {3000, 1000, 4000};
        long[] node5Times = {1000, 3000, 4000};

        when(node3.getRemainingTimeForBackoff()).thenAnswer((Answer<Long>) invocation -> node3Times[i.get()]);
        when(node4.getRemainingTimeForBackoff()).thenAnswer((Answer<Long>) invocation -> node4Times[i.get()]);
        when(node5.getRemainingTimeForBackoff()).thenAnswer((Answer<Long>) invocation -> node5Times[i.get()]);

        var tx = new DummyTransaction();
        tx.setNodeAccountIds(nodeAccountIds);
        tx.setNodesFromNodeAccountIds(client);
        tx.setMinBackoff(Duration.ofMillis(10));
        tx.setMaxBackoff(Duration.ofMillis(1000));

        var node = tx.getNodeForExecute(1);
        assertEquals(node5, node);
        i.incrementAndGet();

        node = tx.getNodeForExecute(2);
        assertEquals(node4, node);
        i.incrementAndGet();

        node = tx.getNodeForExecute(3);
        assertEquals(node3, node);
    }

    @Test
    void successfulExecute() throws PrecheckStatusException, TimeoutException {
        var now = org.threeten.bp.Instant.now();
        var tx = new DummyTransaction() {
            @Nullable
            @Override
            TransactionResponse mapResponse(com.hedera.hashgraph.sdk.proto.TransactionResponse response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Transaction request) {
                return new TransactionResponse(
                    new AccountId(3),
                    TransactionId.withValidStart(new AccountId(3), now),
                    null,
                    null);
            }
        };

        var nodeAccountIds = new ArrayList<AccountId>() {{
            add(new AccountId(3));
            add(new AccountId(4));
            add(new AccountId(5));
        }};
        tx.setNodeAccountIds(nodeAccountIds);

        var txResp =
            com.hedera.hashgraph.sdk.proto.TransactionResponse
                .newBuilder()
                .setNodeTransactionPrecheckCode(ResponseCodeEnum.OK)
                .build();

        tx.blockingUnaryCall = (grpcRequest) -> txResp;
        com.hedera.hashgraph.sdk.TransactionResponse resp = (com.hedera.hashgraph.sdk.TransactionResponse) tx.execute(client);

        assertEquals(new AccountId(3), resp.nodeId);
    }

    @Test
    void executeWithChannelFailure() throws PrecheckStatusException, TimeoutException {
        when(node3.isHealthy()).thenReturn(true);
        when(node4.isHealthy()).thenReturn(true);

        when(node3.channelFailedToConnect()).thenReturn(true);
        when(node4.channelFailedToConnect()).thenReturn(false);

        var now = org.threeten.bp.Instant.now();
        var tx = new DummyTransaction() {
            @Nullable
            @Override
            TransactionResponse mapResponse(com.hedera.hashgraph.sdk.proto.TransactionResponse response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Transaction request) {
                return new TransactionResponse(
                    new AccountId(4),
                    TransactionId.withValidStart(new AccountId(4), now),
                    null,
                    null);
            }
        };

        var nodeAccountIds = new ArrayList<AccountId>() {{
            add(new AccountId(3));
            add(new AccountId(4));
            add(new AccountId(5));
        }};
        tx.setNodeAccountIds(nodeAccountIds);

        var txResp =
            com.hedera.hashgraph.sdk.proto.TransactionResponse
                .newBuilder()
                .setNodeTransactionPrecheckCode(ResponseCodeEnum.OK)
                .build();

        tx.blockingUnaryCall = (grpcRequest) -> txResp;
        com.hedera.hashgraph.sdk.TransactionResponse resp = (com.hedera.hashgraph.sdk.TransactionResponse) tx.execute(client);

        verify(node3).channelFailedToConnect();
        verify(node4).channelFailedToConnect();
        assertEquals(new AccountId(4), resp.nodeId);
    }

    @Test
    void executeWithAllUnhealthyNodes() throws PrecheckStatusException, TimeoutException {
        AtomicInteger i = new AtomicInteger();

        // 1st round, pick node3, fail channel connect
        // 2nd round, pick node4, fail channel connect
        // 3rd round, pick node5, fail channel connect
        // 4th round, pick node 3, wait for delay, channel connect ok
        when(node3.isHealthy()).thenAnswer((Answer<Boolean>) inv -> i.get() == 0);
        when(node4.isHealthy()).thenAnswer((Answer<Boolean>) inv -> i.get() == 0);
        when(node5.isHealthy()).thenAnswer((Answer<Boolean>) inv -> i.get() == 0);

        when(node3.channelFailedToConnect()).thenAnswer((Answer<Boolean>) inv -> i.get() == 0);
        when(node4.channelFailedToConnect()).thenAnswer((Answer<Boolean>) inv -> i.get() == 0);
        when(node5.channelFailedToConnect()).thenAnswer((Answer<Boolean>) inv -> i.getAndIncrement() == 0);

        when(node3.getRemainingTimeForBackoff()).thenReturn(500L);
        when(node4.getRemainingTimeForBackoff()).thenReturn(600L);
        when(node5.getRemainingTimeForBackoff()).thenReturn(700L);

        var now = org.threeten.bp.Instant.now();
        var tx = new DummyTransaction() {
            @Nullable
            @Override
            TransactionResponse mapResponse(com.hedera.hashgraph.sdk.proto.TransactionResponse response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Transaction request) {
                return new TransactionResponse(
                    new AccountId(3),
                    TransactionId.withValidStart(new AccountId(3), now),
                    null,
                    null);
            }
        };

        var nodeAccountIds = new ArrayList<AccountId>() {{
            add(new AccountId(3));
            add(new AccountId(4));
            add(new AccountId(5));
        }};
        tx.setNodeAccountIds(nodeAccountIds);

        var txResp =
            com.hedera.hashgraph.sdk.proto.TransactionResponse
                .newBuilder()
                .setNodeTransactionPrecheckCode(ResponseCodeEnum.OK)
                .build();

        tx.blockingUnaryCall = (grpcRequest) -> txResp;
        com.hedera.hashgraph.sdk.TransactionResponse resp = (com.hedera.hashgraph.sdk.TransactionResponse) tx.execute(client);

        verify(node3, times(2)).channelFailedToConnect();
        verify(node4).channelFailedToConnect();
        verify(node5).channelFailedToConnect();
        assertEquals(new AccountId(3), resp.nodeId);
    }

    @Test
    void executeExhaustRetries() {
        AtomicInteger i = new AtomicInteger();

        when(node3.isHealthy()).thenReturn(true);
        when(node4.isHealthy()).thenReturn(true);
        when(node5.isHealthy()).thenReturn(true);

        when(node3.channelFailedToConnect()).thenReturn(true);
        when(node4.channelFailedToConnect()).thenReturn(true);
        when(node5.channelFailedToConnect()).thenReturn(true);

        var tx = new DummyTransaction();
        var nodeAccountIds = new ArrayList<AccountId>() {{
            add(new AccountId(3));
            add(new AccountId(4));
            add(new AccountId(5));
        }};
        tx.setNodeAccountIds(nodeAccountIds);
        assertThrows(MaxAttemptsExceededException.class, () -> tx.execute(client));
    }

    @Test
    void executeRetriableErrorDuringCall() {
        AtomicInteger i = new AtomicInteger();

        when(node3.isHealthy()).thenReturn(true);
        when(node4.isHealthy()).thenReturn(true);

        when(node3.channelFailedToConnect()).thenReturn(false);
        when(node4.channelFailedToConnect()).thenReturn(false);

        var tx = new DummyTransaction();
        var nodeAccountIds = new ArrayList<AccountId>() {{
            add(new AccountId(3));
            add(new AccountId(4));
            add(new AccountId(5));
        }};
        tx.setNodeAccountIds(nodeAccountIds);

        var txResp =
            com.hedera.hashgraph.sdk.proto.TransactionResponse
                .newBuilder()
                .setNodeTransactionPrecheckCode(ResponseCodeEnum.OK)
                .build();

        tx.blockingUnaryCall = (grpcRequest) -> {
            if (i.getAndIncrement() == 0)
                throw new StatusRuntimeException(io.grpc.Status.UNAVAILABLE);
            else
                throw new StatusRuntimeException(io.grpc.Status.ABORTED);
        };

        assertThrows(RuntimeException.class, () -> tx.execute(client));

        verify(node3).channelFailedToConnect();
        verify(node4).channelFailedToConnect();
    }

    @Test
    void executeQueryDelay() throws PrecheckStatusException, TimeoutException {
        when(node3.isHealthy()).thenReturn(true);
        when(node4.isHealthy()).thenReturn(true);

        when(node3.channelFailedToConnect()).thenReturn(false);
        when(node4.channelFailedToConnect()).thenReturn(false);

        AtomicInteger i = new AtomicInteger();
        var tx = new DummyQuery() {
            @Override
            Status mapResponseStatus(com.hedera.hashgraph.sdk.proto.Response response) {
                return Status.RECEIPT_NOT_FOUND;
            }

            @Override
            ExecutionState shouldRetry(Status status, Response response) {
                return i.getAndIncrement() == 0 ? ExecutionState.Retry : ExecutionState.Success;
            }
        };
        var nodeAccountIds = new ArrayList<AccountId>() {{
            add(new AccountId(3));
            add(new AccountId(4));
            add(new AccountId(5));
        }};
        tx.setNodeAccountIds(nodeAccountIds);

        var receipt = com.hedera.hashgraph.sdk.proto.TransactionReceipt.newBuilder()
            .setStatus(ResponseCodeEnum.OK)
            .build();
        var receiptResp = com.hedera.hashgraph.sdk.proto.TransactionGetReceiptResponse.newBuilder()
            .setReceipt(receipt)
            .build();

        var resp = Response.newBuilder().setTransactionGetReceipt(receiptResp).build();
        tx.blockingUnaryCall = (grpcRequest) -> resp;
        TransactionReceipt rcp = (TransactionReceipt) tx.execute(client);

        verify(node3).channelFailedToConnect();
        verify(node4).channelFailedToConnect();
    }


    @Test
    void executeUserError() throws PrecheckStatusException, TimeoutException {
        when(node3.isHealthy()).thenReturn(true);
        when(node3.channelFailedToConnect()).thenReturn(false);

        var tx = new DummyTransaction() {
            @Override
            Status mapResponseStatus(com.hedera.hashgraph.sdk.proto.TransactionResponse response) {
                return Status.ACCOUNT_DELETED;
            }
        };
        var nodeAccountIds = new ArrayList<AccountId>() {{
            add(new AccountId(3));
            add(new AccountId(4));
            add(new AccountId(5));
        }};
        tx.setNodeAccountIds(nodeAccountIds);

        var txResp =
            com.hedera.hashgraph.sdk.proto.TransactionResponse
                .newBuilder()
                .setNodeTransactionPrecheckCode(ResponseCodeEnum.ACCOUNT_DELETED)
                .build();

        tx.blockingUnaryCall = (grpcRequest) -> txResp;
        assertThrows(PrecheckStatusException.class, () -> tx.execute(client));

        verify(node3).channelFailedToConnect();
    }

    @Test
    void shouldRetryReturnsCorrectStates() {
        var tx = new DummyTransaction();

        assertEquals(ExecutionState.ServerError, tx.shouldRetry(Status.PLATFORM_TRANSACTION_NOT_CREATED, null));
        assertEquals(ExecutionState.ServerError, tx.shouldRetry(Status.PLATFORM_NOT_ACTIVE, null));
        assertEquals(ExecutionState.ServerError, tx.shouldRetry(Status.BUSY, null));
        assertEquals(ExecutionState.Success, tx.shouldRetry(Status.OK, null));
        assertEquals(ExecutionState.RequestError, tx.shouldRetry(Status.ACCOUNT_DELETED, null));
    }

    static class DummyTransaction<T extends Transaction<T>>
        extends Executable<T, com.hedera.hashgraph.sdk.proto.Transaction, com.hedera.hashgraph.sdk.proto.TransactionResponse, com.hedera.hashgraph.sdk.TransactionResponse> {

        @Override
        void onExecute(Client client) {
        }

        @Nullable
        @Override
        CompletableFuture<Void> onExecuteAsync(Client client) {
            return null;
        }

        @Nullable
        @Override
        com.hedera.hashgraph.sdk.proto.Transaction makeRequest() {
            return null;
        }

        @Nullable
        @Override
        TransactionResponse mapResponse(com.hedera.hashgraph.sdk.proto.TransactionResponse response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Transaction request) {
            return null;
        }

        @Override
        Status mapResponseStatus(com.hedera.hashgraph.sdk.proto.TransactionResponse response) {
            return Status.OK;
        }

        @Nullable
        @Override
        MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, com.hedera.hashgraph.sdk.proto.TransactionResponse> getMethodDescriptor() {
            return null;
        }

        @Nullable
        @Override
        TransactionId getTransactionIdInternal() {
            return null;
        }
    }

    static class DummyQuery extends Query<TransactionReceipt, TransactionReceiptQuery> {
        @Override
        void onExecute(Client client) {
        }

        @Override
        TransactionReceipt mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request) {
            return null;
        }

        @Override
        Status mapResponseStatus(com.hedera.hashgraph.sdk.proto.Response response) {
            return Status.OK;
        }

        @Override
        MethodDescriptor<com.hedera.hashgraph.sdk.proto.Query, Response> getMethodDescriptor() {
            return null;
        }

        @Override
        void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        }

        @Override
        ResponseHeader mapResponseHeader(Response response) {
            return null;
        }

        @Override
        QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
            return null;
        }

        @Override
        void validateChecksums(Client client) {
        }
    }
}
