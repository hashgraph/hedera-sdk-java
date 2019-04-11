package com.hedera.sdk;

import com.hedera.sdk.proto.ResponseCodeEnum;
import com.hedera.sdk.proto.Transaction;
import com.hedera.sdk.proto.TransactionResponse;

import static com.hedera.sdk.TransactionException.Reason;

import javax.annotation.Nullable;
import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.Function;

public abstract class TransactionCall extends HederaCall<Transaction, TransactionResponse, Boolean, TransactionException> {
    protected final Transaction.Builder inner;

    TransactionCall() {
        this(Transaction.newBuilder());
    }

    TransactionCall(Transaction.Builder inner) {
        super(TransactionCall::mapTxResponse);
        this.inner = inner;
    }

    /** Execute the transaction and immediately get its receipt which may not be available yet. */
    public final Optional<TransactionReceipt> executeForReceipt() throws TransactionException {

    }

    public void executeForReceiptAsync(Function<Optional<TransactionReceipt>, Void> onSuccess, Function<Throwable, Void> onFailure) {

    }

    public Future<Optional<TransactionReceipt>> executeForReceiptFuture() {

    }

    public Optional<TransactionRecord> executeForRecord() throws TransactionException {

    }

    public void executeForRecordAsync(Function<Optional<TransactionRecord>, Void> onSuccess, Function<Throwable, Void> onFailure) {

    }

    public Future<Optional<TransactionRecord>> executeForRecordFuture() {

    }

    private static Boolean mapTxResponse(TransactionResponse response) throws TransactionException {
        var precheck = response.getNodeTransactionPrecheckCode();

        if (precheck != ResponseCodeEnum.OK && precheck != ResponseCodeEnum.UNKNOWN) {
            throw new TransactionException(precheck);
        }

        return precheck == ResponseCodeEnum.OK;
    }
}
