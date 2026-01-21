package com.messenger.aichat.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HuggingFaceConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
