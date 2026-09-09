package com.coditramuntana.musicrecords.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata exposed through Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI musicRecordsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Discography API")
                        .description("REST API to manage the musical records of a discography firm. "
                                + "Technical test for CodiTramuntana.")
                        .version("1.0.0")
                        .contact(new Contact().name("Joel Hernandez Pla")));
    }

}
