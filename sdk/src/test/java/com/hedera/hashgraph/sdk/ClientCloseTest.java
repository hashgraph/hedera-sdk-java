package com.hedera.hashgraph.sdk;

import org.bouncycastle.asn1.x509.Time;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockSettings;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.in;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientCloseTest {
    @Test
    void closeHandlesNetworkTimeout() {
        var executor = Client.createExecutor();
        var network = mock(Network.class);
        when(network.awaitClose(any(), any())).thenReturn(new TimeoutException("network timeout"));
        var mirrorNetwork = MirrorNetwork.forNetwork(executor, Collections.emptyList());
        var client = new Client(executor, network, mirrorNetwork);

        assertThatExceptionOfType(TimeoutException.class).isThrownBy(client::close).withMessage("network timeout");
        assertThat(mirrorNetwork.hasShutDownNow).isTrue();
        assertThat(client.executorHasShutDownNow).isTrue();
    }

    @Test
    void closeHandlesNetworkInterrupted() {
        var interruptedException = new InterruptedException("network interrupted");
        var executor = Client.createExecutor();
        var network = mock(Network.class);
        when(network.awaitClose(any(), any())).thenReturn(interruptedException);
        var mirrorNetwork = MirrorNetwork.forNetwork(executor, Collections.emptyList());
        var client = new Client(executor, network, mirrorNetwork);

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(client::close).withCause(interruptedException);
        assertThat(mirrorNetwork.hasShutDownNow).isTrue();
        assertThat(client.executorHasShutDownNow).isTrue();
    }

    @Test
    void closeHandlesMirrorNetworkTimeout() {
        var executor = Client.createExecutor();
        var network = Network.forNetwork(executor, Collections.emptyMap());
        var mirrorNetwork = mock(MirrorNetwork.class);
        when(mirrorNetwork.awaitClose(any(), any())).thenReturn(new TimeoutException("mirror timeout"));
        var client = new Client(executor, network, mirrorNetwork);

        assertThatExceptionOfType(TimeoutException.class).isThrownBy(client::close).withMessage("mirror timeout");
        assertThat(network.hasShutDownNow).isFalse();
        assertThat(client.executorHasShutDownNow).isTrue();
    }

    @Test
    void closeHandlesMirrorNetworkInterrupted() {
        var interruptedException = new InterruptedException("network interrupted");
        var executor = Client.createExecutor();
        var network = Network.forNetwork(executor, Collections.emptyMap());
        var mirrorNetwork = mock(MirrorNetwork.class);
        when(mirrorNetwork.awaitClose(any(), any())).thenReturn(interruptedException);
        var client = new Client(executor, network, mirrorNetwork);

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(client::close).withCause(interruptedException);
        assertThat(network.hasShutDownNow).isFalse();
        assertThat(client.executorHasShutDownNow).isTrue();
    }

    @Test
    void closeHandlesExecutorTimeout() throws InterruptedException {
        var executor = mock(ExecutorService.class);
        when(executor.awaitTermination(anyLong(), any())).thenReturn(false);
        var network = Network.forNetwork(executor, Collections.emptyMap());
        var mirrorNetwork = MirrorNetwork.forNetwork(executor, Collections.emptyList());
        var client = new Client(executor, network, mirrorNetwork);

        assertThatExceptionOfType(TimeoutException.class).isThrownBy(client::close);
        assertThat(network.hasShutDownNow).isFalse();
        assertThat(mirrorNetwork.hasShutDownNow).isFalse();
    }

    @Test
    void closeHandlesExecutorInterrupted() throws InterruptedException {
        var interruptedException = new InterruptedException("executor interrupted");
        var executor = mock(ExecutorService.class);
        when(executor.awaitTermination(anyLong(), any())).thenThrow(interruptedException);
        var network = Network.forNetwork(executor, Collections.emptyMap());
        var mirrorNetwork = MirrorNetwork.forNetwork(executor, Collections.emptyList());
        var client = new Client(executor, network, mirrorNetwork);

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(client::close).withCause(interruptedException);
        assertThat(network.hasShutDownNow).isFalse();
        assertThat(mirrorNetwork.hasShutDownNow).isFalse();
    }
}
