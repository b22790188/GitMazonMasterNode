package org.example.gitmazonmasternode.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(ResourceUrlProvider mvcResourceUrlProvider) {
        return new RestTemplate();
    }
}
