package com.esprit.microservice.trainingservice.client;

import com.esprit.microservice.trainingservice.dto.UserInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserClient {

    @GetMapping("/internal/user/{userId}")
    UserInfoDto getUserInfo(@PathVariable("userId") String userId);
}
