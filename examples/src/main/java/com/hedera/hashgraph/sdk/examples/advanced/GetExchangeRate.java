package com.hedera.hashgraph.sdk.examples.advanced;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;
import com.hedera.hashgraph.sdk.file.FileContentsQuery;
import com.hedera.hashgraph.sdk.file.FileId;
import com.hedera.hashgraph.sdk.proto.ExchangeRateSet;

public class GetExchangeRate {
    private GetExchangeRate() { }

    public static void main(String[] args) throws HederaException, InvalidProtocolBufferException {
        final var client = ExampleHelper.createHederaClient();

        final var responseBytes = new FileContentsQuery(client)
            .setFileId(new FileId(0, 0, 112))
            .setPaymentDefault()
            .execute()
            .getFileContents().getContents();

        final var ers = ExchangeRateSet.parseFrom(responseBytes);

        System.out.println(ers);
    }
}
