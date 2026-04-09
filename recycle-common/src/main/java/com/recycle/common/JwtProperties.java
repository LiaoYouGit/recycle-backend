package com.recycle.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret = "recycle_admin_secret_key_2024";
    private long expiration = 604800000;
    private String header = "Authorization";
    private String prefix = "Bearer ";
}
