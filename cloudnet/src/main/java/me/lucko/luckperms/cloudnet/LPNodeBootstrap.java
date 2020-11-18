package me.lucko.luckperms.cloudnet;

import de.dytanic.cloudnet.driver.module.ModuleLifeCycle;
import de.dytanic.cloudnet.driver.module.ModuleTask;
import de.dytanic.cloudnet.driver.module.driver.DriverModule;
import de.dytanic.cloudnet.ext.bridge.player.ICloudPlayer;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import me.lucko.luckperms.common.dependencies.classloader.PluginClassLoader;
import me.lucko.luckperms.common.dependencies.classloader.ReflectionClassLoader;
import me.lucko.luckperms.common.plugin.bootstrap.LuckPermsBootstrap;
import me.lucko.luckperms.common.plugin.logging.JavaPluginLogger;
import me.lucko.luckperms.common.plugin.logging.PluginLogger;
import me.lucko.luckperms.common.plugin.scheduler.SchedulerAdapter;
import net.luckperms.api.platform.Platform;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class LPNodeBootstrap extends DriverModule implements LuckPermsBootstrap {


    private PluginLogger logger = null;
    private final SchedulerAdapter schedulerAdapter;
    private final PluginClassLoader classLoader;
    private final LPNodePlugin plugin;

    private Instant startTime;

    private final CountDownLatch loadLatch = new CountDownLatch(1);
    private final CountDownLatch enableLatch = new CountDownLatch(1);


    public LPNodeBootstrap() {
        this.schedulerAdapter = new NodeSchedulerAdapter(this);
        this.classLoader = new ReflectionClassLoader(this);
        this.plugin = new LPNodePlugin(this, this.getDriver());
    }

    @ModuleTask(event = ModuleLifeCycle.LOADED)
    public void onModuleLoad() {
        this.logger = new NodeLogger(this.getDriver());

        this.plugin.load();
        this.startTime = Instant.now();
    }

    @ModuleTask(event = ModuleLifeCycle.STARTED)
    public void onStart() {
        this.plugin.enable();
        this.enableLatch.countDown();
    }

    @ModuleTask(event = ModuleLifeCycle.STOPPED)
    public void onStop() {
        this.plugin.enable();
        this.plugin.disable();
    }

    @Override
    public PluginLogger getPluginLogger() {
        if (this.logger == null) {
            throw new IllegalStateException("Logger has not been initialised yet");
        }
        return this.logger;
    }

    @Override
    public SchedulerAdapter getScheduler() {
        return this.schedulerAdapter;
    }

    @Override
    public PluginClassLoader getPluginClassLoader() {
        return this.classLoader;
    }

    @Override
    public CountDownLatch getLoadLatch() {
        return this.loadLatch;
    }

    @Override
    public CountDownLatch getEnableLatch() {
        return this.enableLatch;
    }

    @Override
    public Instant getStartupTime() {
        return this.startTime;
    }

    @Override
    public Platform.Type getType() {
        return Platform.Type.NODE;
    }

    @Override
    public String getServerBrand() {
        return "Node";
    }

    @Override
    public String getServerVersion() {
        return "1.0";
    }

    @Override
    public @Nullable String getServerName() {
        return this.getDriver().getComponentName();
    }

    @Override
    public Path getDataDirectory() {
        if(!this.getModuleWrapper().getDataFolder().exists()) this.getModuleWrapper().getDataFolder().mkdirs();
        return this.getModuleWrapper().getDataFolder().toPath();
    }

    @Override
    public Path getConfigDirectory() {
        if(!this.getModuleWrapper().getDataFolder().exists()) this.getModuleWrapper().getDataFolder().mkdirs();
        return this.getModuleWrapper().getDataFolder().toPath();
    }

    @Override
    public InputStream getResourceStream(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

    @Override
    public Optional<ICloudPlayer> getPlayer(UUID uniqueId) {
        return Optional.ofNullable(this.getDriver().getServicesRegistry().getFirstService(IPlayerManager.class).getOnlinePlayer(uniqueId));
    }

    @Override
    public Optional<UUID> lookupUniqueId(String username) {
        return Optional.empty();
    }

    @Override
    public Optional<String> lookupUsername(UUID uniqueId) {
        return Optional.empty();
    }

    @Override
    public int getPlayerCount() {
        return this.getDriver().getServicesRegistry().getFirstService(IPlayerManager.class).getOnlineCount();
    }

    @Override
    public Collection<String> getPlayerList() {
        return this.getDriver().getServicesRegistry().getFirstService(IPlayerManager.class).onlinePlayers().asNames();
    }

    @Override
    public Collection<UUID> getOnlinePlayers() {
        return this.getDriver().getServicesRegistry().getFirstService(IPlayerManager.class).onlinePlayers().asUUIDs();
    }

    @Override
    public boolean isPlayerOnline(UUID uniqueId) {
        return this.getDriver().getServicesRegistry().getFirstService(IPlayerManager.class).getOnlinePlayer(uniqueId) != null;
    }

    @Override
    public @Nullable String identifyClassLoader(ClassLoader classLoader) {
        return null;
    }

    @Override
    public String getGroup() {
        return super.getGroup();
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public String getVersion() {
        return super.getVersion();
    }

    @Override
    public String getWebsite() {
        return super.getWebsite();
    }

    @Override
    public String getDescription() {
        return super.getDescription();
    }
}
