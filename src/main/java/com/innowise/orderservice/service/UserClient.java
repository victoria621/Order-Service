package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.UserInfoResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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
    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserFallback")
    public UserInfoResponse getUserById(Long userId) {
        String url = userServiceUrl + "/api/users/" + userId;

        log.info("Calling User Service for userId: {}", userId);
        return restTemplate.getForObject(url, UserInfoResponse.class);
    }

    public UserInfoResponse getUserFallback(Long userId, Exception e) {
        log.warn("User Service is down, returning fallback for userId: {}", userId);
        return new UserInfoResponse(userId, "unknown@email.com", "Unknown", "Unknown", false);
    }

}
