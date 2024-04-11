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
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.model.labels.LabelAtom;
import hudson.security.Permission;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("PMD.TooManyMethods")
public class PoolConfiguration extends GlobalConfiguration {
    @Extension
    public static class DescriptorImpl extends Descriptor<GlobalConfiguration> {
        private Set<LabelAtom> poolLabelAtoms;
        private Set<String> masterImages;
        private Set<String> testImages;
        private boolean keepOffline;
        private Set<LabelAtom> keepOfflineNodes;


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
            if (json.has("keepOffline")) {
                setKeepOffline(json.getBoolean("keepOffline"));
            }
            if (json.has("keepOfflineNodes")) {
                setKeepOfflineNodes(json.getString("keepOfflineNodes"));
            }
            return super.configure(req, json);
        }

        @RequirePOST
        public FormValidation doCheckPoolLabels(@QueryParameter String poolLabels) {
            checkPermission(Jenkins.ADMINISTER);
            return validateParameter(poolLabels, "Label");
        }

        @RequirePOST
        public FormValidation doCheckMasterImages(@QueryParameter String masterImages) {
            checkPermission(Jenkins.ADMINISTER);
            return validateParameter(masterImages, "Master Images");
        }

        @RequirePOST
        public FormValidation doCheckTestImages(@QueryParameter String testImages) {
            checkPermission(Jenkins.ADMINISTER);
            return validateParameter(testImages, "Test Images");
        }

        @RequirePOST
        public FormValidation doCheckKeepOfflineNodes(@QueryParameter String nodes) {
            checkPermission(Jenkins.ADMINISTER);
            return validateParameter(nodes, "Keep offline Nodes");
        }

        @NonNull
        public String getPoolLabels() {
            return labelAtomsToString(poolLabelAtoms);
        }

        @NonNull
        public Set<LabelAtom> getPoolLabelAtoms() {
            return Objects.requireNonNullElse(poolLabelAtoms, Collections.emptySet());
        }

        public void setPoolLabels(@CheckForNull String labelString) {
            this.poolLabelAtoms = parseLabels(labelString);
            save();
        }

        @NonNull
        public String getMasterImages() {
            return collectionToString(masterImages);
        }

        @NonNull
        public Collection<String> getMasterImageNames() {
            return Objects.requireNonNullElse(masterImages, Collections.emptySet());
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
            return Objects.requireNonNullElse(testImages, Collections.emptySet());
        }

        public void setTestImages(@CheckForNull String testImagesString) {
            this.testImages = parseElements(testImagesString);
            save();
        }

        public boolean isKeepOffline() {
            return this.keepOffline;
        }

        public void setKeepOffline(boolean keepOffline) {
            this.keepOffline = keepOffline;
            save();
        }

        public String getKeepOfflineNodes() {
            return labelAtomsToString(keepOfflineNodes);
        }

        @NonNull
        public Set<LabelAtom> getKeepOfflineNodesLabelAtoms() {
            return Objects.requireNonNullElse(keepOfflineNodes, Collections.emptySet());
        }

        public void setKeepOfflineNodes(@CheckForNull String keepOfflineNodes) {
            this.keepOfflineNodes = parseLabels(keepOfflineNodes);
            save();
        }

        public void checkPermission(@NonNull Permission permission) {
            Jenkins.get().checkPermission(permission);
        }

        protected Set<LabelAtom> parseLabels(@Nullable String labelString) {
            return Label.parse(Objects.requireNonNullElse(labelString, "").trim());
        }

        @NonNull
        private Set<String> parseElements(@CheckForNull String str) {
            final Set<String> elements = new HashSet<>(Arrays.asList(Objects.requireNonNullElse(str, "").split(" ")));
            elements.removeIf(item -> Objects.requireNonNullElse(item, "").trim().isEmpty());
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

        @NonNull
        private String labelAtomsToString(Set<LabelAtom> labelAtoms) {
            return Objects.<Set<LabelAtom>>requireNonNullElse(labelAtoms, Collections.emptySet()).stream()
                    .map(LabelAtom::getExpression)
                    .collect(Collectors.joining(" "));
        }
    }
}
