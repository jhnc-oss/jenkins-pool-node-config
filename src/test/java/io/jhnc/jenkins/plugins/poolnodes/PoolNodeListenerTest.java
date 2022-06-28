package io.jhnc.jenkins.plugins.poolnodes;

import hudson.model.Computer;
import hudson.slaves.OfflineCause;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PoolNodeListenerTest {

    @Test
    void preOnlineIsSafeToNull() {
        final PoolNodeListener listener = create(true);
        listener.preOnline(null, null, null, null);
    }

    @Test
    void preOnlineIgnoresUnrelatedNodes() {
        final Computer computer = createComputer("node-0", Collections.singletonList("unrelated"));

        final PoolNodeListener listener = create(true);
        listener.preOnline(computer, null, null, null);

        verify(computer, never()).setTemporarilyOffline(anyBoolean(), any(OfflineCause.class));
    }

    @Test
    void preOnlineKeepsNodeOnlineIfKeepOfflineIsFalse() {
        final Computer computer = createComputer("node-0", Collections.singletonList("vdi-pool-test"));

        final PoolNodeListener listener = create(false);
        listener.preOnline(computer, null, null, null);

        verify(computer).setTemporarilyOffline(eq(false), any(OfflineCause.class));
    }

    @Test
    void preOnlineKeepsNodeOfflineIfKeepOfflineIsTrue() {
        final Computer computer = createComputer("node-0", Collections.singletonList("vdi-pool-test"));

        final PoolNodeListener listener = create(true);
        listener.preOnline(computer, null, null, null);

        verify(computer).setTemporarilyOffline(eq(true), any(OfflineCause.class));
    }

    @Test
    void preOnlineIsSafeToNullDescriptor() {
        final Computer computer = createComputer("node-0", Collections.singletonList("vdi-pool-test"));

        final PoolNodeListener listener = create(true);
        doReturn(null).when(listener).getDescriptor();

        listener.preOnline(computer, null, null, null);
    }

    @Test
    void onConfigurationChangeUpdatesRelatedNodesIfKeepOfflineIsTrue() {
        final PoolNodeListener listener = create(true);
        final List<Computer> nodes = Arrays.asList(
                createComputer("unrelated-0", Collections.emptyList()),
                createComputer("node-1", Collections.singletonList("vdi-pool-test")),
                createComputer("unrelated-2", Collections.emptyList()),
                createComputer("node-3", Collections.singletonList("vdi-pool-test")));
        doReturn(nodes).when(listener).getComputers();

        listener.onConfigurationChange();

        verify(nodes.get(0), never()).setTemporarilyOffline(eq(false), any(OfflineCause.class));
        verify(nodes.get(1)).setTemporarilyOffline(eq(true), any(OfflineCause.class));
        verify(nodes.get(2), never()).setTemporarilyOffline(eq(false), any(OfflineCause.class));
        verify(nodes.get(3)).setTemporarilyOffline(eq(true), any(OfflineCause.class));
    }

    @Test
    void onConfigurationDoesNotChangeRelatedNodesIfKeepOfflineIsFalse() {
        final PoolNodeListener listener = create(false);
        final List<Computer> nodes = Arrays.asList(
                createComputer("unrelated-0", Collections.emptyList()),
                createComputer("node-1", Collections.singletonList("vdi-pool-test")));
        doReturn(nodes).when(listener).getComputers();

        listener.onConfigurationChange();

        verify(nodes.get(0), never()).setTemporarilyOffline(eq(false), any(OfflineCause.class));
        verify(nodes.get(1)).setTemporarilyOffline(eq(false), any(OfflineCause.class));
    }

    @Test
    void onConfigurationIsSafeToNullDescriptor() {
        final PoolNodeListener listener = create(true);
        doReturn(Collections.singletonList(createComputer("node-1", Collections.singletonList("vdi-pool-test"))))
                .when(listener).getComputers();
        doReturn(null).when(listener).getDescriptor();

        listener.onConfigurationChange();
    }

    private PoolNodeListener create(boolean keepOffline) {
        final PoolConfiguration.DescriptorImpl descriptor = mock(PoolConfiguration.DescriptorImpl.class);
        when(descriptor.isKeepOffline()).thenReturn(keepOffline);

        final PoolNodeListener listener = spy(new PoolNodeListener(new TestHelper.TestNodeNames()));
        doReturn(descriptor).when(listener).getDescriptor();
        return listener;
    }

    private Computer createComputer(String name, Collection<String> label) {
        final Computer computer = mock(Computer.class);
        doReturn(TestHelper.create(name, label)).when(computer).getNode();
        return computer;
    }
}