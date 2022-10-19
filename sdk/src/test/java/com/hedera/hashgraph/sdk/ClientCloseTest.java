package com.hedera.hashgraph.sdk;

import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import java.util.Collections;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ClientCloseTest {
    @Test
    void closeHandlesNetworkTimeout() {
        var executor = Client.createExecutor();
        var network = mock(Network.class);
        when(network.awaitClose(any(), any())).thenReturn(new TimeoutException("network timeout"));
        var mirrorNetwork = MirrorNetwork.forNetwork(executor, Collections.emptyList());
        var client = new Client(executor, network, mirrorNetwork, null, null);

        assertThatExceptionOfType(TimeoutException.class).isThrownBy(client::close).withMessage("network timeout");
        assertThat(mirrorNetwork.hasShutDownNow).isTrue();
    }

    @Test
    void closeHandlesNetworkInterrupted() {
        var interruptedException = new InterruptedException("network interrupted");
        var executor = Client.createExecutor();
        var network = mock(Network.class);
        when(network.awaitClose(any(), any())).thenReturn(interruptedException);
        var mirrorNetwork = MirrorNetwork.forNetwork(executor, Collections.emptyList());
        var client = new Client(executor, network, mirrorNetwork, null, null);

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(client::close).withCause(interruptedException);
        assertThat(mirrorNetwork.hasShutDownNow).isTrue();
    }

    @Test
    void closeHandlesMirrorNetworkTimeout() {
        var executor = Client.createExecutor();
        var network = Network.forNetwork(executor, Collections.emptyMap());
        var mirrorNetwork = mock(MirrorNetwork.class);
        when(mirrorNetwork.awaitClose(any(), any())).thenReturn(new TimeoutException("mirror timeout"));
        var client = new Client(executor, network, mirrorNetwork, null, null);

        assertThatExceptionOfType(TimeoutException.class).isThrownBy(client::close).withMessage("mirror timeout");
        assertThat(network.hasShutDownNow).isFalse();
    }

    @Test
    void closeHandlesMirrorNetworkInterrupted() {
        var interruptedException = new InterruptedException("network interrupted");
        var executor = Client.createExecutor();
        var network = Network.forNetwork(executor, Collections.emptyMap());
        var mirrorNetwork = mock(MirrorNetwork.class);
        when(mirrorNetwork.awaitClose(any(), any())).thenReturn(interruptedException);
        var client = new Client(executor, network, mirrorNetwork, null, null);

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(client::close).withCause(interruptedException);
        assertThat(network.hasShutDownNow).isFalse();
    }

    @Test
    void closeHandlesExecutorShutdown() throws TimeoutException {
        var executor = Client.createExecutor();
        var network = Network.forNetwork(executor, Collections.emptyMap());
        var mirrorNetwork = MirrorNetwork.forNetwork(executor, Collections.emptyList());
        var client = new Client(executor, network, mirrorNetwork, null, null);

        client.close();
        assertThat(executor.isShutdown()).isTrue();
    }

    @Test
    void closeHandlesExecutorNotTerminatingInTime() throws InterruptedException, TimeoutException {
        var duration = Duration.ofSeconds(30);
        var executor = (ThreadPoolExecutor) Client.createExecutor();
        var executorSpy = spy(ThreadPoolExecutor.class);
        doNothing().when(executorSpy).shutdown();
//        when(executorSpy.shutdownNow()).thenCallRealMethod();
//        FieldSetter.setField(executorSpy, executorSpy.getClass().getDeclaredField("mainLock"), new ReentrantLock());
//        when(executorSpy.awaitTermination(30, TimeUnit.SECONDS)).thenReturn(false);
        doReturn(false).when(executorSpy).awaitTermination(30 / 2, TimeUnit.SECONDS);
        var network = Network.forNetwork(executorSpy, Collections.emptyMap());
        var mirrorNetwork = MirrorNetwork.forNetwork(executorSpy, Collections.emptyList());
        var client = new Client(executorSpy, network, mirrorNetwork, null, null);

        client.close(duration);
        assertThat(executorSpy.isShutdown()).isTrue();
    }
}
