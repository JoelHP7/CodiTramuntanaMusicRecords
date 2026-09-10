package com.coditramuntana.musicrecords.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata exposed through Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI musicRecordsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Discography API")
                        .description("REST API to manage the musical records of a discography firm. "
                                + "Technical test for CodiTramuntana. Reading is public; creating, "
                                + "updating and deleting require a bearer token obtained from "
                                + "/api/auth/login.")
                        .version("1.1.0")
                        .contact(new Contact().name("Joel Hernandez Pla")))
                // Declared as a component but not applied globally: most endpoints are public,
                // so the padlock is attached per operation with @SecurityRequirement instead.
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the accessToken returned by /api/auth/login")));
    }

}
