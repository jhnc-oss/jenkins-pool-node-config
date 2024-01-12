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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.doReturn;

class NodeNamesTest {
    @Test
    void masterNodes() {
        final NodeNames nodeNames = new TestHelper.TestNodeNames();
        assertThat(nodeNames.isMasterNode(TestHelper.create("host.is-master"))).isTrue();
        assertThat(nodeNames.isMasterNode(TestHelper.create("master.pool0"))).isTrue();
    }

    @Test
    void testNodes() {
        final NodeNames nodeNames = new TestHelper.TestNodeNames();
        assertThat(nodeNames.isTestNode(TestHelper.create("host.is-test"))).isTrue();
        assertThat(nodeNames.isTestNode(TestHelper.create("test.pool0"))).isTrue();
    }

    @Test
    void masterNodeNodeAcceptsExactMatch() {
        final NodeNames nodeNames = new TestHelper.TestNodeNames();
        assertThat(nodeNames.isMasterNode(TestHelper.create("host.isnt-master"))).isFalse();
        assertThat(nodeNames.isMasterNode(TestHelper.create("host-is-master"))).isFalse();
        assertThat(nodeNames.isMasterNode(TestHelper.create("host.is-master"))).isTrue();
    }

    @Test
    void masterNodeNodeAcceptsStartOfNameOnly() {
        final NodeNames nodeNames = new TestHelper.TestNodeNames();
        assertThat(nodeNames.isMasterNode(TestHelper.create("host.is-master.ignore.this.sub-string"))).isTrue();
        assertThat(nodeNames.isMasterNode(TestHelper.create("NodE37990.oF.PooL.n900"))).isTrue();
        assertThat(nodeNames.isMasterNode(TestHelper.create("node37990.OF.anOTHER.pool"))).isTrue();

        assertThat(nodeNames.isMasterNode(TestHelper.create("Xhost.is-master"))).isFalse();
    }

    @Test
    void masterNodeNodeIsCaseInsensitive() {
        final NodeNames nodeNames = new TestHelper.TestNodeNames();
        assertThat(nodeNames.isMasterNode(TestHelper.create("hOSt.IS-mAstER"))).isTrue();
    }

    @Test
    void masterNodeNodeIsSafeToNullOrEmpty() {
        final NodeNames nodeNames = new TestHelper.TestNodeNames();
        assertThat(nodeNames.isMasterNode(TestHelper.create(""))).isFalse();
        assertThat(nodeNames.isMasterNode(null)).isFalse();
    }

    @Test
    void testNodeAcceptsExactMatch() {
        final NodeNames nodeNames = new TestHelper.TestNodeNames();
        assertThat(nodeNames.isTestNode(TestHelper.create("host.isnt-test"))).isFalse();
        assertThat(nodeNames.isTestNode(TestHelper.create("host.is-test"))).isTrue();
    }

    @Test
    void testNodeAcceptsStartOfNameOnly() {
        final NodeNames nodeNames = new TestHelper.TestNodeNames();
        assertThat(nodeNames.isTestNode(TestHelper.create("host.is-test.ignore.this.sub-string"))).isTrue();
        assertThat(nodeNames.isTestNode(TestHelper.create("node51843.NodE.PO.OL34"))).isTrue();
        assertThat(nodeNames.isTestNode(TestHelper.create("nODE51843.NodE.PoOL.xy.z64"))).isTrue();

        assertThat(nodeNames.isTestNode(TestHelper.create("Xhost.is-test"))).isFalse();
    }

    @Test
    void testNodeMasterIsCaseInsensitive() {
        final NodeNames nodeNames = new TestHelper.TestNodeNames();
        assertThat(nodeNames.isTestNode(TestHelper.create("HosT.iS-TEst"))).isTrue();
    }

    @Test
    void testNodeIsSafeToNullOrEmpty() {
        final NodeNames nodeNames = new TestHelper.TestNodeNames();
        assertThat(nodeNames.isTestNode(TestHelper.create(""))).isFalse();
        assertThat(nodeNames.isTestNode(null)).isFalse();
    }

