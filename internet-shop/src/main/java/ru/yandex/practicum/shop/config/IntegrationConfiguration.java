package ru.yandex.practicum.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.ApiClient;
import ru.yandex.practicum.shop.api.PaymentsApi;

@Configuration
public class IntegrationConfiguration {

    @Value("${payments-service-uri}")
    private String paymentsServiceUri;

    @Bean
    public PaymentsApi paymentsApi() {
        return new PaymentsApi(apiClient());
    }

    @Bean
    public ApiClient apiClient() {
        var apiClient = new ApiClient();
        apiClient.setBasePath(paymentsServiceUri);
        return apiClient;
    }
}
