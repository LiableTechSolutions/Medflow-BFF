package com.medflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfiguration {

  @Bean
  OpenAPI medflowOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("MedFlow AI API")
            .description("Backend for the MedFlow AI clinical operating system: doctors, patients, "
                + "appointments, prescriptions, laboratory, pharmacy, notifications, analytics and "
                + "workspace administration.")
            .version("v1")
            .contact(new Contact().name("MedFlow Platform Team")))
        .components(new Components().addSecuritySchemes("bearerAuth",
            new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }
}
