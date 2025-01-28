// SPDX-License-Identifier: Apache-2.0
module org.hiero.sdk.java {
    requires transitive com.google.protobuf;
    requires com.esaulpaugh.headlong;
    requires com.google.common;
    requires com.google.gson;
    requires io.grpc.inprocess;
    requires io.grpc.protobuf.lite;
    requires io.grpc.stub;
    requires io.grpc;
    requires java.net.http;
    requires org.bouncycastle.pkix;
    requires org.bouncycastle.provider;
    requires org.slf4j;
    requires static transitive java.annotation;

    exports org.hiero.sdk.java;
    exports org.hiero.sdk.java.proto;

    opens org.hiero.sdk.java;

    exports org.hiero.sdk.java.logger;
}
