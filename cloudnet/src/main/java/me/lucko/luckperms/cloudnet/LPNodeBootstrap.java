package me.lucko.luckperms.cloudnet;

import com.google.gson.Gson;
import de.dytanic.cloudnet.common.document.gson.JsonDocument;
import de.dytanic.cloudnet.common.logging.LogLevel;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.channel.ChannelMessage;
import de.dytanic.cloudnet.driver.event.EventListener;
import de.dytanic.cloudnet.driver.event.events.channel.ChannelMessageReceiveEvent;
import de.dytanic.cloudnet.driver.module.ModuleLifeCycle;
import de.dytanic.cloudnet.driver.module.ModuleTask;
import de.dytanic.cloudnet.driver.module.driver.DriverModule;
import de.dytanic.cloudnet.ext.bridge.player.ICloudPlayer;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import lombok.Getter;
import lombok.SneakyThrows;
import me.lucko.luckperms.cloudnet.config.LuckPermsNodeConfig;
import me.lucko.luckperms.cloudnet.listener.PluginIncludeListener;
import me.lucko.luckperms.common.dependencies.classloader.PluginClassLoader;
import me.lucko.luckperms.common.dependencies.classloader.ReflectionClassLoader;
import me.lucko.luckperms.common.plugin.bootstrap.LuckPermsBootstrap;
import me.lucko.luckperms.common.plugin.logging.PluginLogger;
import me.lucko.luckperms.common.plugin.scheduler.SchedulerAdapter;
import net.luckperms.api.platform.Platform;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.*;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@Getter
public class LPNodeBootstrap extends DriverModule implements LuckPermsBootstrap {

    private PluginLogger lpLogger = null;
    private final SchedulerAdapter schedulerAdapter;
    private final PluginClassLoader lpClassLoader;
    private final LPNodePlugin plugin;
    private LuckPermsNodeConfig luckPermsNodeConfig;

    private Instant startTime;

    private final CountDownLatch loadLatch = new CountDownLatch(1);
    private final CountDownLatch enableLatch = new CountDownLatch(1);


    public LPNodeBootstrap() {
        this.schedulerAdapter = new NodeSchedulerAdapter(this);
        this.lpClassLoader = new ReflectionClassLoader(this);
        this.plugin = new LPNodePlugin(this, this.getDriver());
    }

    @ModuleTask(event = ModuleLifeCycle.LOADED)
    public void onModuleLoad() {
        this.getDriver().getEventManager().registerListener(this);
        this.luckPermsNodeConfig = this.loadModuleConfig();
        this.lpLogger = new NodeLogger(this.getDriver());
        this.plugin.load();
        this.startTime = Instant.now();
    }

    @ModuleTask(event = ModuleLifeCycle.STARTED)
    public void onStart() {
        new PluginIncludeListener(this);
        this.plugin.enable();
        this.enableLatch.countDown();
    }

    @ModuleTask(event = ModuleLifeCycle.STOPPED)
    public void onStop() {
        this.plugin.disable();
    }

    @Override
    public PluginLogger getPluginLogger() {
        if (this.lpLogger == null) {
            throw new IllegalStateException("Logger has not been initialised yet");
        }
        return this.lpLogger;
    }

    @Override
    public SchedulerAdapter getScheduler() {
        return this.schedulerAdapter;
    }

    @Override
    public PluginClassLoader getPluginClassLoader() {
        return this.lpClassLoader;
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
        if (!this.getModuleWrapper().getDataFolder().exists()) this.getModuleWrapper().getDataFolder().mkdirs();
        return this.getModuleWrapper().getDataFolder().toPath();
    }

    @Override
    public Path getConfigDirectory() {
        if (!this.getModuleWrapper().getDataFolder().exists()) this.getModuleWrapper().getDataFolder().mkdirs();
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

    @SneakyThrows
    public LuckPermsNodeConfig loadModuleConfig() {
        this.getModuleWrapper().getDataFolder().mkdirs();
        Gson gson = new Gson();
        File configFile = new File(super.getModuleWrapper().getDataFolder() + "//config.json");
        if (!configFile.exists()) {
            configFile.createNewFile();
            List<String> list = new ArrayList<>();
            list.add("ExampleGroup");
            this.saveContentToFile(configFile, gson.toJson(new LuckPermsNodeConfig(true, list)));
        }
        return gson.fromJson(new FileReader(configFile), LuckPermsNodeConfig.class);
    }

    public void saveContentToFile(File file, String content) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(content);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventListener
    public void handle(ChannelMessageReceiveEvent event) {

        if (event.getMessage() == null || !event.getChannel().equals("luckperms_cloudnet") || !event.getMessage().equals("query_config"))
            return;

        event.setQueryResponse(ChannelMessage.buildResponseFor(event.getChannelMessage())
                .json(JsonDocument.newDocument().append("filePath", new File(this.getModuleWrapper().getDataFolder(), "config.yml").getAbsolutePath()))
                .build());
    }
}
