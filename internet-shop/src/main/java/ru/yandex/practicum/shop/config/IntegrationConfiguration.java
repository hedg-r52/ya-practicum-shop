package ru.yandex.practicum.shop.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.ApiClient;
import ru.yandex.practicum.shop.api.PaymentsApi;

@Configuration
@Slf4j
public class IntegrationConfiguration {

    @Value("${payments-service-uri}")
    private String paymentsServiceUri;

    @Bean
    public PaymentsApi paymentsApi(ApiClient apiClient) {
        return new PaymentsApi(apiClient);
    }

    @Bean
    public WebClient webClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        var oauth2 = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2.setDefaultClientRegistrationId("shop");

        return WebClient.builder()
                .filter(oauth2)
                .build();
    }

    @Bean
    public ApiClient apiClient(WebClient webClient) {
        var apiClient = new ApiClient(webClient);
        apiClient.setBasePath(paymentsServiceUri);
        log.info("Payments URI: {}", apiClient.getBasePath());
        return apiClient;
    }

}
