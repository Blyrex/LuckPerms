package me.lucko.luckperms.cloudnet;

import de.dytanic.cloudnet.common.logging.LogLevel;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import me.lucko.luckperms.common.plugin.logging.PluginLogger;

public class NodeLogger implements PluginLogger {

    private final CloudNetDriver driver;

    public NodeLogger(CloudNetDriver driver) {
        this.driver = driver;
    }

    @Override
    public void info(String s) {
        this.driver.getLogger().log(LogLevel.INFO, s);
    }

    @Override
    public void warn(String s) {
        this.driver.getLogger().log(LogLevel.WARNING, s);
    }

    @Override
    public void warn(String s, Throwable t) {
        this.driver.getLogger().log(LogLevel.INFO, t.getMessage());
    }

    @Override
    public void severe(String s) {
        this.driver.getLogger().log(LogLevel.IMPORTANT, s);
    }

    @Override
    public void severe(String s, Throwable t) {
        this.driver.getLogger().log(LogLevel.IMPORTANT, t.getMessage());
    }
}
