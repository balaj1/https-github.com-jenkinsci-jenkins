package jenkins.timemachine.pluginchange;

import jenkins.timemachine.PluginSnapshot;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Restricted(NoExternalUse.class)
public class Enabled extends PluginChange {
    public Enabled(PluginSnapshot plugin) {
        super(plugin);
    }
}
