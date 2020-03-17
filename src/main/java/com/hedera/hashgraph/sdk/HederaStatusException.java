package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.ResponseCodeEnum;

/**
 * Base class for exceptions thrown from HAPI calls that result in a {@link Status} code that
 * is not {@link Status#Success}.
 *
 * Additional context is provided by the specific subclass that is thrown:
 *
 * <ul>
 *     <li>{@link HederaPrecheckStatusException}</li>
 *     <li>{@link HederaReceiptStatusException}</li>
 *     <li>{@link HederaRecordStatusException}</li>
 * </ul>
 */
public class HederaStatusException extends Exception implements HederaThrowable {
    /**
     * The status code carried by this exception. It will not be {@link Status#Ok} or
     * {@link Status#Success}.
     */
    public final Status status;

    HederaStatusException(ResponseCodeEnum responseCode) {
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

    @Override
    public String getMessage() {
        return status.toString();
    }
}
