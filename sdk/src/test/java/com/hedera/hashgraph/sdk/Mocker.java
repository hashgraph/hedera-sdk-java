package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.*;
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServiceDescriptor;
import io.grpc.Status;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.ServerCalls;
import java8.util.function.Function;
import org.threeten.bp.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class Mocker implements AutoCloseable {
    private static final PrivateKey PRIVATE_KEY = PrivateKey.fromString("302e020100300506032b657004220420d45e1557156908c967804615af59a000be88c7aa7058bfcbe0f46b16c28f887d");

    private final List<ServiceDescriptor> services = List.of(
        CryptoServiceGrpc.getServiceDescriptor(),
        FileServiceGrpc.getServiceDescriptor(),
        SmartContractServiceGrpc.getServiceDescriptor(),
        ConsensusServiceGrpc.getServiceDescriptor(),
        TokenServiceGrpc.getServiceDescriptor()
    );
    private final List<List<Object>> responses;
    private final List<Server> servers = new ArrayList<>();

    public final Client client;

    Mocker(List<List<Object>> responses) {
        this.responses = responses;

        var network = new HashMap<String, AccountId>(responses.size());

        for (var i = 0; i < responses.size(); i++) {
            var index = new AtomicInteger();
            var response = responses.get(i);
            var name = InProcessServerBuilder.generateName();
            var nodeAccountId = new AccountId(3 + i);
            var builder = InProcessServerBuilder.forName(name);

            network.put("in-process:" + name, nodeAccountId);

            for (var service : services) {
                var descriptor = ServerServiceDefinition.builder(service);

                for (MethodDescriptor<?, ?> method : service.getMethods()) {
                    var methodDefinition = ServerMethodDefinition.create((MethodDescriptor<Object, Object>) method,
                        ServerCalls.asyncUnaryCall((request, responseObserver) -> {
                            var responseIndex = index.getAndIncrement();

                            if (responseIndex >= response.size()) {
                                responseObserver.onError(Status.Code.ABORTED.toStatus().asRuntimeException());
                                return;
                            }

                            var r = response.get(responseIndex);

                            if (r instanceof Function<?, ?>){
                                r = ((Function<Object, Object>) r).apply(request);
                            }

                            if (r instanceof Throwable) {
                                responseObserver.onError((Throwable) r);
                            } else {
                                responseObserver.onNext(r);
                                responseObserver.onCompleted();
                            }
                        })
                    );
                    descriptor.addMethod(methodDefinition);
                }

                builder.addService(descriptor.build());
            }

            try {
                this.servers.add(builder.directExecutor().build().start());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        this.client = Client.forNetwork(network)
            .setOperator(new AccountId(1800), PRIVATE_KEY)
            .setMinBackoff(Duration.ofMillis(0))
            .setMaxBackoff(Duration.ofMillis(0))
            .setNodeMinBackoff(Duration.ofMillis(0))
            .setNodeMaxBackoff(Duration.ofMillis(0))
            .setMinNodeReadmitTime(Duration.ofMillis(0))
            .setMaxNodeReadmitTime(Duration.ofMillis(0));
    }

    public static Mocker withResponses(List<List<Object>> responses) {
        return new Mocker(responses);
    }

    public void close() throws TimeoutException, InterruptedException {
        client.close();

        for (var server : servers) {
            server.shutdown();
            server.awaitTermination();
        }
    }
}
