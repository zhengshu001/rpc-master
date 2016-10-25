package com.hualala.core.config.datasource;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 数据库连接池设置
 * Created by xiangbin on 2016/8/26.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceProperties {

    private String driverClassName;

    private String url;

    private String username;

    private String password;

    private int initialSize;

    private int maxActive;

    private int minIdle;

    private String validationQuery;
}
