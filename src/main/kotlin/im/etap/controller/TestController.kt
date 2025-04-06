package im.etap.controller

import im.etap.domain.message.command.CreateFooCommand
import im.etap.domain.message.command.IncreaseFooValueCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/foo")
class TestController(
    private val commandGateway: CommandGateway,
) {
    @PostMapping
    fun create(): String {
        val command = CreateFooCommand(id = UUID.randomUUID().toString())
        commandGateway.sendAndWait<Unit>(command)
        return command.id
    }

    @PostMapping("{id}/increase")
    fun increase(@PathVariable id: String): Int {
        println("call increase foo value")
        val command = IncreaseFooValueCommand(id = id)
        val value = commandGateway.sendAndWait<Int>(command)
        return value
    }
}