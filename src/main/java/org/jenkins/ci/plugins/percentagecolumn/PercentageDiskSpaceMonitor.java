package org.jenkins.ci.plugins.percentagecolumn;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.node_monitors.AbstractAsyncNodeMonitorDescriptor;
import hudson.node_monitors.NodeMonitor;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Checks % of usage disk space of the remote FS root.
 * Requires Mustang.
 *
 * @author Victor Martinez
 */
@ExportedBean
public class PercentageDiskSpaceMonitor extends NodeMonitor {

    @Extension
    public static final class DESCRIPTOR extends AbstractAsyncNodeMonitorDescriptor<DiskSpace> {

        @Override
        protected Callable<DiskSpace,IOException> createCallable(Computer c) {
            Node node = c.getNode();
            if (node == null) return null;

            FilePath p = node.getRootPath();
            if(p==null) return null;

            return p.asCallableWith(new GetDiskSpace());
        }

        public String getDisplayName() {
            return Messages.PercentageMonitor_DisplayName();
        }

        public NodeMonitor newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new PercentageDiskSpaceMonitor();
        }
    }

    protected static final class GetDiskSpace extends MasterToSlaveFileCallable<DiskSpace> {
        public DiskSpace invoke(File f, VirtualChannel channel) throws IOException {
            long total = f.getTotalSpace();
            if(total<=0)    return null;
            long usable = f.getUsableSpace();
            if(usable<=0)    return null;
            double percentage = 100 - Math.round((double) usable / (double) total * 100);
            return new DiskSpace(f.getCanonicalPath(), total, usable, percentage);
        }
        private static final long serialVersionUID = 1L;
    }

    @ExportedBean
    public static final class DiskSpace implements Serializable {
        private final String path;
        @Exported
        public final long totalSpace;
        @Exported
        public final long usableSpace;
        @Exported
        public final double percentage;
        private static final long serialVersionUID = 2L;

        public DiskSpace(String path, long totalSpace, long usableSpace, double percentage) {
            this.path = path;
            this.totalSpace = totalSpace;
            this.usableSpace = usableSpace;
            this.percentage = percentage;
        }

        public String toString() {
            return this.percentage + " %";
        }

        @Exported
        public String getPath() {
            return this.path;
        }

        public String toHtml() {
            return toString();
        }
    }
}
