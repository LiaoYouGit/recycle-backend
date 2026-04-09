package com.recycle.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.recycle.common.JwtUtil;
import com.recycle.common.JwtProperties;
import com.recycle.common.Result;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthGatewayFilterFactory.Config> {

    private final JwtUtil jwtUtil;

    public JwtAuthGatewayFilterFactory(JwtProperties jwtProperties) {
        super(Config.class);
        this.jwtUtil = new JwtUtil(jwtProperties.getSecret(), 604800000);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();

            // 放行登录接口
            if (path.contains("/auth/login")) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorized(exchange.getResponse(), "未登录或token缺失");
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return unauthorized(exchange.getResponse(), "token无效或已过期");
            }

            // 将用户信息透传给下游服务
            Long userId = jwtUtil.getUserId(token);
            String username = jwtUtil.getUsername(token);

            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-Username", username)
                    .build();

            return chain.filter(exchange.mutate().request(request).build());
        };
    }

    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        Result<?> result = Result.error(401, message);
        DataBuffer buffer = response.bufferFactory().wrap(JSON.toJSONString(result).getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // 可扩展配置
    }
}
