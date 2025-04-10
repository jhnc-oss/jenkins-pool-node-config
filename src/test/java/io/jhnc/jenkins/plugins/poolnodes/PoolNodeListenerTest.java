package io.jhnc.jenkins.plugins.poolnodes;

import hudson.model.Computer;
import hudson.model.labels.LabelAtom;
import hudson.slaves.OfflineCause;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
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

        verify(computer, never()).setTemporaryOfflineCause(any());
    }

    @Test
    void preOnlineKeepsNodeOnlineIfKeepOfflineIsFalse() {
        final Computer computer = createComputer("node-0", Collections.singletonList("vdi-pool-test"));

        final PoolNodeListener listener = create(false);
        listener.preOnline(computer, null, null, null);

        verify(computer).setTemporaryOfflineCause(isNull());
    }

    @Test
    void preOnlineKeepsNodeOfflineIfKeepOfflineIsTrue() {
        final Computer computer = createComputer("node-0", Collections.singletonList("vdi-pool-test"));

        final PoolNodeListener listener = create(true);
        listener.preOnline(computer, null, null, null);

        verify(computer).setTemporaryOfflineCause(any(PoolNodeListener.PoolOfflineCause.class));
    }

    @Test
    void preOnlineKeepsOfflineNodesOffline() {
        final Computer computer = createComputer("node-0", Collections.singletonList("keep-offline-node"));

        final PoolNodeListener listener = create(true);
        listener.preOnline(computer, null, null, null);

        verify(computer).setTemporaryOfflineCause(any(PoolNodeListener.NodeOfflineCause.class));
    }

    @Test
    void preOnlineKeepsUnrelatedNodeOnline() {
        final Computer computer = createComputer("node-0", Collections.singletonList("unrelated-node"));

        final PoolNodeListener listener = create(true);
        listener.preOnline(computer, null, null, null);

        verify(computer, never()).setTemporaryOfflineCause(any());
    }

    @Test
    void preOnlineIsSafeToNullDescriptor() {
        final Computer computer = createComputer("node-0", List.of("vdi-pool-test", "keep-offline-node"));

        final PoolNodeListener listener = create(true);
        doReturn(null).when(listener).getDescriptor();

        listener.preOnline(computer, null, null, null);
    }

    @Test
    void preOnlineIsSafeToNullNode() {
        final Computer computer = mock(Computer.class);
        doReturn(null).when(computer).getNode();

        final PoolNodeListener listener = create(true);

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

        verify(nodes.get(0), never()).setTemporaryOfflineCause(any());
        verify(nodes.get(1)).setTemporaryOfflineCause(any(PoolNodeListener.PoolOfflineCause.class));
        verify(nodes.get(2), never()).setTemporaryOfflineCause(any());
        verify(nodes.get(3)).setTemporaryOfflineCause(any(PoolNodeListener.PoolOfflineCause.class));
    }

    @Test
    void onConfigurationDoesNotChangeRelatedNodesIfKeepOfflineIsFalse() {
        final PoolNodeListener listener = create(false);
        final List<Computer> nodes = Arrays.asList(
                createComputer("unrelated-0", Collections.emptyList()),
                createComputer("node-1", Collections.singletonList("vdi-pool-test")));
        doReturn(nodes).when(listener).getComputers();

        listener.onConfigurationChange();

        verify(nodes.get(0), never()).setTemporaryOfflineCause(any());
        verify(nodes.get(1)).setTemporaryOfflineCause(isNull());
    }

    @Test
    void onConfigurationChangeUpdatesRelatedNodesOffKeepOfflineNode() {
        final PoolNodeListener listener = create(true);
        final Computer shouldComeOnline = createComputer("node-0", Collections.singletonList("offline-already"));
        doReturn(true).when(shouldComeOnline).isOffline();
        doReturn(new PoolNodeListener.NodeOfflineCause()).when(shouldComeOnline).getOfflineCause();
        final List<Computer> nodes = Arrays.asList(
                createComputer("unrelated-0", Collections.emptyList()),
                createComputer("node-0", Collections.singletonList("keep-offline-node")),
                createComputer("node-1", Collections.singletonList("keep-offline-node")),
                createComputer("unrelated-1", Collections.emptyList()),
                shouldComeOnline);
        doReturn(nodes).when(listener).getComputers();

        listener.onConfigurationChange();

        verify(nodes.get(0), never()).setTemporaryOfflineCause(any());
        verify(nodes.get(1)).setTemporaryOfflineCause(any(PoolNodeListener.NodeOfflineCause.class));
        verify(nodes.get(2)).setTemporaryOfflineCause(any(PoolNodeListener.NodeOfflineCause.class));
        verify(nodes.get(3), never()).setTemporaryOfflineCause(any());
        verify(nodes.get(4)).setTemporaryOfflineCause(isNull());
    }

    @Test
    void onConfigurationChangeIgnoresUnrelatedNodesIfKeepOfflineNode() {
        final PoolNodeListener listener = create(true);
        final Computer alreadyOffline = createComputer("already-offline", Collections.emptyList());
        doReturn(true).when(alreadyOffline).isOffline();
        doReturn(mock(OfflineCause.class)).when(alreadyOffline).getOfflineCause();
        doReturn(Collections.singletonList(alreadyOffline)).when(listener).getComputers();

        listener.onConfigurationChange();

        verify(alreadyOffline, never()).setTemporaryOfflineCause(any());
    }

    @Test
    void onConfigurationIsSafeToNullDescriptor() {
        final PoolNodeListener listener = create(true);
        doReturn(Collections.singletonList(createComputer("node-1", List.of("vdi-pool-test", "keep-offline-node"))))
                .when(listener).getComputers();
        doReturn(null).when(listener).getDescriptor();

        listener.onConfigurationChange();
    }

    private PoolNodeListener create(boolean keepOffline) {
        final PoolConfiguration.DescriptorImpl descriptor = mock(PoolConfiguration.DescriptorImpl.class);
        when(descriptor.isKeepOffline()).thenReturn(keepOffline);
        when(descriptor.getKeepOfflineNodesLabelAtoms()).thenReturn(Set.of(new LabelAtom("keep-offline-node")));

        final PoolNodeListener listener = spy(new PoolNodeListener(new TestHelper.TestNodeNames()));
        doReturn(descriptor).when(listener).getDescriptor();
        return listener;
    }

    private Computer createComputer(String name, Collection<String> label) {
        final Computer computer = mock(Computer.class);
        doReturn(TestHelper.simpleParseLabel(String.join(" ", label))).when(computer).getAssignedLabels();
        doReturn(TestHelper.create(name, label)).when(computer).getNode();
        return computer;
    }
}