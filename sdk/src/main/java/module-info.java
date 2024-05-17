module com.hedera.hashgraph.sdk {
    requires transitive com.google.protobuf;

    requires com.google.common;
    requires com.google.gson;
    requires grpc.protobuf.lite;
    requires headlong;
    requires io.grpc.stub;
    requires io.grpc;
    requires java.net.http;
    requires org.bouncycastle.pkix;
    requires org.bouncycastle.provider;
    requires org.slf4j;

    requires static com.github.spotbugs.annotations;
    requires static com.google.errorprone.annotations;
    requires static java.annotation;

    exports com.hedera.hashgraph.sdk;
    exports com.hedera.hashgraph.sdk.logger;
    exports com.hedera.hashgraph.sdk.proto;

    opens com.hedera.hashgraph.sdk;
}
