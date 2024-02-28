package com.hedera.hashgraph.tck.params;

public record SetupParams(
        String operatorAccountId,
        String operatorPrivateKey,
        String nodeIp,
        String nodeAccountId,
        String mirrorNetworkIp) {}
