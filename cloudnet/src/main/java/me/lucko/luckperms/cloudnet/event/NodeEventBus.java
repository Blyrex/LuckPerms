package me.lucko.luckperms.cloudnet.event;

import me.lucko.luckperms.cloudnet.LPNodePlugin;
import me.lucko.luckperms.common.api.LuckPermsApiProvider;
import me.lucko.luckperms.common.event.AbstractEventBus;
import me.lucko.luckperms.common.plugin.LuckPermsPlugin;

public class NodeEventBus extends AbstractEventBus<LPNodePlugin> {

    public NodeEventBus(LuckPermsPlugin plugin, LuckPermsApiProvider apiProvider) {
        super(plugin, apiProvider);
    }

    @Override
    protected LPNodePlugin checkPlugin(Object plugin) throws IllegalArgumentException {
        if (plugin instanceof LPNodePlugin) {
            return (LPNodePlugin) plugin;
        }
        return null;
    }

}
