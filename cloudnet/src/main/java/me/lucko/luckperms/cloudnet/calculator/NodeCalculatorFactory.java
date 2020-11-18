package me.lucko.luckperms.cloudnet.calculator;

import com.google.common.collect.ImmutableList;
import me.lucko.luckperms.cloudnet.LPNodePlugin;
import me.lucko.luckperms.common.cacheddata.CacheMetadata;
import me.lucko.luckperms.common.calculator.CalculatorFactory;
import me.lucko.luckperms.common.calculator.PermissionCalculator;
import me.lucko.luckperms.common.calculator.processor.*;
import me.lucko.luckperms.common.config.ConfigKeys;
import net.luckperms.api.query.QueryOptions;

public class NodeCalculatorFactory implements CalculatorFactory {

    private final LPNodePlugin plugin;

    public NodeCalculatorFactory(LPNodePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public PermissionCalculator build(QueryOptions queryOptions, CacheMetadata metadata) {
        ImmutableList.Builder<PermissionProcessor> processors = ImmutableList.builder();

        processors.add(new MapProcessor());

        if (this.plugin.getConfiguration().get(ConfigKeys.APPLYING_REGEX)) {
            processors.add(new RegexProcessor());
        }

        if (this.plugin.getConfiguration().get(ConfigKeys.APPLYING_WILDCARDS)) {
            processors.add(new WildcardProcessor());
        }

        if (this.plugin.getConfiguration().get(ConfigKeys.APPLYING_WILDCARDS_SPONGE)) {
            processors.add(new SpongeWildcardProcessor());
        }

        return new PermissionCalculator(this.plugin, metadata, processors.build());
    }

}
