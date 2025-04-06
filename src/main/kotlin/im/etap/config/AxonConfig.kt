package im.etap.config

import org.axonframework.common.jpa.EntityManagerProvider
import org.axonframework.common.transaction.TransactionManager
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition
import org.axonframework.eventsourcing.SnapshotTriggerDefinition
import org.axonframework.eventsourcing.Snapshotter
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore
import org.axonframework.eventsourcing.eventstore.EventStorageEngine
import org.axonframework.eventsourcing.eventstore.EventStore
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


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
    ): EventStorageEngine {
        return JpaEventStorageEngine
            .builder()
            .entityManagerProvider(entityManagerProvider)
            .transactionManager(transactionManager)
            .build()
    }

    @Bean
    fun snapShotTrigger(snapshotter: Snapshotter): SnapshotTriggerDefinition {
        return EventCountSnapshotTriggerDefinition(snapshotter, 3)
    }
}