package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.AccountAmount;
import com.hedera.hashgraph.sdk.proto.ContractFunctionResult;
import com.hedera.hashgraph.sdk.proto.ContractLoginfo;
import com.hedera.hashgraph.sdk.proto.NftTransfer;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.TokenTransferList;
import com.hedera.hashgraph.sdk.proto.TransactionGetRecordResponse;
import com.hedera.hashgraph.sdk.proto.TransferList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TransactionRecordTest {
    final Instant exampleInstant = Instant.ofEpochSecond(1554158542);

    @Test
    @DisplayName("using toBytes and fromBytes will produce the correct response")
    void toFromBytes() throws InvalidProtocolBufferException {
        var hbarTransfer = AccountAmount.newBuilder()
            .setAccountID(AccountId.fromString("0.0.5005").toProtobuf())
            .setAmount(100_000);

        var hbarTransferList = TransferList.newBuilder();
        hbarTransferList.addAccountAmounts(hbarTransfer);

        var tokenTransfer = AccountAmount.newBuilder()
            .setAccountID(AccountId.fromString("0.0.5006").toProtobuf())
            .setAmount(100_000);

        var tokenTransferList = TokenTransferList.newBuilder()
            .setToken(TokenId.fromString("0.0.5007").toProtobuf());
        tokenTransferList.addTransfers(tokenTransfer);

        var nftTransfer = NftTransfer.newBuilder()
            .setSenderAccountID(AccountId.fromString("0.0.5006").toProtobuf())
            .setReceiverAccountID(AccountId.fromString("0.0.5007").toProtobuf())
            .setSerialNumber(888);

        var nftTransferList = TokenTransferList.newBuilder();
        nftTransferList.addNftTransfers(nftTransfer);

        Response response = Response.newBuilder()
            .setTransactionGetRecord(
                TransactionGetRecordResponse.newBuilder()
                    .setTransactionRecord(com.hedera.hashgraph.sdk.proto.TransactionRecord.newBuilder()
                        .setReceipt(com.hedera.hashgraph.sdk.proto.TransactionReceipt.newBuilder().build())
                        .setTransactionHash(ByteString.copyFrom("hello", StandardCharsets.UTF_8))
                        .setConsensusTimestamp(InstantConverter.toProtobuf(exampleInstant))
                        .setTransactionID(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), exampleInstant).toProtobuf())
                        .setMemo("hola")
                        .setTransactionFee(100_000)
                        .setTransferList(hbarTransferList)
                        .addTokenTransferLists(tokenTransferList)
                        .addTokenTransferLists(nftTransferList)
                        .setContractCallResult(ContractFunctionResult.newBuilder()
                            .addLogInfo(ContractLoginfo.newBuilder()
                                .addTopic(ByteString.copyFrom("aloha", StandardCharsets.UTF_8))
                                .setContractID(ContractId.fromString("0.0.5007").toProtobuf())
                                .setBloom(ByteString.copyFrom("bonjour", StandardCharsets.UTF_8))
                            )
                            .setContractID(ContractId.fromString("0.0.5008").toProtobuf())
                            .setContractCallResult(ByteString.copyFrom("hello again", StandardCharsets.UTF_8))
                            .setBloom(ByteString.copyFrom("hola otra vez", StandardCharsets.UTF_8))
                            .setGasUsed(100_000)
                            .setErrorMessage("hello x3")
                        )
                    )
            )
            .build();

        TransactionRecord record = TransactionRecord.fromProtobuf(response.getTransactionGetRecord().getTransactionRecord());

        assertNotNull(record);
        assertNotNull(record.toBytes());

        byte[] recordBytes = record.toBytes();
        TransactionRecord newRecord = TransactionRecord.fromBytes(recordBytes);

        assertEquals(record.toString(), newRecord.toString());
    }
}
