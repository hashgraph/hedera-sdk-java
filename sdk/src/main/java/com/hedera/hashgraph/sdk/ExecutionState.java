package com.hedera.hashgraph.sdk;

/**
 * Enum for the execution states.
 */
public enum ExecutionState {
    Success,
    Retry,          // call successful but operation not complete, retry with same/new node
    ServerError,    // bad node, retry with new node
    RequestError    // user error
}
