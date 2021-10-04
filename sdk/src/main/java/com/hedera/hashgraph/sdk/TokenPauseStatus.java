package com.hedera.hashgraph.sdk;

public enum TokenPauseStatus {
    /**
     * Indicates that a Token has no pauseKey
     */
    PAUSE_NOT_APPLICABLE(com.hedera.hashgraph.sdk.proto.TokenPauseStatus.PauseNotApplicable),

    /**
     * Indicates that a Token is Paused
     */
    PAUSED(com.hedera.hashgraph.sdk.proto.TokenPauseStatus.Paused),

    /**
     * Indicates that a Token is Unpaused.
     */
    UNPAUSED(com.hedera.hashgraph.sdk.proto.TokenPauseStatus.Unpaused);

    final com.hedera.hashgraph.sdk.proto.TokenPauseStatus code;

    TokenPauseStatus(com.hedera.hashgraph.sdk.proto.TokenPauseStatus code) {
        this.code = code;
    }

    static TokenPauseStatus valueOf(com.hedera.hashgraph.sdk.proto.TokenPauseStatus code) {
        switch (code) {
            case PauseNotApplicable:
                return PAUSE_NOT_APPLICABLE;
            case Paused:
                return PAUSED;
            case Unpaused:
                return UNPAUSED;
            default:
                throw new IllegalStateException("(BUG) unhandled TokenPauseStatus");
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case PAUSE_NOT_APPLICABLE:
                return "PAUSE_NOT_APPLICABLE";
            case PAUSED:
                return "PAUSED";
            case UNPAUSED:
                return "UNPAUSED";
            default:
                return "<UNRECOGNIZED VALUE>";
        }
    }
}
