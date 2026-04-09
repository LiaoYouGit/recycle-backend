package com.recycle.auth.config;

import com.recycle.common.JwtUtil;
import com.recycle.common.JwtProperties;
import com.recycle.common.AliyunSmsUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class AuthBeanConfig {

    @Bean
    public JwtUtil jwtUtil(JwtProperties properties) {
        return new JwtUtil(properties.getSecret(), properties.getExpiration());
    }

    @Bean
    public AliyunSmsUtil aliyunSmsUtil(
            @Value("${aliyun.sms.access-key-id}") String akId,
            @Value("${aliyun.sms.access-key-secret}") String akSecret,
            @Value("${aliyun.sms.sign-name}") String signName,
            @Value("${aliyun.sms.template-code}") String templateCode,
            RedisTemplate<String, Object> redisTemplate) {
        return new AliyunSmsUtil(akId, akSecret, signName, templateCode, redisTemplate);
    }
}
