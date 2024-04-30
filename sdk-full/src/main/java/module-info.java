module com.hedera.hashgraph.sdk.full {
    requires transitive com.google.common;
    requires transitive com.google.protobuf;
    requires transitive headlong;
    requires transitive io.grpc.stub;
    requires transitive io.grpc;
    requires transitive org.bouncycastle.provider;
    requires transitive org.slf4j;

    requires com.google.gson;
    requires grpc.protobuf;
    requires java.net.http;
    requires org.bouncycastle.pkix;

    requires static com.github.spotbugs.annotations;
    requires static com.google.errorprone.annotations;
    requires static java.annotation;

    exports com.hedera.hashgraph.sdk;
    exports com.hedera.hashgraph.sdk.logger;
    exports com.hedera.hashgraph.sdk.proto;

    opens com.hedera.hashgraph.sdk;
}
