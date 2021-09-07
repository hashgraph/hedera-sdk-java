package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.SignedTransaction;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MockingTest {

    @ParameterizedTest(name = "Executable retries on error with status {0} and description {1}")
    @CsvSource({
        "INTERNAL, internal RST_STREAM error",
        "INTERNAL, rst stream",
        "RESOURCE_EXHAUSTED, ",
        "UNAVAILABLE, "
    })
    void executableRetryTest(Status.Code code, String description) throws Exception {
        var service = new TestCryptoService();
        var server = new TestServer("executableRetry", service);

        var exception = Status.fromCode(code)
            .withDescription(description)
            .asRuntimeException();

        service.buffer
            .enqueueResponse(TestResponse.error(exception))
            .enqueueResponse(TestResponse.transactionOk());

        new AccountCreateTransaction()
            .execute(server.client);

        Assertions.assertEquals(2, service.buffer.transactionRequestsReceived.size());

        server.close();
    }

    @Test
    @DisplayName("Client.setDefaultMaxTransactionFee() functions correctly")
    void defaultMaxTransactionFeeTest() throws Exception {
        var service = new TestCryptoService();
        var server = new TestServer("maxTransactionFee", service);

        service.buffer
            .enqueueResponse(TestResponse.transactionOk())
            .enqueueResponse(TestResponse.transactionOk())
            .enqueueResponse(TestResponse.transactionOk())
            .enqueueResponse(TestResponse.transactionOk());

        new AccountCreateTransaction()
            .execute(server.client);

        new AccountCreateTransaction()
            .setMaxTransactionFee(new Hbar(5))
            .execute(server.client);

        server.client.setDefaultMaxTransactionFee(new Hbar(1));

        new AccountCreateTransaction()
            .execute(server.client);

        new AccountCreateTransaction()
            .setMaxTransactionFee(new Hbar(3))
            .execute(server.client);

        Assertions.assertEquals(4, service.buffer.transactionRequestsReceived.size());
        var transactions = new ArrayList<com.hedera.hashgraph.sdk.Transaction<?>>();
        for (var request : service.buffer.transactionRequestsReceived) {
            transactions.add(com.hedera.hashgraph.sdk.Transaction.fromBytes(request.toByteArray()));
        }
        Assertions.assertEquals(new Hbar(2), transactions.get(0).getMaxTransactionFee());
        Assertions.assertEquals(new Hbar(5), transactions.get(1).getMaxTransactionFee());
        Assertions.assertEquals(new Hbar(1), transactions.get(2).getMaxTransactionFee());
        Assertions.assertEquals(new Hbar(3), transactions.get(3).getMaxTransactionFee());

        server.close();
    }

    @Disabled
    @Test
    @DisplayName("Client.setDefaultMaxQueryPayment() functions correctly")
    void defaultMaxQueryPaymentTest() throws Exception {
        var service = new TestCryptoService();
        var server = new TestServer("queryPayment", service);

        var response = Response.newBuilder()
            .setCryptogetAccountBalance(
                new AccountBalance(
                    new Hbar(0),
                    new HashMap<TokenId, Long>(),
                    new HashMap<TokenId, Integer>()
                ).toProtobuf()
            ).build();

        service.buffer
            .enqueueResponse(TestResponse.query(response))
            .enqueueResponse(TestResponse.query(response))
            .enqueueResponse(TestResponse.query(response));

        // TODO: this will take some work, since I have to contend with Query's getCost behavior
        // TODO: actually, because AccountBalanceQuery is free, I'll need some other query type to test this.
        //       Perhaps getAccountInfo?

        server.close();
    }

    @Test
    @DisplayName("Signer is prevented from signing twice")
    void signerDoesNotSignTwice() throws Exception {
        var service = new TestCryptoService();
        var server = new TestServer("signerDoesNotSignTwice", service);

        service.buffer.enqueueResponse(TestResponse.transactionOk());

        var aliceKey = PrivateKey.generate();

        var transaction = new AccountCreateTransaction()
            .setTransactionId(TransactionId.generate(Objects.requireNonNull(server.client.getOperatorAccountId())))
            .setNodeAccountIds(server.client.network.getNodeAccountIdsForExecute())
            .freeze()
            .sign(aliceKey);

        // This will cause the SDK Transaction to populate the sigPairLists list
        transaction.getTransactionHashPerNode();

        // This will clear the outerTransactions list while keeping the sigPairLists list
        transaction.signWithOperator(server.client);

        // If Transaction.signTransaction() is not programmed correctly, it will add Alice's signature to the
        // sigPairList a second time here.
        transaction.execute(server.client);

        // Now we must go through the laborious process of digging info out of the response.  =(
        Assertions.assertEquals(1, service.buffer.transactionRequestsReceived.size());
        var request = service.buffer.transactionRequestsReceived.get(0);
        var sigPairList = SignedTransaction.parseFrom(request.getSignedTransactionBytes()).getSigMap().getSigPairList();
        Assertions.assertEquals(2, sigPairList.size());
        Assertions.assertNotEquals(
            sigPairList.get(0).getEd25519().toString(),
            sigPairList.get(1).getEd25519().toString());

        server.close();
    }

    private static class TestCryptoService extends CryptoServiceGrpc.CryptoServiceImplBase implements TestService {
        public Buffer buffer = new Buffer();

        @Override
        public Buffer getBuffer() {
            return buffer;
        }

        @Override
        public void createAccount(Transaction request, StreamObserver<TransactionResponse> responseObserver) {
            respondToTransactionFromQueue(request, responseObserver);
        }

        @Override
        public void cryptoGetBalance(Query request, StreamObserver<Response> responseObserver) {
            respondToQueryFromQueue(request, responseObserver);
        }
    }
}

