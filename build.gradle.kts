plugins {
    id("com.hedera.gradle.root")
}

dependencyAnalysis.abi {
    exclusions {
        // Exposes: org.slf4j.Logger
        excludeClasses("logger")
        // Exposes: com.google.common.base.MoreObjects.ToStringHelper
        excludeClasses(".*\\.CustomFee")
        // Exposes: com.esaulpaugh.headlong.abi.Tuple
        excludeClasses(".*\\.ContractFunctionResult")
        // Exposes: org.bouncycastle.crypto.params.KeyParameter
        excludeClasses(".*\\.PrivateKey.*")
        // Exposes: io.grpc.stub.AbstractFutureStub (and others)
        excludeClasses(".*Grpc")
    }
}
