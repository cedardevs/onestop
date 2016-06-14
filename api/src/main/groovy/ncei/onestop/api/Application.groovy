package ncei.onestop.api

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class Application {

    // TODO config setup here or in separate config location

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args)
    }

}

