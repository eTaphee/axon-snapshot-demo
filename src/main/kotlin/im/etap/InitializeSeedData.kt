package im.etap

import im.etap.domain.message.command.CreateFooCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class InitializeSeedData(
    private val commandGateway: CommandGateway
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        commandGateway.send<Unit>(CreateFooCommand(id = "0"))
    }
}