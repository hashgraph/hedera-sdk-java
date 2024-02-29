import com.google.errorprone.annotations.Var;
import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.FileAppendTransaction;
import com.hedera.hashgraph.sdk.FileContentsQuery;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileDeleteTransaction;
import com.hedera.hashgraph.sdk.FileInfoQuery;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.KeyList;
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
import java.util.Objects;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

public class TransactionIntegrationTest {

    @Test
    @DisplayName("transaction hash in transaction record is equal to the derived transaction hash")
    void transactionHashInTransactionRecordIsEqualToTheDerivedTransactionHash() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var key = PrivateKey.generateED25519();

        var transaction = new AccountCreateTransaction()
            .setKey(key)
            .freezeWith(testEnv.client)
            .signWithOperator(testEnv.client);

        var expectedHash = transaction.getTransactionHashPerNode();

        var response = transaction.execute(testEnv.client);

        var record = response.getRecord(testEnv.client);

        assertThat(expectedHash.get(response.nodeId)).containsExactly(record.transactionHash.toByteArray());

        var accountId = record.receipt.accountId;
        assertThat(accountId).isNotNull();

        var transactionId = transaction.getTransactionId();
        assertThat(transactionId.getReceipt(testEnv.client)).isNotNull();
        assertThat(transactionId.getReceiptAsync(testEnv.client).get()).isNotNull();
        assertThat(transactionId.getRecord(testEnv.client)).isNotNull();
        assertThat(transactionId.getRecordAsync(testEnv.client).get()).isNotNull();

