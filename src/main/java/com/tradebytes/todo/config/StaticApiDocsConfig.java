package com.tradebytes.todo.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Controller to serve static OpenAPI specification.
 * Serves the api.yml as the single source of truth for API documentation.
 */
@RestController
public class StaticApiDocsConfig {

    @GetMapping(value = "/api-docs.yaml", produces = "application/x-yaml")
    public String getOpenApiSpec() throws IOException {
        ClassPathResource resource = new ClassPathResource("api.yml");
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }
}
