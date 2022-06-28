package io.jhnc.jenkins.plugins.poolnodes;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.slaves.ComputerListener;
import hudson.slaves.OfflineCause;
import jenkins.model.Jenkins;

import java.util.Arrays;
import java.util.List;

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
        if (computer != null && nodeNames.isProdNode(computer.getNode())) {
            updateOnlineState(computer);
        }
    }

    @Override
    public void onConfigurationChange() {
        for (Computer computer : getComputers()) {
            if (nodeNames.isProdNode(computer.getNode())) {
                updateOnlineState(computer);
            }
        }
    }

    @CheckForNull
    protected PoolConfiguration.DescriptorImpl getDescriptor() {
        return (PoolConfiguration.DescriptorImpl) Jenkins.get().getDescriptor(PoolConfiguration.class);
    }

    @NonNull
    protected List<Computer> getComputers() {
        return Arrays.asList(Jenkins.get().getComputers());
    }

    private void updateOnlineState(@NonNull Computer computer) {
        final PoolConfiguration.DescriptorImpl descriptor = getDescriptor();

        if (descriptor != null) {
            computer.setTemporarilyOffline(descriptor.isKeepOffline(), new OfflineCause() {
                @Override
                public String toString() {
                    return Messages.PoolNodeListener_offlineCause();
                }
            });
        }
    }
}
