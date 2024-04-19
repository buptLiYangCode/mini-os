package bupt.os;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MiniOsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniOsApplication.class, args);
    }

}
