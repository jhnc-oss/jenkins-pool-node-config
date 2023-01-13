/*
 * MIT License
 *
 * Copyright (c) 2021-2023 jhnc-oss
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
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import jenkins.model.Jenkins;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

public class NodeNames {
    private static final Locale COMPARE_LOCALE = Locale.ENGLISH;
    private static final String POOL_NODE_LABEL_PREFIX = "vdi-pool";
    private final Function<String, Set<LabelAtom>> labelParser;

    public NodeNames() {
        this(Label::parse);
    }

    protected NodeNames(@NonNull Function<String, Set<LabelAtom>> labelParser) {
        this.labelParser = labelParser;
    }

    public boolean isMasterNode(@CheckForNull Node node) {
        final PoolConfiguration.DescriptorImpl descriptor = getDescriptor();
        return descriptor != null && isIn(descriptor.getMasterImageNames(), node);
    }

    public boolean isTestNode(@CheckForNull Node node) {
        final PoolConfiguration.DescriptorImpl descriptor = getDescriptor();
        return descriptor != null && isIn(descriptor.getTestImageNames(), node);
    }

    public boolean isProdNode(@CheckForNull Node node) {
        if (node == null || isTestNode(node) || isMasterNode(node)) {
            return false;
        }

        return labelParser.apply(node.getLabelString()).stream().anyMatch(labelAtom -> {
            final String label = labelAtom.getExpression();
            return label != null && startsWithIgnoreCase(label, POOL_NODE_LABEL_PREFIX);
        });
    }

    @CheckForNull
    protected PoolConfiguration.DescriptorImpl getDescriptor() {
        return (PoolConfiguration.DescriptorImpl) Jenkins.get().getDescriptor(PoolConfiguration.class);
    }

    private boolean isIn(@NonNull Collection<String> labels, @CheckForNull Node node) {
        if (node == null) {
            return false;
        }

        final String nodeName = node.getNodeName();
        return !nodeName.isEmpty()
                && labels.stream().anyMatch(name -> startsWithIgnoreCase(nodeName, name));
    }

    private boolean startsWithIgnoreCase(@NonNull String str, @NonNull String prefix) {
        return str.toLowerCase(COMPARE_LOCALE).startsWith(prefix.toLowerCase(COMPARE_LOCALE));
    }
}
