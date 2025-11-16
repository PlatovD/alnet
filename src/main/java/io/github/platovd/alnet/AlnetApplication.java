package io.github.platovd.alnet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class AlnetApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlnetApplication.class, args);
    }

}
