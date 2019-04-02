package com.hedera.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public abstract class ValidatedBuilder {

    private @Nullable List<String> validationErrors;

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

    protected void require(@Nullable List setValue, String errMsg) {
        if (setValue == null || setValue.isEmpty()) {
            addValidationError(errMsg);
        }
    }

    protected void require(@Nullable ByteString setValue, String errMsg) {
        if (setValue == null || setValue.isEmpty()) {
            addValidationError(errMsg);
        }
    }

    protected void require(@Nullable MessageOrBuilder setValue, String errMsg) {
        if (setValue == null || setValue.isInitialized()) {
            addValidationError(errMsg);
        }
    }

    protected void requireExactlyOne(String errMsg, String errCollision, MessageOrBuilder... values) {
        var oneIsInit = false;

        for (var message : values) {
            if (message != null && message.isInitialized()) {
                if (oneIsInit) {
                    addValidationError(errCollision);
                    return;
                }

                oneIsInit = true;
            }
        }

        if (!oneIsInit) {
            addValidationError(errMsg);
        }
    }

    protected void require(@Nullable String setValue, String errMsg) {
        if (setValue == null || setValue.isEmpty()) {
            addValidationError(errMsg);
        }
    }

    /* protected void require(@Nullable Object setValue, String errMsg) {
     * if (setValue == null) {
     * addValidationError(errMsg);
     * }
     * } */
}
