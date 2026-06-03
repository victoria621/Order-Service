package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.UserInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@Slf4j
public class UserClient {
    private final RestTemplate restTemplate;
    @Value("${user.service.url:http://localhost:8080}")
    private String userServiceUrl;

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable(value = "users", key = "#userId")
    public UserInfoResponse getUserById(Long userId) {
        String url = userServiceUrl + "/api/users/" + userId;

        try {
            log.info("Calling User Service for userId: {}", userId);
            return restTemplate.getForObject(url, UserInfoResponse.class);
        } catch (Exception e) {
            log.error("Failed to get user by id {}: {}", userId, e.getMessage());
            return null;
        }
    }

}
