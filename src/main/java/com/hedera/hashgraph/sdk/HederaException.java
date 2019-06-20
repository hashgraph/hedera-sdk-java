package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum;

public class HederaException extends Exception implements HederaThrowable {
    public final ResponseCodeEnum responseCode;

    HederaException(ResponseCodeEnum responseCode) {
        if (!isCodeExceptional(responseCode, true)) {
            throw new IllegalArgumentException("code not exceptional: " + responseCode);
        }

        this.responseCode = responseCode;
    }

    static boolean isCodeExceptional(ResponseCodeEnum responseCode, boolean unknownIsExceptional) {
        switch (responseCode) {
        case UNKNOWN:
        case RECEIPT_NOT_FOUND:
        case RECORD_NOT_FOUND:
            return unknownIsExceptional;
        case SUCCESS:
        case OK:
            return false;

        default:
        }

        return true;
    }

    static void throwIfExceptional(ResponseCodeEnum responseCode) throws HederaException {
        throwIfExceptional(responseCode, true);
    }

    static void throwIfExceptional(ResponseCodeEnum responseCode, boolean throwIfUnknown) throws HederaException {
        if (isCodeExceptional(responseCode, throwIfUnknown)) {
            throw new HederaException(responseCode);
        }
    }

    @Override
    public String getMessage() {
        return responseCode.toString();
    }
}
