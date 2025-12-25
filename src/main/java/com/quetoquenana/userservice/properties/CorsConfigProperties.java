package com.quetoquenana.userservice.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "cors")
public class CorsConfigProperties {
    private String hosts;
    private String methods;
    private String headers;
}