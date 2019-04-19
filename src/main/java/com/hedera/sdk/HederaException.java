package com.hedera.sdk;

import com.hedera.sdk.proto.ResponseCodeEnum;

public class HederaException extends Exception {
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
            if (unknownIsExceptional)
                break;
            // fall through
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
