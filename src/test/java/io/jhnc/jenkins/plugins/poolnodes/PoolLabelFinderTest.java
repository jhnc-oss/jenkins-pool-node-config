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

import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

class PoolLabelFinderTest {
    @Test
    void returnsEmptyOnUnrelatedNode() {
        final Node node0 = TestHelper.create("unrelated-node-0");
        final Node node1 = TestHelper.create("unrelated-node-1", Collections.singletonList("vdi-image-abc"));
        final PoolLabelFinder labelFinder = create();
        assertThat(labelFinder.findLabels(node0)).isEmpty();
        assertThat(labelFinder.findLabels(node1)).isEmpty();
    }

    @Test
    void returnsLabelsOnMasterNode() {
        final Node node = TestHelper.create("master.pool1");
        final PoolLabelFinder labelFinder = create();
        assertThat(labelFinder.findLabels(node)).containsExactly(new LabelAtom("vdi-image-master"));
    }

    @Test
    void returnsLabelsOnTestNode() {
        final Node node = TestHelper.create("test.pool0");
        final PoolLabelFinder labelFinder = create();
        assertThat(labelFinder.findLabels(node)).containsExactly(new LabelAtom("vdi-image-test"));
    }

    @Test
    void returnsLabelsOnProdNode() {
        final Node node = TestHelper.create("node-0", Collections.singletonList("vdi-pool-test"));
        final PoolLabelFinder labelFinder = create();
        doReturn(Collections.emptySet()).when(labelFinder).getConfiguredLabel();
        assertThat(labelFinder.findLabels(node)).containsExactly(new LabelAtom("vdi-image-prod"));
    }

    @Test
    void returnsConfiguredLabelsOnProdNode() {
        final Node node = TestHelper.create("node-0", Collections.singletonList("vdi-pool-test"));
        final PoolLabelFinder labelFinder = create();
        doReturn(new HashSet<>(Arrays.asList(new LabelAtom("label-0"), new LabelAtom("label-1"))))
                .when(labelFinder).getConfiguredLabel();
        assertThat(labelFinder.findLabels(node)).containsExactly(new LabelAtom("vdi-image-prod"),
                new LabelAtom("label-0"), new LabelAtom("label-1"));
    }

    private PoolLabelFinder create() {
        final PoolLabelFinder labelFinder = Mockito.spy(new PoolLabelFinder(new TestHelper.TestNodeNames()));
        doAnswer(invocation -> Collections.singleton(new LabelAtom(invocation.getArgument(0, PoolImageLabel.class).getLabelName())))
                .when(labelFinder).assignedLabels(any(PoolImageLabel.class));
        return labelFinder;
    }
}