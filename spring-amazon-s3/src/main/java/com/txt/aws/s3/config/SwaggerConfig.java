package com.txt.aws.s3.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI
            (
                    @Value("${server.servlet.context-path}") String contextPath,
                    @Value("${application.service-name}") String serviceName,
                    @Value("${application.version}") String version
            ) {

        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("Spring AWS S3 API Rest")
                        .version(version)
                        .description(serviceName.concat(" [").concat("]"))
                        .license(new License().name("AWS S3").url("http://localhost:8080" + contextPath)));

        return openAPI;
    }

    @Bean
    public GroupedOpenApi awsS3API() {
        return GroupedOpenApi.builder()
                .group("AWS S3 API")
                .pathsToMatch(
                        "/**"
                ).build();
    }

}
