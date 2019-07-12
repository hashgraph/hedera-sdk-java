package com.hedera.hashgraph.sdk.examples.advanced;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;
import com.hedera.hashgraph.sdk.file.FileContentsQuery;
import com.hedera.hashgraph.sdk.file.FileId;
import com.hedera.hashgraph.sdk.proto.CurrentAndNextFeeSchedule;

public final class GetFeeSchedule {
    private GetFeeSchedule() { }

    public static void main(String[] args) throws HederaException, InvalidProtocolBufferException {

        // Build the Hedera client using ExampleHelper class
        var client = ExampleHelper.createHederaClient();

        // Get file contents
        final var scheduleContents = new FileContentsQuery(client)
            // fileNum 111 is the fee schedule
            // this should be free
            .setFileId(new FileId(0, 0, 111))
            .setPaymentDefault()
            .execute();

        final var feeSchedule = CurrentAndNextFeeSchedule.parseFrom(
            scheduleContents.getFileContents().getContents());

        System.out.println("fee schedule: " + feeSchedule);
    }
}
