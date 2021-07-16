import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransferTransaction;
import com.hedera.hashgraph.sdk.proto.AccountAmount;
import com.hedera.hashgraph.sdk.proto.AccountID;
import com.hedera.hashgraph.sdk.proto.CryptoTransferTransactionBody;
import com.hedera.hashgraph.sdk.proto.Duration;
import com.hedera.hashgraph.sdk.proto.SignatureMap;
import com.hedera.hashgraph.sdk.proto.SignaturePair;
import com.hedera.hashgraph.sdk.proto.SignedTransaction;
import com.hedera.hashgraph.sdk.proto.Timestamp;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionID;
import com.hedera.hashgraph.sdk.proto.TransactionList;
import com.hedera.hashgraph.sdk.proto.TransferList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Disabled;

public class TransactionIntegrationTest {
    @Test
    @DisplayName("transaction hash in transaction record is equal to the derived transaction hash")
    void transactionHashInTransactionRecordIsEqualToTheDerivedTransactionHash() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();

            var key = PrivateKey.generate();

            var transaction = new AccountCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                //.setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .freezeWith(testEnv.client)
                .signWithOperator(testEnv.client);

            var expectedHash = transaction.getTransactionHashPerNode();

            var response = transaction.execute(testEnv.client);

            var record = response.getRecord(testEnv.client);

            assertArrayEquals(expectedHash.get(response.nodeId), record.transactionHash.toByteArray());

            var accountId = record.receipt.accountId;
            assertNotNull(accountId);

