package jenkins.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.PersistentDescriptor;
import hudson.security.Permission;
import hudson.slaves.ComputerRetentionWork;
import java.util.logging.Logger;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Configures check interval for computer retention.
 *
 * @author Jakob Ackermann
 */
@Extension(ordinal = 401) @Symbol("computerRetentionCheckInterval")
public class GlobalComputerRetentionCheckIntervalConfiguration extends GlobalConfiguration implements PersistentDescriptor {
    /**
     * For {@link hudson.slaves.ComputerRetentionWork#getRecurrencePeriod()}
     */
    private int computerRetentionCheckInterval = 60;

    /**
     * Gets the check interval for computer retention.
     *
     * @since TODO
     */
    public int getComputerRetentionCheckInterval() {
        if (computerRetentionCheckInterval <= 0) {
            LOGGER.info("computerRetentionCheckInterval must be greater than zero, falling back to 60s");
            return 60;
        }
        return computerRetentionCheckInterval;
    }

    /**
     * Updates the check interval for computer retention and restarts the check cycle.
     *
     * @param interval new check interval in seconds
     * @throws IllegalArgumentException interval must be greater than zero
     * @since TODO
     */
    private void setComputerRetentionCheckInterval(int interval) throws IllegalArgumentException {
        if (interval <= 0) {
            throw new IllegalArgumentException("interval must be greater than zero");
        }
        computerRetentionCheckInterval = interval;
        save();
        ExtensionList.lookupSingleton(ComputerRetentionWork.class).restart();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        try {
            final int interval = json.getInt("computerRetentionCheckInterval");
            setComputerRetentionCheckInterval(interval);
            return true;
        } catch (IllegalArgumentException e) {
            throw new FormException(e, "computerRetentionCheckInterval");
        } catch (JSONException e) {
            throw new FormException(e.getMessage(), "computerRetentionCheckInterval");
        }
    }

    @NonNull
    @Override
    public Permission getRequiredGlobalConfigPagePermission() {
        return Jenkins.ADMINISTER;
    }

    private static final Logger LOGGER = Logger.getLogger(GlobalComputerRetentionCheckIntervalConfiguration.class.getName());
}
