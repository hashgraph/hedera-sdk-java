/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
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
package com.hedera.hashgraph.sdk;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.inprocess.InProcessServerBuilder;
import org.threeten.bp.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

// TODO: we may want to refactor to separate TestClient from TestServer.
//       That way, we can have a client with a network of multiple test servers.
//       Maybe we can test load-balancing?

public class TestServer {
    public final Client client;
    private final Server grpcServer;

    public TestServer(String name, BindableService... services) throws IOException {
        var serverBuilder = InProcessServerBuilder.forName(name);
        for (var service : services) {
            serverBuilder.addService(service);
        }
        grpcServer = serverBuilder.directExecutor().build().start();

        var network = new HashMap<String, AccountId>();
        network.put("in-process:" + name, AccountId.fromString("1.1.1"));
        client = Client.forNetwork(network)
            .setMinBackoff(Duration.ofMillis(1))
            .setMaxBackoff(Duration.ofMillis(1))
            .setNodeMinBackoff(Duration.ofMillis(1))
            .setNodeMaxBackoff(Duration.ofMillis(1))
            .setOperator(AccountId.fromString("2.2.2"), PrivateKey.generate());
    }

    public void close() throws TimeoutException, InterruptedException {
        client.close();
        grpcServer.shutdown();
        grpcServer.awaitTermination();
    }
}
