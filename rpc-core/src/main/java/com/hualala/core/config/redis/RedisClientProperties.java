package com.hualala.core.config.redis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by xiangbin on 2016/9/2.
 */
@Data
@Component
@ConfigurationProperties(prefix = "redis.client")
public class RedisClientProperties {
    private String host;
    private int port;
    private int db;
    private int maxActive;
    private int maxIdle;
    private int minIdle;
}
