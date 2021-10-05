/*
 * MIT License
 *
 * Copyright (c) 2021 jhnc-oss
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.jhnc.jenkins.plugins.poolnodes;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.labels.LabelAtom;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueTaskDispatcher;

@Extension
public class PoolImageBlockingDispatcher extends QueueTaskDispatcher {
    private final NodeNames nodeNames;

    public PoolImageBlockingDispatcher() {
        this(new NodeNames());
    }

    protected PoolImageBlockingDispatcher(@NonNull NodeNames nodeNames) {
        this.nodeNames = nodeNames;
    }


    @CheckForNull
    @Override
    public CauseOfBlockage canTake(Node node, Queue.BuildableItem item) {
        if (isRestrictedNode(node)) {
            return new CauseOfBlockage() {
                @Override
                public String getShortDescription() {
                    return Messages.PoolImageBlockingDispatcher_restricted(node.getNodeName());
                }
            };
        }

        return null;
    }

    protected boolean hasMasterImageLabel(@NonNull Node node) {
        return node.getAssignedLabels().contains(LabelAtom.get(PoolImageLabel.MASTER.getLabelName()));
    }

    private boolean isRestrictedNode(Node node) {
        return nodeNames.isMasterNode(node) || hasMasterImageLabel(node);
    }
}
