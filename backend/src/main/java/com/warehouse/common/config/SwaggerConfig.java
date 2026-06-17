package com.warehouse.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI warehouseOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("창고지기 API")
                .description("3PL 물류센터 WMS 백엔드 API")
                .version("v1"));
    }
}
