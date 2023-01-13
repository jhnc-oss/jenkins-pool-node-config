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

import hudson.model.Descriptor;
import hudson.model.labels.LabelAtom;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.kohsuke.stapler.StaplerRequest;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

class PoolConfigurationTest {
    @Test
    void displayNameIsSet() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        assertThat(descriptor.getDisplayName()).isNotEmpty();
    }

    @Test
    void labelEmptyOnDefault() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        assertThat(descriptor.getPoolLabels()).isEmpty();
        assertThat(descriptor.getPoolLabelAtoms()).isEmpty();
    }

    @Test
    void labelIsSafeToNullOrEmpty() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        descriptor.setPoolLabels(null);
        assertThat(descriptor.getPoolLabels()).isEmpty();
        assertThat(descriptor.getPoolLabelAtoms()).isEmpty();

        descriptor.setPoolLabels("");
        assertThat(descriptor.getPoolLabels()).isEmpty();
        assertThat(descriptor.getPoolLabelAtoms()).isEmpty();

        descriptor.setPoolLabels("    ");
        assertThat(descriptor.getPoolLabels()).isEmpty();
        assertThat(descriptor.getPoolLabelAtoms()).isEmpty();
    }

    @Test
    void setLabelTrimsString() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        descriptor.setPoolLabels(" aa bb    cc  ");
        assertThat(descriptor.getPoolLabels()).isEqualTo("aa bb cc");
    }

    @Test
    void setLabelSetsLabel() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        descriptor.setPoolLabels("abc");
        assertThat(descriptor.getPoolLabels()).isEqualTo("abc");
        assertThat(descriptor.getPoolLabelAtoms()).containsExactly(new LabelAtom("abc"));
    }

    @Test
    void setLabelSavesUpdate() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        descriptor.setPoolLabels("xyz");
        assertThat(descriptor.getPoolLabels()).isEqualTo("xyz");
        assertThat(descriptor.getPoolLabelAtoms()).containsExactly(new LabelAtom("xyz"));
        verify(descriptor).save();
    }

    @Test
    void getLabelAtoms() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        descriptor.setPoolLabels("abc def ghi");
        assertThat(descriptor.getPoolLabelAtoms()).containsExactly(new LabelAtom("abc"),
                new LabelAtom("def"), new LabelAtom("ghi"));
    }

    @Test
    void configureSetsLabels() throws Descriptor.FormException {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        final StaplerRequest req = mock(StaplerRequest.class);
        final JSONObject json = new JSONObject().element("poolLabels", "label-1 label-2 label-3");

        descriptor.configure(req, json);
        assertThat(descriptor.getPoolLabels().split(" "))
                .asList().containsExactly("label-1", "label-2", "label-3");
        assertThat(descriptor.getPoolLabelAtoms()).containsExactly(new LabelAtom("label-1"),
                new LabelAtom("label-2"), new LabelAtom("label-3"));
    }

    @Test
    void labelFormValidationChecksPermission() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        assertThat(descriptor.doCheckPoolLabels("x").kind).isEqualTo(FormValidation.Kind.OK);
        verify(descriptor).checkPermission(Jenkins.ADMINISTER);
    }

    @Test
    void labelFormValidationAcceptsLabel() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        assertThat(descriptor.doCheckPoolLabels("label-0").kind).isEqualTo(FormValidation.Kind.OK);
    }

    @Test
    void labelFormValidationAcceptsEmpty() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        assertThat(descriptor.doCheckPoolLabels("").kind).isEqualTo(FormValidation.Kind.OK);
        assertThat(descriptor.doCheckPoolLabels(" ").kind).isEqualTo(FormValidation.Kind.OK);
    }

    @Test
    void labelFormValidationRejectsNull() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        assertThat(descriptor.doCheckPoolLabels(null).kind).isEqualTo(FormValidation.Kind.ERROR);
    }

    @Test
    void masterImagesEmptyOnDefault() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        assertThat(descriptor.getMasterImages()).isEmpty();
        assertThat(descriptor.getMasterImageNames()).isEmpty();
    }

    @Test
    void masterImagesIsSafeToNullOrEmpty() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        descriptor.setMasterImages(null);
        assertThat(descriptor.getMasterImages()).isEmpty();

        descriptor.setMasterImages("");
        assertThat(descriptor.getMasterImages()).isEmpty();
        assertThat(descriptor.getMasterImageNames()).isEmpty();

        descriptor.setMasterImages("    ");
        assertThat(descriptor.getMasterImages()).isEmpty();
        assertThat(descriptor.getMasterImageNames()).isEmpty();
    }

    @Test
    void setMasterImagesTrimsString() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        descriptor.setMasterImages(" host-1 host-2    host-3  ");
        assertThat(descriptor.getMasterImages().split(" "))
                .asList().containsExactly("host-1", "host-2", "host-3");
        assertThat(descriptor.getMasterImageNames()).containsExactly("host-1", "host-2", "host-3");
    }

    @Test
    void setMasterImagesSetsLabel() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        descriptor.setMasterImages("wxyz");
        assertThat(descriptor.getMasterImages()).isEqualTo("wxyz");
        assertThat(descriptor.getMasterImageNames()).containsExactly("wxyz");
    }

    @Test
    void setMasterImagesSavesUpdate() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        descriptor.setMasterImages("abc");
        assertThat(descriptor.getMasterImages()).isEqualTo("abc");
        assertThat(descriptor.getMasterImageNames()).containsExactly("abc");
        verify(descriptor).save();
    }

    @Test
    void configureSetsMasterImages() throws Descriptor.FormException {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        final StaplerRequest req = mock(StaplerRequest.class);
        final JSONObject json = new JSONObject().element("masterImages", "host-a host-b host-c");

        descriptor.configure(req, json);
        assertThat(descriptor.getMasterImages().split(" "))
                .asList().containsExactly("host-a", "host-b", "host-c");
        assertThat(descriptor.getMasterImageNames()).containsExactly("host-a", "host-b", "host-c");
    }

    @Test
    void masterImageFormValidationChecksPermission() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        assertThat(descriptor.doCheckMasterImages("x").kind).isEqualTo(FormValidation.Kind.OK);
        verify(descriptor).checkPermission(Jenkins.ADMINISTER);
    }

    @Test
    void masterImageFormValidationAcceptsImageName() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        assertThat(descriptor.doCheckMasterImages("host-0").kind).isEqualTo(FormValidation.Kind.OK);
    }

    @Test
    void masterImagesFormValidationAcceptsEmpty() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        assertThat(descriptor.doCheckMasterImages("").kind).isEqualTo(FormValidation.Kind.OK);
        assertThat(descriptor.doCheckMasterImages(" ").kind).isEqualTo(FormValidation.Kind.OK);
    }

    @Test
    void masterImageFormValidationRejectsNull() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        assertThat(descriptor.doCheckMasterImages(null).kind).isEqualTo(FormValidation.Kind.ERROR);
    }

    @Test
    void testImagesEmptyOnDefault() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        assertThat(descriptor.getTestImages()).isEmpty();
        assertThat(descriptor.getTestImageNames()).isEmpty();
    }

    @Test
    void testImagesIsSafeToNullOrEmpty() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        descriptor.setTestImages(null);
        assertThat(descriptor.getTestImages()).isEmpty();
        assertThat(descriptor.getTestImageNames()).isEmpty();

        descriptor.setTestImages("");
        assertThat(descriptor.getTestImages()).isEmpty();
        assertThat(descriptor.getTestImageNames()).isEmpty();

        descriptor.setMasterImages("    ");
        assertThat(descriptor.getTestImages()).isEmpty();
        assertThat(descriptor.getTestImageNames()).isEmpty();
    }

    @Test
    void setTestImagesTrimsString() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        descriptor.setTestImages(" host-t1 host-t2   host-t3  ");
        assertThat(descriptor.getTestImages().split(" "))
                .asList().containsExactly("host-t1", "host-t2", "host-t3");
        assertThat(descriptor.getTestImageNames()).containsExactly("host-t1", "host-t2", "host-t3");
    }

    @Test
    void setTestImagesSetsLabel() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        descriptor.setTestImages("xxyyzz");
        assertThat(descriptor.getTestImages()).isEqualTo("xxyyzz");
        assertThat(descriptor.getTestImageNames()).containsExactly("xxyyzz");
    }

    @Test
    void setTestImagesSavesUpdate() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        descriptor.setTestImages("aabbcc");
        assertThat(descriptor.getTestImages()).isEqualTo("aabbcc");
        assertThat(descriptor.getTestImageNames()).containsExactly("aabbcc");
        verify(descriptor).save();
    }

    @Test
    void configureSetsTestImages() throws Descriptor.FormException {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        final StaplerRequest req = mock(StaplerRequest.class);
        final JSONObject json = new JSONObject().element("testImages", "host.a host.b host.c");

        descriptor.configure(req, json);
        assertThat(descriptor.getTestImages().split(" "))
                .asList().containsExactly("host.a", "host.b", "host.c");
        assertThat(descriptor.getTestImageNames()).containsExactly("host.a", "host.b", "host.c");
    }

    @Test
    void testImageFormValidationChecksPermission() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        assertThat(descriptor.doCheckTestImages("x").kind).isEqualTo(FormValidation.Kind.OK);
        verify(descriptor).checkPermission(Jenkins.ADMINISTER);
    }

    @Test
    void testImageFormValidationAcceptsImageName() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        assertThat(descriptor.doCheckTestImages("host-x").kind).isEqualTo(FormValidation.Kind.OK);
    }

    @Test
    void testImagesFormValidationAcceptsEmpty() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        assertThat(descriptor.doCheckTestImages("").kind).isEqualTo(FormValidation.Kind.OK);
        assertThat(descriptor.doCheckTestImages(" ").kind).isEqualTo(FormValidation.Kind.OK);
    }

    @Test
    void testImageFormValidationRejectsNull() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        assertThat(descriptor.doCheckTestImages(null).kind).isEqualTo(FormValidation.Kind.ERROR);
    }


    @Test
    void keepOfflineFalseOnDefault() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        assertThat(descriptor.isKeepOffline()).isFalse();
    }

    @Test
    void setKeepOfflineSetsKeepOffline() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        descriptor.setKeepOffline(true);
        assertThat(descriptor.isKeepOffline()).isTrue();
    }

    @Test
    void setKeepOfflineSavesUpdate() {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        descriptor.setKeepOffline(true);
        assertThat(descriptor.isKeepOffline()).isTrue();
        verify(descriptor).save();
    }

    @Test
    void configureSetsKeepOffline() throws Descriptor.FormException {
        final PoolConfiguration.DescriptorImpl descriptor = create();
        final StaplerRequest req = mock(StaplerRequest.class);
        final JSONObject json = new JSONObject().element("keepOffline", true);

        descriptor.configure(req, json);
        assertThat(descriptor.isKeepOffline()).isTrue();
    }

    private PoolConfiguration.DescriptorImpl create() {
        final PoolConfiguration.DescriptorImpl descriptor = mock(PoolConfiguration.DescriptorImpl.class,
                withSettings().defaultAnswer(CALLS_REAL_METHODS));
        doAnswer(invocation -> TestHelper.simpleParseLabel(invocation.getArgument(0, String.class)))
                .when(descriptor).parseLabels(anyString());
        doNothing().when(descriptor).load();
        doNothing().when(descriptor).save();
        doNothing().when(descriptor).checkPermission(Jenkins.ADMINISTER);
        return descriptor;
    }
}