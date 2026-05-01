package pt.achman

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["pt.achman"])
class WebApp {

}

fun main() {
    runApplication<WebApp>()
}
