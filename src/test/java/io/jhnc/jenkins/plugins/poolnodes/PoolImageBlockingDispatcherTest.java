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

import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.labels.LabelAtom;
import hudson.model.queue.CauseOfBlockage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;

import java.util.Objects;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PoolImageBlockingDispatcherTest {
    private static final Queue.BuildableItem item = new Queue.BuildableItem(mock(Queue.NotWaitingItem.class));


    @Test
    void dispatcherAcceptsItemIfNonRestrictedNode() {
        final PoolImageBlockingDispatcher dispatcher = create();
        doReturn(false).when(dispatcher).hasMasterImageLabel(any());
        assertThat(dispatcher.canTake(TestHelper.create("agent-0"), item)).isNull();
    }

    @Test
    void dispatcherBlocksItemIfRestrictedNode() {
        final PoolImageBlockingDispatcher dispatcher = create();
        doReturn(false).when(dispatcher).hasMasterImageLabel(any());
        assertThat(dispatcher.canTake(TestHelper.create("master.pool0"), item)).isNotNull();
    }

    @Test
    void dispatcherBlocksItemIfRestrictedLabel() {
        final PoolImageBlockingDispatcher dispatcher = create();
        doReturn(true).when(dispatcher).hasMasterImageLabel(any());
        final Node node = TestHelper.create("node-0");
        when(node.getAssignedLabels()).thenReturn(Sets.newSet(new LabelAtom("abc"),
                new LabelAtom("vdi-image-master"), new LabelAtom("def")));
        assertThat(dispatcher.canTake(node, item)).isNotNull();
    }

    @Test
    void dispatcherBlocksItemIfAtLeastOneRestrictedNode() {
        final PoolImageBlockingDispatcher dispatcher = create();
        doReturn(false).when(dispatcher).hasMasterImageLabel(any());
        assertThat(dispatcher.canTake(TestHelper.create("master.pool0"), item)).isNotNull();
    }

    @Test
    void dispatcherBlocksItemIfRestrictedNodeWithUniqueId() {
        final PoolImageBlockingDispatcher dispatcher = create();
        doReturn(false).when(dispatcher).hasMasterImageLabel(any());
        assertThat(dispatcher.canTake(TestHelper.create("master.pool1-1fa97cd8"), item)).isNotNull();
    }

    @Test
    void dispatcherBlockMessageContainsShortMessageWithNodeName() {
        final PoolImageBlockingDispatcher dispatcher = create();
        doReturn(false).when(dispatcher).hasMasterImageLabel(any());
        final CauseOfBlockage cause = dispatcher.canTake(TestHelper.create("master.pool0"), item);
        assertThat(Objects.requireNonNull(cause).getShortDescription()).contains("master.pool0");
    }

    @Test
    void dispatcherIgnoresCases() {
        final PoolImageBlockingDispatcher dispatcher = create();
        doReturn(false).when(dispatcher).hasMasterImageLabel(any());
        assertThat(dispatcher.canTake(TestHelper.create("master.pool0"), item)).isNotNull();
        assertThat(dispatcher.canTake(TestHelper.create("MASTER.POOL1"), item)).isNotNull();
        assertThat(dispatcher.canTake(TestHelper.create("mAsTER.pOoL1"), item)).isNotNull();
    }

    private PoolImageBlockingDispatcher create() {
        return Mockito.spy(new PoolImageBlockingDispatcher(new TestHelper.TestNodeNames()));
    }
}
