package com.twog.shopping.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "toss.payments")
@Getter
@Setter
public class TossPaymentsConfig {

    private String secretKey;
    private String clientKey;
    private String apiUrl;

}
