package org.gridsuite.mapping.server;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MappingSwaggerConfig {
    @Bean
    public OpenAPI createOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Study API")
                        .description("This is the documentation of the Mapping REST API")
                        .version(MappingApi.API_VERSION));
    }
}
