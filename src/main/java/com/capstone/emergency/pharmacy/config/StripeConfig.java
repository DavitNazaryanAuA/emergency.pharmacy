package com.capstone.emergency.pharmacy.config;

import com.stripe.net.RequestOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${stripe.api.key}")
    private String apiKey;

    @Bean
    RequestOptions stripeRequestOptions() {
        return RequestOptions.builder()
                .setApiKey(apiKey).build();
    }
}
