package com.hualala.core.config.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by xiangbin on 2016/9/2.
 */
public class RedisClientConfiguration {

    private Logger logger = LoggerFactory.getLogger(RedisClientConfiguration.class);

    @Autowired
    private RedisClientProperties redisClientProperties;

    @Bean
    public RedisTemplate getRedisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        logger.info("Initializing redis client... properties [" + redisClientProperties + "]");
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean(destroyMethod = "destroy")
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMinIdle(redisClientProperties.getMinIdle());
        jedisPoolConfig.setMaxIdle(redisClientProperties.getMaxIdle());
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setHostName(redisClientProperties.getHost());
        jedisConnectionFactory.setPort(redisClientProperties.getPort());
        jedisConnectionFactory.setDatabase(redisClientProperties.getDb());
        jedisConnectionFactory.setPoolConfig(jedisPoolConfig);
        jedisConnectionFactory.setUsePool(true);
        return jedisConnectionFactory;
    }
}
