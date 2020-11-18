package me.lucko.luckperms.cloudnet;

import de.dytanic.cloudnet.common.logging.ILogger;
import de.dytanic.cloudnet.common.logging.LogLevel;
import de.dytanic.cloudnet.ext.bridge.player.ICloudPlayer;
import me.lucko.luckperms.common.sender.SenderFactory;
import net.kyori.adventure.text.Component;
import net.luckperms.api.util.Tristate;

import java.util.UUID;

public class NodeSenderFactory extends SenderFactory<LPNodePlugin, ILogger> {

    public NodeSenderFactory(LPNodePlugin plugin) {
        super(plugin);
    }

    @Override
    protected UUID getUniqueId(ILogger sender) {
        return UUID.randomUUID();
    }

    @Override
    protected String getName(ILogger sender) {
        return "Console";
    }

    @Override
    protected void sendMessage(ILogger sender, Component message) {
        sender.log(LogLevel.COMMAND, message.insertion());
    }

    @Override
    protected Tristate getPermissionValue(ILogger sender, String node) {
        return null;
    }

    @Override
    protected boolean hasPermission(ILogger sender, String node) {
        return false;
    }

    @Override
    protected void performCommand(ILogger sender, String command) {
        // Do nothing
    }


}
