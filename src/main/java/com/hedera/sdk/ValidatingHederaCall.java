package com.hedera.sdk;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public abstract class ValidatingHederaCall<Req, RawResp, Resp> extends HederaCall<Req, RawResp, Resp> {
    private @Nullable List<String> validationErrors;

    protected ValidatingHederaCall(Function<RawResp, Resp> mapResponse) {
        super(mapResponse);
    }

    public abstract void validate();

    protected void addValidationError(String errMsg) {
        if (validationErrors == null)
            validationErrors = new ArrayList<>();
        validationErrors.add(errMsg);
    }

    protected void checkValidationErrors(String prologue) {
        if (validationErrors == null)
            return;
        var errors = validationErrors;
        validationErrors = null;
        throw new IllegalStateException(prologue + ":\n" + String.join("\n", errors));
    }

    protected final void require(boolean mustBeTrue, String errMsg) {
        if (!mustBeTrue) {
            addValidationError(errMsg);
        }
    }

    protected void require(@Nullable List setValue, String errMsg) {
        require(setValue != null && !setValue.isEmpty(), errMsg);
    }

    protected void require(@Nullable ByteString setValue, String errMsg) {
        require(setValue != null && !setValue.isEmpty(), errMsg);
    }

    // builder.isInitialized() is always true
    /* protected void require(@Nullable MessageOrBuilder setValue, String errMsg) {
     * if (setValue == null || !setValue.isInitialized()) {
     * addValidationError(errMsg);
     * }
     * } */

    protected void requireExactlyOne(String errMsg, String errCollision, boolean... values) {
        var oneIsTrue = false;

        for (var maybeTrue : values) {
            if (maybeTrue && oneIsTrue) {
                addValidationError(errCollision);
                return;
            }

            oneIsTrue |= maybeTrue;
        }

        if (!oneIsTrue) {
            addValidationError(errMsg);
        }
    }

    protected void require(@Nullable String setValue, String errMsg) {
        require(setValue != null && setValue.isEmpty(), errMsg);
    }

    /* protected void require(@Nullable Object setValue, String errMsg) {
     * if (setValue == null) {
     * addValidationError(errMsg);
     * }
     * } */
}
