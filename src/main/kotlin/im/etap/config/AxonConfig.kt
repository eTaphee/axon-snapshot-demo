package im.etap.config

import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition
import org.axonframework.eventsourcing.SnapshotTriggerDefinition
import org.axonframework.eventsourcing.Snapshotter
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore
import org.axonframework.eventsourcing.eventstore.EventStorageEngine
import org.axonframework.eventsourcing.eventstore.EventStore
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AxonConfig {
    @Bean
    fun eventStore(storageEngine: EventStorageEngine): EventStore {
        return EmbeddedEventStore.builder()
            .storageEngine(storageEngine)
            .build()
    }

    @Bean
    fun storageEngine(): EventStorageEngine {
        return InMemoryEventStorageEngine()
    }

    @Bean
    fun snapShotTrigger(snapshotter: Snapshotter): SnapshotTriggerDefinition {
        return EventCountSnapshotTriggerDefinition(snapshotter, 3)
    }
}