        testEnv.close(accountId, key);
    }

    @Test
    @DisplayName("transaction can be serialized into bytes, deserialized and be equal to the original one")
    void canSerializeDeserializeCompareFields() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var adminKey = PrivateKey.generateECDSA();
        var publicKey = adminKey.getPublicKey();

        var createAccountTransaction = new AccountCreateTransaction()
            .setKey(publicKey)
            .setInitialBalance(new Hbar(5L));

        var expectedNodeAccountIds = createAccountTransaction.getNodeAccountIds();
        var expectedBalance = new Hbar(5L);

        var transactionBytesSerialized = createAccountTransaction.toBytes();
        AccountCreateTransaction createAccountTransactionDeserialized = (AccountCreateTransaction) Transaction.fromBytes(transactionBytesSerialized);

        assertThat(expectedNodeAccountIds).isEqualTo(createAccountTransactionDeserialized.getNodeAccountIds());
        assertThat(expectedBalance).isEqualTo(createAccountTransactionDeserialized.getInitialBalance());
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
            createAccountTransactionDeserialized::getTransactionId);

        testEnv.close();
    }

    @Test
    @DisplayName("incomplete transaction can be serialized into bytes, deserialized, signature added and executed")
    void canIncompleteSerializeDeserializeAndExecute() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var adminKey = PrivateKey.generateECDSA();
        var publicKey = adminKey.getPublicKey();

        var createAccountTransaction = new AccountCreateTransaction()
            .setKey(publicKey)
            .setInitialBalance(new Hbar(5L));

        var transactionBytesSerialized = createAccountTransaction.toBytes();
        AccountCreateTransaction createAccountTransactionDeserialized = (AccountCreateTransaction) Transaction.fromBytes(transactionBytesSerialized);

        var txReceipt = createAccountTransactionDeserialized
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new AccountDeleteTransaction()
            .setAccountId(txReceipt.accountId)
            .setTransferAccountId(testEnv.client.getOperatorAccountId())
            .freezeWith(testEnv.client)
            .sign(adminKey)
            .execute(testEnv.client);
    }

    @Test
    @DisplayName("transaction can be serialized into bytes, deserialized, edited and executed")
    void canSerializeDeserializeEditExecuteCompareFields() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var adminKey = PrivateKey.generateECDSA();
        var publicKey = adminKey.getPublicKey();

        var createAccountTransaction = new AccountCreateTransaction()
            .setKey(publicKey);

        var expectedBalance = new Hbar(5L);
        var nodeAccountIds = testEnv.client.getNetwork().values().stream().toList();

        var transactionBytesSerialized = createAccountTransaction.toBytes();
        AccountCreateTransaction createAccountTransactionDeserialized = (AccountCreateTransaction) Transaction.fromBytes(transactionBytesSerialized);

        var txReceipt = createAccountTransactionDeserialized
            .setInitialBalance(new Hbar(5L))
            .setNodeAccountIds(nodeAccountIds)
            .setTransactionId(TransactionId.generate(testEnv.client.getOperatorAccountId()))
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        assertThat(expectedBalance).isEqualTo(createAccountTransactionDeserialized.getInitialBalance());

        new AccountDeleteTransaction()
            .setAccountId(txReceipt.accountId)
            .setTransferAccountId(testEnv.client.getOperatorAccountId())
            .freezeWith(testEnv.client)
            .sign(adminKey)
            .execute(testEnv.client);
    }

    @Test
    @DisplayName("transaction can be serialized into bytes, deserialized, edited and executed")
    void canSerializeDeserializeEditExecuteCompareFields_2() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var adminKey = PrivateKey.generateECDSA();
        var publicKey = adminKey.getPublicKey();

        var nodeAccountIds = testEnv.client.getNetwork().values().stream().toList();

        var createAccountTransaction = new AccountCreateTransaction()
            .setKey(publicKey)
            .setNodeAccountIds(nodeAccountIds);

        var expectedBalance = new Hbar(5L);

        var transactionBytesSerialized = createAccountTransaction.toBytes();
        AccountCreateTransaction createAccountTransactionDeserialized = (AccountCreateTransaction) Transaction.fromBytes(transactionBytesSerialized);

        var txReceipt = createAccountTransactionDeserialized
            .setInitialBalance(new Hbar(5L))
            .setTransactionId(TransactionId.generate(testEnv.client.getOperatorAccountId()))
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        assertThat(expectedBalance).isEqualTo(createAccountTransactionDeserialized.getInitialBalance());

        new AccountDeleteTransaction()
            .setAccountId(txReceipt.accountId)
            .setTransferAccountId(testEnv.client.getOperatorAccountId())
            .freezeWith(testEnv.client)
            .sign(adminKey)
            .execute(testEnv.client);
    }

    @Test
    @DisplayName("complete frozen and signed transaction can be serialized into bytes, deserialized and executed")
    void canFreezeSignSerializeDeserializeAndExecute() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var adminKey = PrivateKey.generateECDSA();
        var publicKey = adminKey.getPublicKey();

        var evmAddress = publicKey.toEvmAddress();
        var initialBalance = new Hbar(5L);
        var autoRenewPeriod = java.time.Duration.ofSeconds(2592000);
        var memo = "test account memo";
        var maxAutomaticTokenAssociations = 4;

        var createAccountTransaction = new AccountCreateTransaction()
            .setKey(publicKey)
            .setInitialBalance(initialBalance)
            .setReceiverSignatureRequired(true)
            .setAutoRenewPeriod(autoRenewPeriod)
            .setAccountMemo(memo)
            .setMaxAutomaticTokenAssociations(maxAutomaticTokenAssociations)
            .setDeclineStakingReward(true)
            .setAlias(evmAddress)
            .freezeWith(testEnv.client)
            .sign(adminKey);

        var transactionBytesSerialized = createAccountTransaction.toBytes();
        AccountCreateTransaction createAccountTransactionDeserialized = (AccountCreateTransaction) Transaction.fromBytes(transactionBytesSerialized);

        var transactionBytesReserialized = createAccountTransactionDeserialized.toBytes();
        assertThat(transactionBytesSerialized).isEqualTo(transactionBytesReserialized);

        AccountCreateTransaction createAccountTransactionDeserialized2 = (AccountCreateTransaction) Transaction.fromBytes(transactionBytesReserialized);

        var txResponse = createAccountTransactionDeserialized2.execute(testEnv.client);

        var accountId = txResponse.getReceipt(testEnv.client).accountId;

        new AccountDeleteTransaction()
            .setAccountId(accountId)
            .setTransferAccountId(testEnv.client.getOperatorAccountId())
            .freezeWith(testEnv.client)
            .sign(adminKey)
            .execute(testEnv.client);
    }

    @Test
    @DisplayName("complete frozen transaction can be serialized into bytes, deserialized, signature added and executed")
    void canFreezeSerializeDeserializeAddSignatureAndExecute() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var key = PrivateKey.generateED25519();

        var transaction = new AccountCreateTransaction()
            .setKey(key)
            .freezeWith(testEnv.client)
            .signWithOperator(testEnv.client);

        var expectedHash = transaction.getTransactionHashPerNode();

        @Var var response = transaction.execute(testEnv.client);

        var record = response.getRecord(testEnv.client);

        assertThat(expectedHash.get(response.nodeId)).containsExactly(record.transactionHash.toByteArray());

        var accountId = record.receipt.accountId;
        assertThat(accountId).isNotNull();

        var deleteTransaction = new AccountDeleteTransaction()
            .setAccountId(accountId)
            .setTransferAccountId(testEnv.operatorId)
            .freezeWith(testEnv.client);

        System.out.println("TransactionId (before)= " + deleteTransaction.getTransactionId());
        System.out.println("NodeAccountIds (before)= " + deleteTransaction.getNodeAccountIds());
        var updateBytes = deleteTransaction.toBytes();

        var sig1 = key.signTransaction(deleteTransaction);

        var deleteTransaction2 = Transaction.fromBytes(updateBytes);

        System.out.println("TransactionId (after)= " + deleteTransaction2.getTransactionId());
        System.out.println("NodeAccountIds (after)= " + deleteTransaction2.getNodeAccountIds());

        response = deleteTransaction2
            .addSignature(key.getPublicKey(), sig1)
            .execute(testEnv.client);

        System.out.println("record= " + response.getRecord(testEnv.client));
        System.out.println("receipt= " + response.getReceipt(testEnv.client));

        testEnv.close();
    }

    @Test
    @DisplayName("transaction can be serialized into bytes, deserialized, edited and executed")
    void canSerializeDeserializeExecute() throws Exception {
        // There are potential bugs in FileAppendTransaction which require more than one node to trigger.
        var testEnv = new IntegrationTestEnv(2);

        // Skip if using local node.
        // Note: this check should be removed once the local node is supporting multiple nodes.
        testEnv.assumeNotLocalNode();

        var response = new FileCreateTransaction()
            .setKeys(testEnv.operatorKey)
            .setContents("[e2e::FileCreateTransaction]")
            .execute(testEnv.client);

        var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

        Thread.sleep(5000);

        @Var var info = new FileInfoQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

        assertThat(info.fileId).isEqualTo(fileId);
        assertThat(info.size).isEqualTo(28);
        assertThat(info.isDeleted).isFalse();
        assertThat(info.keys).isNotNull();
        assertThat(info.keys.getThreshold()).isNull();
        assertThat(info.keys).isEqualTo(KeyList.of(testEnv.operatorKey));

        var fileAppendTransaction = new FileAppendTransaction()
            .setFileId(fileId)
            .setContents(Contents.BIG_CONTENTS);

        var transactionBytesSerialized = fileAppendTransaction.toBytes();
        FileAppendTransaction fileAppendTransactionDeserialized = (FileAppendTransaction) Transaction.fromBytes(transactionBytesSerialized);

        fileAppendTransactionDeserialized
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var contents = new FileContentsQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

        assertThat(contents.toStringUtf8()).isEqualTo("[e2e::FileCreateTransaction]" + Contents.BIG_CONTENTS);

        info = new FileInfoQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

        assertThat(info.fileId).isEqualTo(fileId);
        assertThat(info.size).isEqualTo(13522);
        assertThat(info.isDeleted).isFalse();
        assertThat(info.keys).isNotNull();
        assertThat(info.keys.getThreshold()).isNull();
        assertThat(info.keys).isEqualTo(KeyList.of(testEnv.operatorKey));

        new FileDeleteTransaction()
            .setFileId(fileId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close();
    }

    @Test
    @DisplayName("transaction can be frozen, signed, serialized into bytes, deserialized and be equal to the original one")
    void canFreezeSignSerializeDeserializeAndCompare() throws Exception {
        // There are potential bugs in FileAppendTransaction which require more than one node to trigger.
        var testEnv = new IntegrationTestEnv(2);

        // Skip if using local node.
        // Note: this check should be removed once the local node is supporting multiple nodes.
        testEnv.assumeNotLocalNode();

        var privateKey = PrivateKey.generateED25519();

        var response = new FileCreateTransaction()
            .setKeys(testEnv.operatorKey)
            .setContents("[e2e::FileCreateTransaction]")
            .execute(testEnv.client);

        var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

        Thread.sleep(5000);

        @Var var info = new FileInfoQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

        assertThat(info.fileId).isEqualTo(fileId);
        assertThat(info.size).isEqualTo(28);
        assertThat(info.isDeleted).isFalse();
        assertThat(info.keys).isNotNull();
        assertThat(info.keys.getThreshold()).isNull();
        assertThat(info.keys).isEqualTo(KeyList.of(testEnv.operatorKey));

        var fileAppendTransaction = new FileAppendTransaction()
            .setFileId(fileId)
            .setContents(Contents.BIG_CONTENTS)
            .freezeWith(testEnv.client)
            .sign(privateKey);

        var transactionBytesSerialized = fileAppendTransaction.toBytes();
        FileAppendTransaction fileAppendTransactionDeserialized = (FileAppendTransaction) Transaction.fromBytes(transactionBytesSerialized);

        var transactionBytesReserialized = fileAppendTransactionDeserialized.toBytes();
        assertThat(transactionBytesSerialized).isEqualTo(transactionBytesReserialized);

        testEnv.close();
    }



    // TODO: this test has a bunch of things hard-coded into it, which is kinda dumb, but it's a good idea for a test.
    //       Any way to fix it and bring it back?
    @Disabled
    @Test
    @DisplayName("transaction can be serialized into bytes, deserialized, signature added and executed")
    void transactionFromToBytes2() {
        assertThatNoException().isThrownBy(() -> {
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
            @Var var byts = signedBuilder.build().toByteString();

            byts = TransactionList.newBuilder()
                .addTransactionList(com.hedera.hashgraph.sdk.proto.Transaction.newBuilder()
                    .setSignedTransactionBytes(byts)
                    .build())
                .build().toByteString();

            var tx = (TransferTransaction) Transaction.fromBytes(byts.toByteArray());

            var testEnv = new IntegrationTestEnv(1);

            assertThat(tx.getHbarTransfers().get(new AccountId(542348)).toTinybars()).isEqualTo(-10);
            assertThat(tx.getHbarTransfers().get(new AccountId(47439)).toTinybars()).isEqualTo(10);

            assertThat(tx.getNodeAccountIds()).isNotNull();
            assertThat(tx.getNodeAccountIds().size()).isEqualTo(1);
            assertThat(tx.getNodeAccountIds().get(0)).isEqualTo(new AccountId(3));

            var signatures = tx.getSignatures();
            assertThat(Arrays.toString(signatures.get(new AccountId(3)).get(publicKey1))).isEqualTo(Arrays.toString(signature1));
            assertThat(Arrays.toString(signatures.get(new AccountId(3)).get(publicKey2))).isEqualTo(Arrays.toString(signature2));
            assertThat(Arrays.toString(signatures.get(new AccountId(3)).get(publicKey3))).isEqualTo(Arrays.toString(signature3));
            assertThat(Arrays.toString(signatures.get(new AccountId(3)).get(publicKey4))).isEqualTo(Arrays.toString(signature4));
            assertThat(Arrays.toString(signatures.get(new AccountId(3)).get(publicKey5))).isEqualTo(Arrays.toString(signature5));

            var resp = tx.execute(testEnv.client);

            resp.getReceipt(testEnv.client);

            testEnv.close();
        });
    }
}
