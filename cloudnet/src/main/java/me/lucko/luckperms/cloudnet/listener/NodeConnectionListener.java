package me.lucko.luckperms.cloudnet.listener;

import de.dytanic.cloudnet.driver.event.EventListener;
import de.dytanic.cloudnet.ext.bridge.event.BridgeProxyPlayerDisconnectEvent;
import de.dytanic.cloudnet.ext.bridge.event.BridgeProxyPlayerLoginRequestEvent;
import me.lucko.luckperms.cloudnet.LPNodePlugin;
import me.lucko.luckperms.common.model.User;
import me.lucko.luckperms.common.plugin.util.AbstractConnectionListener;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NodeConnectionListener extends AbstractConnectionListener {

    private final LPNodePlugin plugin;

    public NodeConnectionListener(LPNodePlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @EventListener
    public void handleConnect(BridgeProxyPlayerLoginRequestEvent event) {
        CompletableFuture.runAsync(() -> {
            UUID uuid = event.getNetworkConnectionInfo().getUniqueId();
            String name = event.getNetworkConnectionInfo().getName();
            try {
                User user = loadUser(uuid, name);
                recordConnection(uuid);
                this.plugin.getEventDispatcher().dispatchPlayerLoginProcess(uuid, name, user);
            } catch (Exception ex) {
                this.plugin.getLogger().severe("Exception occurred whilst loading data for " + uuid + " - " + name, ex);
                // there was some error loading
                this.plugin.getEventDispatcher().dispatchPlayerLoginProcess(uuid, name, null);
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @EventListener
    public void handlePlayerQuit(BridgeProxyPlayerDisconnectEvent event) {
        handleDisconnect(event.getNetworkConnectionInfo().getUniqueId());
    }
}
