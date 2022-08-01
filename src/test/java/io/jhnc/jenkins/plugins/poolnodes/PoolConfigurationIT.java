/*
 * MIT License
 *
 * Copyright (c) 2021-2022 jhnc-oss
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

import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import org.apache.commons.beanutils.PropertyUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;

@Tag("IT")
@WithJenkins
class PoolConfigurationIT {

    @ParameterizedTest
    @ViewTextFieldSource
    void entryCanBeSavedEmpty(String entry, JenkinsRule r) throws Exception {
        final PoolConfiguration.DescriptorImpl descriptor = getDescriptor(r);
        final HtmlPage page = goToConfigure(r);

        r.submit(page.getFormByName("config"));
        assertThat(getValueFromDescriptor(descriptor, entry)).isEmpty();

        submitEntry(r, page, entry, "");
        assertThat(getValueFromDescriptor(descriptor, entry)).isEmpty();

        submitEntry(r, page, entry, "     ");
        assertThat(getValueFromDescriptor(descriptor, entry)).isEmpty();
    }

    @ParameterizedTest
    @ViewTextFieldSource
    void entryValueIsSet(String entry, JenkinsRule r) throws Exception {
        final PoolConfiguration.DescriptorImpl descriptor = getDescriptor(r);

        submitEntry(r, goToConfigure(r), entry, "value-a value-b value-c");
        assertThat(getValuesFromDescriptor(descriptor, entry)).containsExactly("value-a", "value-b", "value-c");
    }

    @ParameterizedTest
    @ViewTextFieldSource
    void entryValueIsUpdated(String entry, JenkinsRule r) throws Exception {
        final PoolConfiguration.DescriptorImpl descriptor = getDescriptor(r);

        submitEntry(r, goToConfigure(r), entry, "value-a value-b value-c");
        assertThat(getValuesFromDescriptor(descriptor, entry)).containsExactly("value-a", "value-b", "value-c");

        submitEntry(r, goToConfigure(r), entry, "value-a value-1 value-b value-2 value-3");
        assertThat(getValuesFromDescriptor(descriptor, entry)).containsExactly("value-1", "value-2", "value-3", "value-a", "value-b");
    }

    @ParameterizedTest
    @ViewTextFieldSource
    void entryIsSavedAcrossConfigurations(String entry, JenkinsRule r) throws Exception {
        final PoolConfiguration.DescriptorImpl descriptor = getDescriptor(r);

        submitEntry(r, goToConfigure(r), entry, "value-1 value-2");
        assertThat(getValuesFromDescriptor(descriptor, entry)).containsExactly("value-1", "value-2");

        final HtmlTextInput entryField2 = goToConfigure(r).getElementByName("_." + entry);
        assertThat(entryField2.getValueAttribute()).isEqualTo("value-1 value-2");
    }

    @Test
    void keepOfflineValueIsSet(JenkinsRule r) throws Exception {
        final PoolConfiguration.DescriptorImpl descriptor = getDescriptor(r);

        submitEntry(r, goToConfigure(r), "keepOffline", true);

        assertThat(getValueFromDescriptor(descriptor, "keepOffline")).isEqualTo("true");
    }

    @Test
    void keepOfflineValueIsUpdated(JenkinsRule r) throws Exception {
        final PoolConfiguration.DescriptorImpl descriptor = getDescriptor(r);

        submitEntry(r, goToConfigure(r), "keepOffline", true);
        assertThat(getValueFromDescriptor(descriptor, "keepOffline")).isEqualTo("true");

        submitEntry(r, goToConfigure(r), "keepOffline", false);
        assertThat(getValueFromDescriptor(descriptor, "keepOffline")).isEqualTo("false");
    }

    @Test
    void keepOfflineIsSavedAcrossConfigurations(JenkinsRule r) throws Exception {
        final PoolConfiguration.DescriptorImpl descriptor = getDescriptor(r);

        submitEntry(r, goToConfigure(r), "keepOffline", true);
        assertThat(getValueFromDescriptor(descriptor, "keepOffline")).isEqualTo("true");

        final HtmlCheckBoxInput entryElement2 = goToConfigure(r).getElementByName("_.keepOffline");
        assertThat(entryElement2.isChecked()).isTrue();
    }

    private PoolConfiguration.DescriptorImpl getDescriptor(JenkinsRule r) {
        return r.jenkins.getDescriptorByType(PoolConfiguration.DescriptorImpl.class);
    }

    private String getValueFromDescriptor(PoolConfiguration.DescriptorImpl descriptor, String entry)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        return PropertyUtils.getProperty(descriptor, entry).toString();
    }

    private Collection<String> getValuesFromDescriptor(PoolConfiguration.DescriptorImpl descriptor, String entry)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        return Arrays.asList(getValueFromDescriptor(descriptor, entry).split(" "));
    }

    private void submitEntry(JenkinsRule r, HtmlPage page, String entry, String value) throws Exception {
        final HtmlTextInput entryElement = page.getElementByName("_." + entry);
        entryElement.setValueAttribute(value);
        r.submit(page.getFormByName("config"));
    }

    private void submitEntry(JenkinsRule r, HtmlPage page, String entry, boolean value) throws Exception {
        final HtmlCheckBoxInput entryElement = page.getElementByName("_." + entry);
        entryElement.setChecked(value);
        r.submit(page.getFormByName("config"));
    }

    private HtmlPage goToConfigure(JenkinsRule r) throws Exception {
        return r.createWebClient().goTo("configure");
    }

    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ValueSource(strings = {"poolLabels", "masterImages", "testImages"})
    private @interface ViewTextFieldSource {
    }
}
