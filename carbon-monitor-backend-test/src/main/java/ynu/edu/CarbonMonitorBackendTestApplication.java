package ynu.edu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CarbonMonitorBackendTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarbonMonitorBackendTestApplication.class, args);
    }

}
