package com.medicare.webui.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * RestClient beans pointing at the backend services. URLs are configurable so the UI
 * can talk to the services directly (dev) or through the gateway (full deployment).
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient appointmentRestClient(
            @Value("${medicare.appointment.url:http://localhost:8082}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public RestClient recordsRestClient(
            @Value("${medicare.records.url:http://localhost:8083}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
