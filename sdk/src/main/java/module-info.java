/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2024 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

module com.hedera.hashgraph.sdk {
    requires transitive com.google.protobuf;

    requires com.google.common;
    requires com.google.gson;
    requires headlong;
    requires io.grpc.inprocess;
    requires io.grpc.protobuf.lite;
    requires io.grpc.stub;
    requires io.grpc;
    requires java.net.http;
    requires org.bouncycastle.pkix;
    requires org.bouncycastle.provider;
    requires org.slf4j;

    requires static transitive java.annotation;

    exports com.hedera.hashgraph.sdk;
    exports com.hedera.hashgraph.sdk.logger;
    exports com.hedera.hashgraph.sdk.proto;

    opens com.hedera.hashgraph.sdk;
}
