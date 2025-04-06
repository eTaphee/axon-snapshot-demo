package im.etap.config

import org.axonframework.common.caching.Cache
import org.axonframework.common.caching.JCacheAdapter
import org.axonframework.common.caching.WeakReferenceCache
import org.axonframework.common.jpa.EntityManagerProvider
import org.axonframework.common.transaction.TransactionManager
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition
import org.axonframework.eventsourcing.SnapshotTriggerDefinition
import org.axonframework.eventsourcing.Snapshotter
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore
import org.axonframework.eventsourcing.eventstore.EventStorageEngine
import org.axonframework.eventsourcing.eventstore.EventStore
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine
import org.axonframework.serialization.Serializer
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.redisson.jcache.JCacheManager
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.net.URI
import java.util.*
import javax.cache.CacheManager
import javax.cache.Caching
import javax.cache.configuration.MutableConfiguration
import javax.cache.expiry.CreatedExpiryPolicy
import javax.cache.expiry.Duration


@Configuration
@EntityScan(basePackages = ["org.axonframework.eventsourcing.eventstore.jpa"])
class AxonConfig {
    @Bean
    fun eventStore(storageEngine: EventStorageEngine): EventStore {
        return EmbeddedEventStore.builder()
            .storageEngine(storageEngine)
            .build()
    }

    @Bean
    fun storageEngine(
        entityManagerProvider: EntityManagerProvider,
        transactionManager: TransactionManager,
        serializer: Serializer
    ): EventStorageEngine {
        return JpaEventStorageEngine
            .builder()
            .entityManagerProvider(entityManagerProvider)
            .transactionManager(transactionManager)
            .eventSerializer(serializer)
            .snapshotSerializer(serializer)
            .build()
    }

    @Bean
    fun snapshotTrigger(snapshotter: Snapshotter): SnapshotTriggerDefinition {
        return EventCountSnapshotTriggerDefinition(snapshotter, 3)
    }

    @Bean(name = ["snapshotCache"])
    @Profile("!redis")
    @ConditionalOnMissingBean
    fun cache(): Cache {
        return WeakReferenceCache()
    }

    @Profile("redis")
    @Bean(name = ["snapshotCache"])
    fun redisCache(@Qualifier("axonCacheManager") cacheManager: CacheManager): Cache {
        val config = MutableConfiguration<String, Any>()
        config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_DAY))
        if (!cacheManager.cacheNames.contains(SNAPSHOT_CACHE_NAME)) {
            cacheManager.createCache(SNAPSHOT_CACHE_NAME, config)
        }
        return JCacheAdapter(cacheManager.getCache(SNAPSHOT_CACHE_NAME))
    }

    @Bean(name = ["axonRedissonClient"])
    fun axonCacheRedissonClient(serializer: Serializer, redisProperties: RedisProperties): RedissonClient {
        return Redisson.create(Config().apply {
            useSingleServer().apply {
                address = buildRedisUri(redisProperties)
                username = redisProperties.username
                password = redisProperties.password
            }
            codec = AxonCacheCodec(serializer)
        })
    }

    @Bean(name = ["axonCacheManager"])
    fun axonCacheManager(
        @Qualifier("axonRedissonClient") redissonClient: RedissonClient,
        redisProperties: RedisProperties
    ): CacheManager {
        return JCacheManager(
            redissonClient as Redisson,
            javaClass.classLoader,
            Caching.getCachingProvider(),
            Properties(),
            URI.create(buildRedisUri(redisProperties))
        )
    }

    private fun buildRedisUri(props: RedisProperties): String {
        val scheme = if (props.ssl.isEnabled) "rediss" else "redis"
        return "$scheme://${props.host}:${props.port}"
    }

    companion object {
        const val SNAPSHOT_CACHE_NAME = "axon-snapshot-cache"
    }
}