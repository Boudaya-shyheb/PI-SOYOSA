package com.englishway.gateway;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for EnglishWay API Gateway.
 * 
 * This gateway serves as the single entry point for all microservices
 * in the EnglishWay platform, providing routing, load balancing,
 * and CORS configuration.
 * 
 * @author EnglishWay Team
 * @version 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
