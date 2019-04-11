package com.hedera.sdk;

import com.hedera.sdk.proto.ResponseCodeEnum;

public class TransactionException extends Exception {
    public enum Reason {
        NodeBusy;

        static Reason fromPrecheck(ResponseCodeEnum precheckCode) {
            switch(precheckCode) {
                case BUSY:
                    return NodeBusy;

                default:
                    throw new IllegalArgumentException("precheck code not associated with transactions: " + precheckCode);
            }
        }
    }

    public final Reason reason;

    TransactionException(ResponseCodeEnum precheckCode) {
        this.reason = Reason.fromPrecheck(precheckCode);
    }

    @Override
    public String toString() {
        return "TransactionException{" +
            "reason=" + reason +
            '}';
    }
}
