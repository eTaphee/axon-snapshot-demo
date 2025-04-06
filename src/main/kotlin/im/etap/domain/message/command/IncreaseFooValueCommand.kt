package im.etap.domain.message.command

import org.axonframework.modelling.command.TargetAggregateIdentifier

data class IncreaseFooValueCommand(
    @TargetAggregateIdentifier
    val id: String,
)
