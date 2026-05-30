package com.controlmezcla.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class R2Config {

    @Value("${R2_ENDPOINT}")
    private String R2_ENDPOINT;

    @Value("${R2_ACCESS_KEY}")
    private String R2_ACCESS_KEY;

    @Value("${R2_SECRET_KEY}")
    private String R2_SECRET_KEY;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(R2_ENDPOINT))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(R2_ACCESS_KEY, R2_SECRET_KEY)
                ))
                .region(Region.of("auto"))
                .forcePathStyle(true)
                .build();
    }

}
