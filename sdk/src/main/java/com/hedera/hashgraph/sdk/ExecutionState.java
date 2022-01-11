package com.hedera.hashgraph.sdk;

public enum ExecutionState {
    Success,
    Retry,          // call successful but operation not complete, retry with same/new node
    ServerError,    // bad node, retry with new node
    RequestError    // user error
}
