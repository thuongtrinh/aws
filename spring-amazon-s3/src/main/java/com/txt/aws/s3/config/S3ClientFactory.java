package com.txt.aws.s3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3ClientFactory {

    @Value(value = "${aws-config.s3.accessKey}")
    private String accessKey;

    @Value(value = "${aws-config.s3.secretKey}")
    private String secretKey;


    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Client.builder()
                .region(Region.AP_SOUTHEAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Presigner
                .builder()
                .region(Region.AP_SOUTHEAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
