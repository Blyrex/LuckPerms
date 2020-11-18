package me.lucko.luckperms.cloudnet;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import lombok.Getter;
import lombok.SneakyThrows;
import me.lucko.luckperms.cloudnet.calculator.NodeCalculatorFactory;
import me.lucko.luckperms.cloudnet.context.NodeContextManager;
import me.lucko.luckperms.cloudnet.event.NodeEventBus;
import me.lucko.luckperms.cloudnet.listener.NodeConnectionListener;
import me.lucko.luckperms.common.api.LuckPermsApiProvider;
import me.lucko.luckperms.common.calculator.CalculatorFactory;
import me.lucko.luckperms.common.command.CommandManager;
import me.lucko.luckperms.common.config.generic.adapter.ConfigurationAdapter;
import me.lucko.luckperms.common.context.ContextManager;
import me.lucko.luckperms.common.event.AbstractEventBus;
import me.lucko.luckperms.common.event.EventDispatcher;
import me.lucko.luckperms.common.messaging.MessagingFactory;
import me.lucko.luckperms.common.model.User;
import me.lucko.luckperms.common.model.manager.group.StandardGroupManager;
import me.lucko.luckperms.common.model.manager.track.StandardTrackManager;
import me.lucko.luckperms.common.model.manager.user.StandardUserManager;
import me.lucko.luckperms.common.plugin.AbstractLuckPermsPlugin;
import me.lucko.luckperms.common.plugin.bootstrap.LuckPermsBootstrap;
import me.lucko.luckperms.common.sender.Sender;
import me.lucko.luckperms.common.tasks.CacheHousekeepingTask;
import me.lucko.luckperms.common.tasks.ExpireTemporaryTask;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.query.QueryOptions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Getter
public class LPNodePlugin extends AbstractLuckPermsPlugin {

    private final LPNodeBootstrap bootstrap;
    private final CloudNetDriver cloudNetDriver;

    private NodeConnectionListener connectionListener;
    private StandardUserManager userManager;
    private StandardGroupManager groupManager;
    private StandardTrackManager trackManager;
    private NodeContextManager contextManager;
    private NodeSenderFactory senderFactory;

    public LPNodePlugin(LPNodeBootstrap bootstrap, CloudNetDriver cloudNetDriver) {
        this.bootstrap = bootstrap;
        this.cloudNetDriver = cloudNetDriver;
    }

    @Override
    public EventDispatcher getEventDispatcher() {
        return super.getEventDispatcher();
    }

    @Override
    protected void setupSenderFactory() {
        this.senderFactory = new NodeSenderFactory(this);
    }

    @Override
    protected ConfigurationAdapter provideConfigurationAdapter() {
        return new NodeConfigAdapter(this, this.resolveConfig());
    }

    @Override
    protected void registerPlatformListeners() {
        this.connectionListener = new NodeConnectionListener(this);
        this.cloudNetDriver.getEventManager().registerListener(this.connectionListener);
    }

    @Override
    protected MessagingFactory<?> provideMessagingFactory() {
        return new MessagingFactory<>(this);
    }

    @Override
    protected void registerCommands() {

    }

    @Override
    protected void setupManagers() {
        this.userManager = new StandardUserManager(this);
        this.groupManager = new StandardGroupManager(this);
        this.trackManager = new StandardTrackManager(this);
    }

    @Override
    protected CalculatorFactory provideCalculatorFactory() {
        return new NodeCalculatorFactory(this);
    }

    @Override
    protected void setupContextManager() {
        this.contextManager = new NodeContextManager(this);
    }

    @Override
    protected void setupPlatformHooks() {

    }

    @Override
    protected AbstractEventBus<?> provideEventBus(LuckPermsApiProvider apiProvider) {
        return new NodeEventBus(this, apiProvider);
    }

    @Override
    protected void registerApiOnPlatform(LuckPerms api) {

    }

    @Override
    protected void registerHousekeepingTasks() {
        this.bootstrap.getScheduler().asyncRepeating(new ExpireTemporaryTask(this), 3, TimeUnit.SECONDS);
        this.bootstrap.getScheduler().asyncRepeating(new CacheHousekeepingTask(this), 2, TimeUnit.MINUTES);
    }

    @Override
    protected void performFinalSetup() {

    }

    @Override
    public LuckPermsBootstrap getBootstrap() {
        return this.bootstrap;
    }

    @Override
    public CommandManager getCommandManager() {
        return null;
    }

    @Override
    public Optional<QueryOptions> getQueryOptionsForUser(User user) {
        return this.bootstrap.getPlayer(user.getUniqueId()).map(player -> this.contextManager.getQueryOptions(player));
    }

    @Override
    public Stream<Sender> getOnlineSenders() {
        return Stream.empty();
    }

    @Override
    public Sender getConsoleSender() {
        return this.senderFactory.wrap(this.cloudNetDriver.getLogger());
    }

    @SneakyThrows
    private File resolveConfig() {
        File configFile = new File(this.bootstrap.getModuleWrapper().getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            this.bootstrap.getModuleWrapper().getDataFolder().mkdirs();
            Files.copy(getClass().getClassLoader().getResourceAsStream("config.yml"), configFile.toPath(), new StandardCopyOption[]{StandardCopyOption.REPLACE_EXISTING});
        }
        return configFile;
    }
}
