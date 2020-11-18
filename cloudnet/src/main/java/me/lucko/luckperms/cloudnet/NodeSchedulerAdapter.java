package me.lucko.luckperms.cloudnet;

import me.lucko.luckperms.common.plugin.scheduler.AbstractJavaScheduler;
import me.lucko.luckperms.common.plugin.scheduler.SchedulerAdapter;

import java.util.concurrent.Executor;

public class NodeSchedulerAdapter extends AbstractJavaScheduler implements SchedulerAdapter {
    private final Executor sync;

    public NodeSchedulerAdapter(LPNodeBootstrap bootstrap) {
        this.sync = r -> bootstrap.getDriver().getTaskScheduler().schedule(r);
    }

    @Override
    public Executor sync() {
        return this.sync;
    }

}

