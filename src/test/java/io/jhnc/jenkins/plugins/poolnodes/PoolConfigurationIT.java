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

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import org.apache.commons.beanutils.PropertyUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
@ExtendWith(JenkinsJUnitAdapter.JenkinsParameterResolver.class)
class PoolConfigurationIT {

    @ParameterizedTest
    @ViewFieldSource
    void entryCanBeSavedEmpty(String entry, JenkinsJUnitAdapter.JUnitJenkinsRule r) throws Exception {
        final PoolConfiguration.DescriptorImpl descriptor = getDescriptor(r);
        final HtmlPage page = r.createWebClient().goTo("configure");

        r.submit(page.getFormByName("config"));
        assertThat(getValueFromDescriptor(descriptor, entry)).isEmpty();

        submitEntry(r, page, entry, "");
        assertThat(getValueFromDescriptor(descriptor, entry)).isEmpty();

        submitEntry(r, page, entry, "     ");
        assertThat(getValueFromDescriptor(descriptor, entry)).isEmpty();
    }

    @ParameterizedTest
    @ViewFieldSource
    void entryValueIsSet(String entry, JenkinsJUnitAdapter.JUnitJenkinsRule r) throws Exception {
        final PoolConfiguration.DescriptorImpl descriptor = getDescriptor(r);
        final HtmlPage page = r.createWebClient().goTo("configure");

        submitEntry(r, page, entry, "value-a value-b value-c");
        assertThat(getValuesFromDescriptor(descriptor, entry)).containsExactly("value-a", "value-b", "value-c");
    }

    @ParameterizedTest
    @ViewFieldSource
    void entryValueIsUpdated(String entry, JenkinsJUnitAdapter.JUnitJenkinsRule r) throws Exception {
        final PoolConfiguration.DescriptorImpl descriptor = getDescriptor(r);
        final HtmlPage page = r.createWebClient().goTo("configure");

        submitEntry(r, page, entry, "value-a value-b value-c");
        assertThat(getValuesFromDescriptor(descriptor, entry)).containsExactly("value-a", "value-b", "value-c");

        final HtmlPage page2 = r.createWebClient().goTo("configure");
        submitEntry(r, page2, entry, "value-a value-1 value-b value-2 value-3");
        assertThat(getValuesFromDescriptor(descriptor, entry)).containsExactly("value-1", "value-2", "value-3", "value-a", "value-b");
    }

    @ParameterizedTest
    @ViewFieldSource
    void entryIsSavedAcrossConfigurations(String entry, JenkinsJUnitAdapter.JUnitJenkinsRule r) throws Exception {
        final PoolConfiguration.DescriptorImpl descriptor = getDescriptor(r);
        final HtmlPage page = r.createWebClient().goTo("configure");

        submitEntry(r, page, entry, "value-1 value-2");
        assertThat(getValuesFromDescriptor(descriptor, entry)).containsExactly("value-1", "value-2");

        final HtmlPage page2 = r.createWebClient().goTo("configure");
        final HtmlTextInput entryField2 = page2.getElementByName("_." + entry);
        assertThat(entryField2.getValueAttribute()).isEqualTo("value-1 value-2");
    }

    private PoolConfiguration.DescriptorImpl getDescriptor(JenkinsJUnitAdapter.JUnitJenkinsRule r) {
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

    private void submitEntry(JenkinsJUnitAdapter.JUnitJenkinsRule r, HtmlPage page, String entry, String value) throws Exception {
        final HtmlTextInput entryElement = page.getElementByName("_." + entry);
        entryElement.setValueAttribute(value);
        r.submit(page.getFormByName("config"));
    }


    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ValueSource(strings = {"poolLabels", "masterImages", "testImages"})
    private @interface ViewFieldSource {
    }
}