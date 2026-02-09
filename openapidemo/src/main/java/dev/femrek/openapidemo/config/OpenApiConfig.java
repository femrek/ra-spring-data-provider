package dev.femrek.openapidemo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OpenAPI Demo API")
                        .version("1.0.0")
                        .description("This is a sample Spring Boot application with OpenAPI specification generation")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@example.com")));
    }
}
