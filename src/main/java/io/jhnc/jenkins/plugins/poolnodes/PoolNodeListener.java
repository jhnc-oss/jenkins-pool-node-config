package io.jhnc.jenkins.plugins.poolnodes;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.slaves.ComputerListener;
import hudson.slaves.OfflineCause;
import jenkins.model.Jenkins;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Extension
public class PoolNodeListener extends ComputerListener {
    private final NodeNames nodeNames;

    public PoolNodeListener() {
        this(new NodeNames());
    }

    protected PoolNodeListener(NodeNames nodeNames) {
        this.nodeNames = nodeNames;
    }


    @Override
    public void preOnline(Computer computer, Channel channel, FilePath root, TaskListener listener) {
        final var descriptor = getDescriptor();

        if (computer != null && descriptor != null) {
            final Node node = computer.getNode();

            if (nodeNames.isProdNode(node)) {
                updatePoolOnlineState(computer, descriptor);
            }

            if (shouldKeepOffline(node, descriptor)) {
                updateNodeOnlineState(computer, descriptor);
            }
        }
    }

    @Override
    public void onConfigurationChange() {
        final var descriptor = getDescriptor();

        if (descriptor != null) {
            for (final Computer computer : getComputers()) {
                if (nodeNames.isProdNode(computer.getNode())) {
                    updatePoolOnlineState(computer, descriptor);
                }

                updateNodeOnlineState(computer, descriptor);
            }
        }
    }

    @CheckForNull
    protected PoolConfiguration.DescriptorImpl getDescriptor() {
        return (PoolConfiguration.DescriptorImpl) Jenkins.get().getDescriptor(PoolConfiguration.class);
    }

    @NonNull
    protected List<Computer> getComputers() {
        return Arrays.stream(Jenkins.get().getComputers()).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void updatePoolOnlineState(@NonNull Computer computer, @NonNull PoolConfiguration.DescriptorImpl descriptor) {
        final boolean keepOffline = descriptor.isKeepOffline();
        final OfflineCause cause = keepOffline ? new PoolOfflineCause() : null;
        computer.setTemporarilyOffline(keepOffline, cause);
    }

    private void updateNodeOnlineState(@NonNull Computer computer, @NonNull PoolConfiguration.DescriptorImpl descriptor) {
        if (shouldKeepOffline(computer.getNode(), descriptor)) {
            computer.setTemporarilyOffline(true, new NodeOfflineCause());
        } else if (computer.isOffline() && computer.getOfflineCause() instanceof NodeOfflineCause) {
            computer.setTemporarilyOffline(false, null);
        }
    }

    private boolean shouldKeepOffline(@CheckForNull Node node, @NonNull PoolConfiguration.DescriptorImpl descriptor) {
        if (node == null) {
            return false;
        }
        return !Collections.disjoint(node.getAssignedLabels(), descriptor.getKeepOfflineNodesLabelAtoms());
    }


    public static class NodeOfflineCause extends OfflineCause {
        @Override
        public String toString() {
            return Messages.PoolNodeListener_NodeOfflineCause_offlineCause();
        }
    }


    public static class PoolOfflineCause extends OfflineCause {
        @Override
        public String toString() {
            return Messages.PoolNodeListener_PoolOfflineCause_offlineCause();
        }
    }
}
