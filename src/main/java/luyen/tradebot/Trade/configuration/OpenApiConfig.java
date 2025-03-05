package luyen.tradebot.Trade.configuration;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile({"dev","test"})
//@Profile("!prod")
public class OpenApiConfig {

//    @Bean
//    public OpenAPI openAPI() {
//        return new OpenAPI().info(new Info()
//                .title("API-service Documentation").version("1.0.0")
//                .license(new License().name("API License 2.0").url("http://domain.vn/license")))
//                .servers(List.of(new Server().url("https://localhost:8080").description("API Server Documentation")));
//    }
    @Bean
    @Profile("!prod")
    public OpenAPI openAPI( @Value("${open.api.title}") String title ,
                            @Value("${open.api.version}") String version,
                            @Value("${open.api.description}") String description,
                            @Value("${open.api.server}") String serverUrl,
                            @Value("${open.api.serverName}") String serverName)
                            {
        return new OpenAPI().info(new Info()
                        .title(title)
                        .version(version)
                        .description(description)
                        .license(new License().name("API License 2.0").url("http://domain.vn/license")))
                .servers(List.of(new Server().url(serverUrl).description(serverName)))
//                .components(
//                        new Components().addSecuritySchemes(
//                                "bearerAuth",
//                                new SecurityScheme()
//                                        .type(SecurityScheme.Type.HTTP)
//                                        .scheme("bearer")
//                                        .bearerFormat("JWT")))
//                .security(List.of(new SecurityRequirement().addList("bearerAuth")))
                ;
    }
    @Bean
    public GroupedOpenApi groupOpenApi(){
        return GroupedOpenApi.builder()
                .group("api-service")
                .packagesToScan("luyen.tradebot.Trade.controller")
                .build();

    }
}
