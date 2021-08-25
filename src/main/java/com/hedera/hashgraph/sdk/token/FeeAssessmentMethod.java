package com.hedera.hashgraph.sdk.token;

public enum FeeAssessmentMethod {
    INCLUSIVE(false),
    EXCLUSIVE(true);

    final boolean code;

    FeeAssessmentMethod(boolean code) {
        this.code = code;
    }

    static FeeAssessmentMethod valueOf(boolean code) {
        return code ? EXCLUSIVE : INCLUSIVE;
    }

    @Override
    public String toString() {
        return code ? "EXCLUSIVE" : "INCLUSIVE";
    }
}
