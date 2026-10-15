package io.cresco.skeletonweb;

import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.Executor;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;

/** MsgEvent handler for the plugin. Add actions to the execute* methods (switch on the "action" param). */
public class PluginExecutor implements Executor {

    private final PluginBuilder plugin;
    private final CLogger logger;

    public PluginExecutor(PluginBuilder pluginBuilder) {
        this.plugin = pluginBuilder;
        this.logger = plugin.getLogger(PluginExecutor.class.getName(), CLogger.Level.Info);
    }

    @Override
    public MsgEvent executeCONFIG(MsgEvent incoming) {
        return null;
    }

    @Override
    public MsgEvent executeDISCOVER(MsgEvent incoming) {
        return null;
    }

    @Override
    public MsgEvent executeERROR(MsgEvent incoming) {
        return null;
    }

    @Override
    public MsgEvent executeINFO(MsgEvent incoming) {
        return null;
    }

    @Override
    public MsgEvent executeEXEC(MsgEvent incoming) {
        return null;
    }

    @Override
    public MsgEvent executeWATCHDOG(MsgEvent incoming) {
        return null;
    }

    @Override
    public MsgEvent executeKPI(MsgEvent incoming) {
        return null;
    }
}
