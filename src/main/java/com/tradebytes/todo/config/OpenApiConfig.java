package com.tradebytes.todo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for OpenAPI/Swagger documentation.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Todo List Service API")
                        .version("1.0.0")
                        .description("""
                                A resilient backend service for managing a simple to-do list.
                                
                                ## Features
                                - Create, read, update todo items
                                - Mark items as "done" or "not done"
                                - Automatic "past due" status detection
                                - Immutable past due items (cannot be modified)
                                
                                ## Status Values
                                - `not done` - Item is pending
                                - `done` - Item has been completed
                                - `past due` - Item's due date has passed (immutable)
                                """)
                        .contact(new Contact()
                                .name("Todo Service Team"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local development server")
                ));
    }
}
