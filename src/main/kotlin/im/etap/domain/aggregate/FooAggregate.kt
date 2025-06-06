package im.etap.domain.aggregate

import im.etap.domain.message.command.CreateFooCommand
import im.etap.domain.message.command.IncreaseFooValueCommand
import im.etap.domain.message.event.FooCreatedEvent
import im.etap.domain.message.event.FooValueIncreasedEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.spring.stereotype.Aggregate

@Aggregate(snapshotTriggerDefinition = "snapshotTrigger", cache = "snapshotCache")
class FooAggregate {

    @AggregateIdentifier
    private lateinit var id: String
    private var value: Int = 0

    constructor()

    @CommandHandler
    constructor(command: CreateFooCommand) {
        this.id = command.id
        apply(FooCreatedEvent(command.id))
    }

    @EventSourcingHandler
    fun on(event: FooCreatedEvent) {
        this.id = event.id
        println("FooAggregate created with id: $id")
    }

    @CommandHandler
    fun handle(command: IncreaseFooValueCommand): Int {
        val event = FooValueIncreasedEvent(command.id, this.value + 1)
        apply(event)
        return event.value
    }

    @EventSourcingHandler
    fun on(event: FooValueIncreasedEvent) {
        this.value = event.value
        println("FooAggregate value increased to: $value")
    }
}