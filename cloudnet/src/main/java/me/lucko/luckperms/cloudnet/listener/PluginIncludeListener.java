package me.lucko.luckperms.cloudnet.listener;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.event.EventListener;
import de.dytanic.cloudnet.driver.util.DefaultModuleHelper;
import de.dytanic.cloudnet.event.service.CloudServicePreStartEvent;
import me.lucko.luckperms.cloudnet.LPNodeBootstrap;

import java.io.File;
import java.util.Arrays;

public class PluginIncludeListener {

    private final LPNodeBootstrap lpNodeBootstrap;

    public PluginIncludeListener(LPNodeBootstrap lpNodeBootstrap) {
        this.lpNodeBootstrap = lpNodeBootstrap;
        CloudNetDriver.getInstance().getEventManager().registerListener(this);
    }

    @EventListener
    public void handleCloudServiceStart(CloudServicePreStartEvent event) {
        boolean installPlugin = this.lpNodeBootstrap.getLuckPermsNodeConfig().isEnabled() &&
                this.lpNodeBootstrap.getLuckPermsNodeConfig().getExcludedGroups()
                        .stream()
                        .noneMatch(excludedGroup -> Arrays.asList(event.getCloudService().getServiceConfiguration().getGroups()).contains(excludedGroup));

        new File(event.getCloudService().getDirectory(), "plugins").mkdirs();
        File file = new File(event.getCloudService().getDirectory(), "plugins/LuckPerms-CloudNetModule-5.2.3.jar");
        file.delete();

        if (installPlugin && DefaultModuleHelper.copyCurrentModuleInstanceFromClass(PluginIncludeListener.class, file)) {
            DefaultModuleHelper.copyPluginConfigurationFileForEnvironment(PluginIncludeListener.class,
                    event.getCloudService().getServiceConfiguration().getProcessConfig().getEnvironment(), file);
        }
    }

}
