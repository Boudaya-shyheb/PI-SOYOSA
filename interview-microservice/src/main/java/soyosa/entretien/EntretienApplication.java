package soyosa.entretien;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EntretienApplication {

    public static void main(String[] args) {
        SpringApplication.run(EntretienApplication.class, args);
    }

}
