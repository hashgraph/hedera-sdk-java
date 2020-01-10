package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.ResponseCodeEnum;

public class HederaException extends Exception implements HederaThrowable {
    public final Status status;

    HederaException(ResponseCodeEnum responseCode) {
        if (!isCodeExceptional(responseCode)) {
            throw new IllegalArgumentException("code not exceptional: " + responseCode);
        }

        this.status = Status.valueOf(responseCode);
    }

    static boolean isCodeExceptional(ResponseCodeEnum responseCode) {
        switch (responseCode) {
        case SUCCESS:
        case OK:
            return false;

        default:
        }

        return true;
    }

    static void throwIfExceptional(ResponseCodeEnum responseCode) throws HederaException {
        if (isCodeExceptional(responseCode)) {
            throw new HederaException(responseCode);
        }
    }

    @Override
    public String getMessage() {
        return status.toString();
    }
}
