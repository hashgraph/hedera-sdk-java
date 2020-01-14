package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.ResponseCodeEnum;

public class HederaStatusException extends Exception implements HederaThrowable {
    public final Status status;

    HederaStatusException(ResponseCodeEnum responseCode) {
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

    static void throwIfExceptional(ResponseCodeEnum responseCode) throws HederaStatusException {
        if (isCodeExceptional(responseCode)) {
            throw new HederaStatusException(responseCode);
        }
    }

    @Override
    public String getMessage() {
        return status.toString();
    }
}
