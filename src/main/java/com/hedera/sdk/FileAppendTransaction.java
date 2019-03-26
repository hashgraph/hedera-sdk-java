package com.hedera.sdk;

import com.google.protobuf.ByteString;
import com.hedera.sdk.proto.FileAppendTransactionBody;

public final class FileAppendTransaction extends TransactionBuilder<FileAppendTransaction> {
    private final FileAppendTransactionBody.Builder builder;

    public FileAppendTransaction() {
        builder = inner.getBodyBuilder().getFileAppendBuilder();
    }

    public FileAppendTransaction setFileId(FileId fileId) {
        builder.setFileID(fileId.inner);
        return this;
    }

    public FileAppendTransaction setContents(byte[] contents) {
        // TODO: there is a maximum length for contents. What is it?
        builder.setContents(ByteString.copyFrom(contents));
        return this;
    }
}