    @Test
    void safeToNullDescriptor() {
        final NodeNames nodeNames = Mockito.spy(NodeNames.class);
        doReturn(null).when(nodeNames).getDescriptor();

        assertThat(nodeNames.isMasterNode(TestHelper.create("x"))).isFalse();
        assertThat(nodeNames.isTestNode(TestHelper.create("y"))).isFalse();
    }

    @Test
    void prodNodeAcceptsLabelMatch() {
        final NodeNames nodeNames = new TestHelper.TestNodeNames();
        assertThat(nodeNames.isProdNode(TestHelper.create("node", Collections.singletonList("vdi-pool")))).isTrue();
        assertThat(nodeNames.isProdNode(TestHelper.create("node", Collections.singletonList("vdi-pool-prod")))).isTrue();
        assertThat(nodeNames.isProdNode(TestHelper.create("node", Collections.singletonList("vdi-pool-test")))).isTrue();
        assertThat(nodeNames.isProdNode(TestHelper.create("node", Collections.singletonList("vdi-pool-staging")))).isTrue();
        assertThat(nodeNames.isProdNode(TestHelper.create("node", Arrays.asList("aa", "vdi-pool", "bb")))).isTrue();
        assertThat(nodeNames.isProdNode(TestHelper.create("node", Arrays.asList("aa", "vdi-pool-prod", "b")))).isTrue();
        assertThat(nodeNames.isProdNode(TestHelper.create("node", Arrays.asList("aa", "vdi-pool-test", "bbb")))).isTrue();
        assertThat(nodeNames.isProdNode(TestHelper.create("node", Arrays.asList("vdi-pool-staging", "bb")))).isTrue();
        assertThat(nodeNames.isProdNode(TestHelper.create("node", Arrays.asList("aaa", "vdi-pool-", "bbb")))).isTrue();

        assertThat(nodeNames.isProdNode(TestHelper.create("node", Collections.singletonList("vdi_pool")))).isFalse();
        assertThat(nodeNames.isProdNode(TestHelper.create("node", Collections.singletonList("vdipool")))).isFalse();
        assertThat(nodeNames.isProdNode(TestHelper.create("node", Collections.singletonList("vdi")))).isFalse();
        assertThat(nodeNames.isProdNode(TestHelper.create("node", Collections.singletonList("vdi-")))).isFalse();
        assertThat(nodeNames.isProdNode(TestHelper.create("node", Collections.singletonList("vdi-unrelated")))).isFalse();
        assertThat(nodeNames.isProdNode(TestHelper.create("node", Collections.singletonList("vdi-image-test")))).isFalse();
        assertThat(nodeNames.isProdNode(TestHelper.create("node", Collections.singletonList("unrelated label")))).isFalse();
        assertThat(nodeNames.isProdNode(TestHelper.create("node", Collections.singletonList("unrelated-label")))).isFalse();
    }

    @Test
    void prodNodeLabelIsCaseInsensitive() {
        final NodeNames nodeNames = new TestHelper.TestNodeNames();
        assertThat(nodeNames.isProdNode(TestHelper.create("node-0", Collections.singletonList("vDI-pOOl")))).isTrue();
    }

    @Test
    void prodNodeIsSafeToNullOrEmpty() {
        final NodeNames nodeNames = new TestHelper.TestNodeNames();
        assertThat(nodeNames.isProdNode(TestHelper.create("node-0", Collections.emptyList()))).isFalse();
        assertThat(nodeNames.isProdNode(null)).isFalse();
    }

    @Test
    void prodNodeIsExcludedByOtherTypes() {
        final NodeNames nodeNames = new TestHelper.TestNodeNames();
        assertThat(nodeNames.isProdNode(TestHelper.create("node", Collections.singletonList("vdi-pool")))).isTrue();
        assertThat(nodeNames.isProdNode(TestHelper.create("host.is-master", Collections.singletonList("vdi-pool")))).isFalse();
        assertThat(nodeNames.isProdNode(TestHelper.create("host.is-test", Collections.singletonList("vdi-pool")))).isFalse();
        assertThat(nodeNames.isProdNode(TestHelper.create("host.is-master"))).isFalse();
        assertThat(nodeNames.isProdNode(TestHelper.create("host.is-test"))).isFalse();
    }
}
