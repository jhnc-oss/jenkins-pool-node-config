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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.LabelFinder;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import jenkins.model.Jenkins;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Extension
public class PoolLabelFinder extends LabelFinder {
    private final NodeNames nodeNames;

    public PoolLabelFinder() {
        this(new NodeNames());
    }

    protected PoolLabelFinder(NodeNames nodeNames) {
        this.nodeNames = nodeNames;
    }

    @Override
    public Collection<LabelAtom> findLabels(@NonNull Node node) {
        if (nodeNames.isMasterNode(node)) {
            return assignedLabels(PoolImageLabel.MASTER);
        }
        if (nodeNames.isTestNode(node)) {
            return assignedLabels(PoolImageLabel.TEST);
        }
        if (nodeNames.isProdNode(node)) {
            return Stream.concat(assignedLabels(PoolImageLabel.PRODUCTION).stream(),
                    getConfiguredLabel().stream()).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    protected Collection<LabelAtom> assignedLabels(PoolImageLabel image) {
        return LabelAtom.parse(image.getLabelName());
    }

    protected Collection<LabelAtom> getConfiguredLabel() {
        final PoolConfiguration.DescriptorImpl descriptor = (PoolConfiguration.DescriptorImpl) Jenkins
                .get().getDescriptor(PoolConfiguration.class);
        return descriptor == null ? null : descriptor.getPoolLabelAtoms();
    }
}
