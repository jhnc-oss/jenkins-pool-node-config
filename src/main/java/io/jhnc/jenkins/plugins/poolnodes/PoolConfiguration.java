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
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.model.labels.LabelAtom;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class PoolConfiguration extends GlobalConfiguration {
    @Extension
    public static class DescriptorImpl extends Descriptor<GlobalConfiguration> {
        private Set<LabelAtom> poolLabelAtoms;
        private Set<String> masterImages;
        private Set<String> testImages;

        public DescriptorImpl() {
            load();
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.PoolConfiguration_displayName();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            if (json.has("poolLabels")) {
                setPoolLabels(json.getString("poolLabels"));
            }
            if (json.has("masterImages")) {
                setMasterImages(json.getString("masterImages"));
            }
            if (json.has("testImages")) {
                setTestImages(json.getString("testImages"));
            }
            return super.configure(req, json);
        }

        public FormValidation doCheckPoolLabels(@QueryParameter String poolLabels) {
            return validateParameter(poolLabels, "Label");
        }

        public FormValidation doCheckMasterImages(@QueryParameter String masterImages) {
            return validateParameter(masterImages, "Master Images");
        }

        public FormValidation doCheckTestImages(@QueryParameter String testImages) {
            return validateParameter(testImages, "Test Images");
        }

        @NonNull
        public String getPoolLabels() {
            return this.<Set<LabelAtom>>ensureNotNull(poolLabelAtoms, Collections.emptySet()).stream()
                    .map(LabelAtom::getExpression)
                    .collect(Collectors.joining(" "));
        }

        @NonNull
        public Set<LabelAtom> getPoolLabelAtoms() {
            return ensureNotNull(poolLabelAtoms, Collections.emptySet());
        }

        public void setPoolLabels(@CheckForNull String labelString) {
            this.poolLabelAtoms = parseLabels(ensureNotNull(labelString, "").trim());
            save();
        }

        @NonNull
        public String getMasterImages() {
            return collectionToString(masterImages);
        }

        @NonNull
        public Collection<String> getMasterImageNames() {
            return ensureNotNull(masterImages, Collections.emptySet());
        }

        public void setMasterImages(@CheckForNull String masterImagesString) {
            this.masterImages = parseElements(masterImagesString);
            save();
        }

        @NonNull
        public String getTestImages() {
            return collectionToString(testImages);
        }

        @NonNull
        public Collection<String> getTestImageNames() {
            return ensureNotNull(testImages, Collections.emptySet());
        }

        public void setTestImages(@CheckForNull String testImagesString) {
            this.testImages = parseElements(testImagesString);
            save();
        }

        protected Set<LabelAtom> parseLabels(@NonNull String labelString) {
            return Label.parse(labelString);
        }

        @NonNull
        private <T> T ensureNotNull(@CheckForNull T value, @NonNull T fallbackValue) {
            return value == null ? fallbackValue : value;
        }

        @NonNull
        private Set<String> parseElements(@CheckForNull String str) {
            final Set<String> elements = new HashSet<>(Arrays.asList(ensureNotNull(str, "").split(" ")));
            elements.removeIf(item -> ensureNotNull(item, "").trim().isEmpty());
            return elements;
        }

        @NonNull
        private String collectionToString(@CheckForNull Collection<String> c) {
            return c == null ? "" : String.join(" ", c).trim();
        }

        @NonNull
        private FormValidation validateParameter(@CheckForNull String value, @NonNull String name) {
            if (value == null) {
                return FormValidation.error(Messages.PoolConfiguration_validationError(name));
            }
            return FormValidation.ok();
        }
    }
}
