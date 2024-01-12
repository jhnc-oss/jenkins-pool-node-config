/*
 * MIT License
 *
 * Copyright (c) 2021-2024 jhnc-oss
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
import hudson.Util;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class TestHelper {
    private TestHelper() {
    }

    public static Node create(String name) {
        final Node node = mock(Node.class);
        when(node.getNodeName()).thenReturn(name);
        return node;
    }

    public static Node create(String name, Collection<String> label) {
        final Node node = create(name);
        final Set<LabelAtom> labelAtoms = label.stream().map(LabelAtom::new).collect(Collectors.toSet());
        when(node.getAssignedLabels()).thenReturn(labelAtoms);
        when(node.getLabelString()).thenReturn(String.join(" ", label));
        return node;
    }

    public static Set<LabelAtom> simpleParseLabel(String labelString) {
        return Arrays.stream(Util.fixNull(labelString).split(" "))
                .filter(s -> !s.isEmpty())
                .map(LabelAtom::new)
                .collect(Collectors.toSet());
    }

    public static class TestNodeNames extends NodeNames {
        private final PoolConfiguration.DescriptorImpl descriptor;

        public TestNodeNames() {
            super(TestHelper::simpleParseLabel);
            this.descriptor = mock(PoolConfiguration.DescriptorImpl.class);
            when(descriptor.getMasterImageNames()).thenReturn(Arrays.asList("host.is-master", "master.pool0", "master.pool1", "nOdE37990"));
            when(descriptor.getTestImageNames()).thenReturn(Arrays.asList("host.is-test", "test.pool0", "test.pool1", "NoDe51843"));
        }

        @CheckForNull
        @Override
        protected PoolConfiguration.DescriptorImpl getDescriptor() {
            return descriptor;
        }
    }
}
