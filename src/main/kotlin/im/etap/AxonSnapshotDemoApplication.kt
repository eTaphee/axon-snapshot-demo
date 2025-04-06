package im.etap

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AxonSnapshotDemoApplication

fun main(args: Array<String>) {
    runApplication<AxonSnapshotDemoApplication>(*args)
}
