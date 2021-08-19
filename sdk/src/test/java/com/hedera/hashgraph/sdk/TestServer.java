package com.hedera.hashgraph.sdk;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.inprocess.InProcessServerBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

public class TestServer {
    private Server grpcServer;
    private Client client;

    public TestServer(String name, BindableService... services) throws IOException {
        var serverBuilder = InProcessServerBuilder.forName(name);
        for(var service : services) {
            serverBuilder.addService(service);
        }
        grpcServer = serverBuilder.directExecutor().build().start();

        var network = new HashMap<String, AccountId>();
        network.put("in-process:test", AccountId.fromString("0.0.1"));
        client = Client.forNetwork(network);
    }

    public Client getClient() {
        return client;
    }

    public void close() throws TimeoutException, InterruptedException {
        client.close();
        grpcServer.shutdown();
        grpcServer.awaitTermination();
    }
}
