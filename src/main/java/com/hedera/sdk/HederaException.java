package com.hedera.sdk;

import com.hedera.sdk.proto.ResponseCodeEnum;

public class HederaException extends Exception {
    public final ResponseCodeEnum responseCode;

    HederaException(ResponseCodeEnum responseCode) {
        if (!isCodeExceptional(responseCode)) {
            throw new IllegalArgumentException("throwing an exception for a successful result code");
        }

        this.responseCode = responseCode;
    }

    static boolean isCodeExceptional(ResponseCodeEnum responseCode) {
        return responseCode != ResponseCodeEnum.OK && responseCode != ResponseCodeEnum.SUCCESS;
    }

    static void throwIfExceptional(ResponseCodeEnum responseCode) throws HederaException {
        if (isCodeExceptional(responseCode))
            throw new HederaException(responseCode);
    }

    @Override
    public String getMessage() {
        return responseCode.toString();
    }
}
