package com.hedera.sdk;

import com.hedera.sdk.proto.ResponseCodeEnum;

public class HederaException extends Exception {
    public final ResponseCodeEnum responseCode;

    private HederaException(ResponseCodeEnum responseCode) {
        this.responseCode = responseCode;
    }

    static void throwIfExceptional(ResponseCodeEnum responseCode) throws HederaException {
        throwIfExceptional(responseCode, true);
    }

    static void throwIfExceptional(ResponseCodeEnum responseCode, boolean throwIfUnknown) throws HederaException {
        switch (responseCode) {
        case UNKNOWN:
            if (throwIfUnknown)
                break;
            // fall through
        case SUCCESS:
        case OK:
            return;

        default:
        }

        throw new HederaException(responseCode);
    }

    @Override
    public String getMessage() {
        return responseCode.toString();
    }
}
