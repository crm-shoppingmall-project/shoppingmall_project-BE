package com.twog.shopping.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {

        // JWT Security Scheme 설정
        SecurityScheme securityScheme = new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("JWT");



        String description = """
                ### 회원가입 더미 데이터 예시

                ```json
                {
                  "memberEmail": "testuser01@example.com",
                  "memberName": "홍길동",
                  "memberPwd": "password123!",
                  "confirmPassword": "password123!",
                  "memberPhone": "01012345678",
                  "memberBirth": "1997-05-18",
                  "memberGender": "M",
                  "role": "USER",
                  "profileAddress": "서울특별시 강남구 테헤란로 123",
                  "profileDetailAddress": "101동 1203호",
                  "profilePreferred": "패션, 전자기기",
                  "profileInterests": "런닝, 테크, 음악"
                }
                ```
                """;

        return new OpenAPI()
                .info(new Info()
                        .title("Twog Shopping API")
                        .description(description)
                        .version("v1.0"))
                .components(new Components()
                        .addSecuritySchemes("JWT", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}