            testEnv.cleanUpAndClose(accountId, key);
        });
    }

    @Test
    @DisplayName("transaction can be serialized into bytes, deserialized, signature added and executed")
    void transactionFromToBytes() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();

            var key = PrivateKey.generate();

            var transaction = new AccountCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                .freezeWith(testEnv.client)
                .signWithOperator(testEnv.client);

            var expectedHash = transaction.getTransactionHashPerNode();

            var response = transaction.execute(testEnv.client);

            var record = response.getRecord(testEnv.client);

            assertArrayEquals(expectedHash.get(response.nodeId), record.transactionHash.toByteArray());

            var accountId = record.receipt.accountId;
            assertNotNull(accountId);

            var deleteTransaction = new AccountDeleteTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(accountId)
                .setTransferAccountId(testEnv.operatorId)
                .freezeWith(testEnv.client);

            var updateBytes = deleteTransaction.toBytes();

            var sig1 = key.signTransaction(deleteTransaction);

            var deleteTransaction2 = Transaction.fromBytes(updateBytes);

            response = deleteTransaction2
                .addSignature(key.getPublicKey(), sig1)
                .execute(testEnv.client);

            response.getReceipt(testEnv.client);

            testEnv.cleanUpAndClose();
        });
    }


    // TODO: this test has a bunch of things hard-coded into it, which is kinda dumb, but it's a good idea for a test.
    //       Any way to fix it and bring it back?

    @Disabled
    @Test
    @DisplayName("transaction can be serialized into bytes, deserialized, signature added and executed")
    void transactionFromToBytes2() {
        assertDoesNotThrow(() -> {
            var id = TransactionId.generate(new AccountId(542348));

            var transactionBodyBuilder = TransactionBody.newBuilder();
            transactionBodyBuilder
                .setTransactionID(TransactionID.newBuilder()
                    .setTransactionValidStart(Timestamp.newBuilder()
                        .setNanos(id.validStart.getNano())
                        .setSeconds(id.validStart.getEpochSecond())
                        .build())
                    .setAccountID(AccountID.newBuilder()
                        .setAccountNum(542348)
                        .setRealmNum(0)
                        .setShardNum(0)
                        .build())
                    .build())
                .setNodeAccountID(AccountID.newBuilder()
                    .setAccountNum(3)
                    .setRealmNum(0)
                    .setShardNum(0)
                    .build()
                )
                .setTransactionFee(200_000_000)
                .setTransactionValidDuration(
                    Duration.newBuilder()
                        .setSeconds(120)
                        .build()
                )
                .setGenerateRecord(false)
                .setMemo("")
                .setCryptoTransfer(
                    CryptoTransferTransactionBody.newBuilder()
                        .setTransfers(TransferList.newBuilder()
                            .addAccountAmounts(AccountAmount.newBuilder()
                                .setAccountID(AccountID.newBuilder()
                                    .setAccountNum(47439)
                                    .setRealmNum(0)
                                    .setShardNum(0)
                                    .build())
                                .setAmount(10)
                                .build())
                            .addAccountAmounts(AccountAmount.newBuilder()
                                .setAccountID(AccountID.newBuilder()
                                    .setAccountNum(542348)
                                    .setRealmNum(0)
                                    .setShardNum(0)
                                    .build())
                                .setAmount(-10)
                                .build())
                            .build())
                        .build());
            var bodyBytes = transactionBodyBuilder.build().toByteString();

            var key1 = PrivateKey.fromString("302e020100300506032b6570042204203e7fda6dde63c3cdb3cb5ecf5264324c5faad7c9847b6db093c088838b35a110");
            var key2 = PrivateKey.fromString("302e020100300506032b65700422042032d3d5a32e9d06776976b39c09a31fbda4a4a0208223da761c26a2ae560c1755");
            var key3 = PrivateKey.fromString("302e020100300506032b657004220420195a919056d1d698f632c228dbf248bbbc3955adf8a80347032076832b8299f9");
            var key4 = PrivateKey.fromString("302e020100300506032b657004220420b9962f17f94ffce73a23649718a11638cac4b47095a7a6520e88c7563865be62");
            var key5 = PrivateKey.fromString("302e020100300506032b657004220420fef68591819080cd9d48b0cbaa10f65f919752abb50ffb3e7411ac66ab22692e");

            var publicKey1 = key1.getPublicKey();
            var publicKey2 = key2.getPublicKey();
            var publicKey3 = key3.getPublicKey();
            var publicKey4 = key4.getPublicKey();
            var publicKey5 = key5.getPublicKey();

            var signature1 = key1.sign(bodyBytes.toByteArray());
            var signature2 = key2.sign(bodyBytes.toByteArray());
            var signature3 = key3.sign(bodyBytes.toByteArray());
            var signature4 = key4.sign(bodyBytes.toByteArray());
            var signature5 = key5.sign(bodyBytes.toByteArray());

            var signedBuilder = SignedTransaction.newBuilder();
            signedBuilder
                .setBodyBytes(bodyBytes)
                .setSigMap(SignatureMap.newBuilder()
                    .addSigPair(SignaturePair.newBuilder()
                        .setEd25519(ByteString.copyFrom(signature1))
                        .setPubKeyPrefix(ByteString.copyFrom(publicKey1.toBytes()))
                        .build())
                    .addSigPair(SignaturePair.newBuilder()
                        .setEd25519(ByteString.copyFrom(signature2))
                        .setPubKeyPrefix(ByteString.copyFrom(publicKey2.toBytes()))
                        .build())
                    .addSigPair(SignaturePair.newBuilder()
                        .setEd25519(ByteString.copyFrom(signature3))
                        .setPubKeyPrefix(ByteString.copyFrom(publicKey3.toBytes()))
                        .build())
                    .addSigPair(SignaturePair.newBuilder()
                        .setEd25519(ByteString.copyFrom(signature4))
                        .setPubKeyPrefix(ByteString.copyFrom(publicKey4.toBytes()))
                        .build())
                    .addSigPair(SignaturePair.newBuilder()
                        .setEd25519(ByteString.copyFrom(signature5))
                        .setPubKeyPrefix(ByteString.copyFrom(publicKey5.toBytes()))
                        .build())
                );
            var byts = signedBuilder.build().toByteString();

            byts = TransactionList.newBuilder()
                .addTransactionList(com.hedera.hashgraph.sdk.proto.Transaction.newBuilder()
                    .setSignedTransactionBytes(byts)
                    .build())
                .build().toByteString();

            var tx = (TransferTransaction)Transaction.fromBytes(byts.toByteArray());

            var testEnv = IntegrationTestEnv.withOneNode();

            assertEquals(tx.getHbarTransfers().get(new AccountId(542348)).toTinybars(),-10);
            assertEquals(tx.getHbarTransfers().get(new AccountId(47439)).toTinybars(),10);

            assertNotNull(tx.getNodeAccountIds());
            assertEquals(tx.getNodeAccountIds().size(), 1);
            assertEquals(new AccountId(3), tx.getNodeAccountIds().get(0));

            var signatures = tx.getSignatures();
            assertEquals(Arrays.toString(signatures.get(new AccountId(3)).get(publicKey1)), Arrays.toString(signature1));
            assertEquals(Arrays.toString(signatures.get(new AccountId(3)).get(publicKey2)), Arrays.toString(signature2));
            assertEquals(Arrays.toString(signatures.get(new AccountId(3)).get(publicKey3)), Arrays.toString(signature3));
            assertEquals(Arrays.toString(signatures.get(new AccountId(3)).get(publicKey4)), Arrays.toString(signature4));
            assertEquals(Arrays.toString(signatures.get(new AccountId(3)).get(publicKey5)), Arrays.toString(signature5));

            var resp = tx.execute(testEnv.client);

            resp.getReceipt(testEnv.client);

            testEnv.cleanUpAndClose();
        });
    }
}
