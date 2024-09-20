package org.example.gitmazonmasternode.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;

@Configuration
public class AwsConfig {
    @Bean
    public Ec2Client ec2Client() {
        return Ec2Client.builder()
            .region(Region.AP_NORTHEAST_1)
            .build();
    }
}
