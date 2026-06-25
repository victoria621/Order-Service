package com.innowise.orderservice.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.innowise.orderservice.dto.UserInfoResponse;
import com.innowise.orderservice.service.UserClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class UserClientIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("orderdb")
            .withUsername("postgres")
            .withPassword("565452");

    @Autowired
    private UserClient userClient;

    @Autowired
    private WireMockServer wireMockServer;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.repositories.enabled", () -> false);
        registry.add("spring.cache.type", () -> "none");
    }

    @BeforeEach
    void setUp() {
        String url = "http://localhost:" + wireMockServer.port();
        userClient.setUserServiceUrl(url);
    }

    @Test
    void getUserById_ShouldReturnUserInfo() {
        stubFor(get(urlEqualTo("/api/users/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": 1,
                                    "email": "test@email.com",
                                    "name": "Test",
                                    "surname": "User",
                                    "active": true
                                }
                                """)));

        UserInfoResponse result = userClient.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("test@email.com");
    }

    @Test
    void getUserById_WhenUserServiceFails_ShouldReturnFallback() {
        stubFor(get(urlEqualTo("/api/users/999"))
                .willReturn(aResponse()
                        .withStatus(500)));

        UserInfoResponse result = userClient.getUserById(999L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(999L);
        assertThat(result.email()).isEqualTo("unknown@email.com");
        assertThat(result.name()).isEqualTo("Unknown");
        assertThat(result.surname()).isEqualTo("Unknown");
        assertThat(result.active()).isFalse();
    }

    @Test
    void getUserById_WhenCircuitBreakerOpen_ShouldReturnFallback() {
        for (int i = 0; i < 5; i++) {
            stubFor(get(urlEqualTo("/api/users/888"))
                    .willReturn(aResponse().withStatus(500)));
            userClient.getUserById(888L);
        }

        UserInfoResponse result = userClient.getUserById(888L);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("unknown@email.com");
    }
}