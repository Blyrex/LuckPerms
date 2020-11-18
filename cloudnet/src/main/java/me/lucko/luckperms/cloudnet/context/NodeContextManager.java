package me.lucko.luckperms.cloudnet.context;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.dytanic.cloudnet.ext.bridge.player.ICloudPlayer;
import me.lucko.luckperms.common.context.ContextManager;
import me.lucko.luckperms.common.context.QueryOptionsSupplier;
import me.lucko.luckperms.common.plugin.LuckPermsPlugin;
import me.lucko.luckperms.common.util.CaffeineFactory;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.query.QueryOptions;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class NodeContextManager extends ContextManager<ICloudPlayer, ICloudPlayer> {

    private final LoadingCache<ICloudPlayer, QueryOptions> contextsCache = CaffeineFactory.newBuilder()
            .expireAfterWrite(50, TimeUnit.MILLISECONDS)
            .build(this::calculate);

    public NodeContextManager(LuckPermsPlugin plugin) {
        super(plugin, ICloudPlayer.class, ICloudPlayer.class);
    }

    @Override
    public UUID getUniqueId(ICloudPlayer player) {
        return player.getUniqueId();
    }

    @Override
    public QueryOptionsSupplier getCacheFor(ICloudPlayer subject) {
        return new InlineQueryOptionsSupplier(subject, this.contextsCache);
    }

    @Override
    public QueryOptions formQueryOptions(ICloudPlayer subject, ImmutableContextSet contextSet) {
        return formQueryOptions(contextSet);
    }

    @Override
    protected void invalidateCache(ICloudPlayer subject) {
        this.contextsCache.invalidate(subject);
    }

    private static final class InlineQueryOptionsSupplier implements QueryOptionsSupplier {
        private final ICloudPlayer key;
        private final com.github.benmanes.caffeine.cache.LoadingCache<ICloudPlayer, QueryOptions> cache;

        private InlineQueryOptionsSupplier(ICloudPlayer key, com.github.benmanes.caffeine.cache.LoadingCache<ICloudPlayer, QueryOptions> cache) {
            this.key = key;
            this.cache = cache;
        }

        @Override
        public QueryOptions getQueryOptions() {
            return this.cache.get(this.key);
        }
    }
}
