package com.hualala.core.service.config.mybatis;

import com.hualala.core.service.config.datasource.DataSourceConfiguration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * Created by xiangbin on 2016/8/26.
 */
@Configuration
@EnableConfigurationProperties(MybatisProperties.class)
@EnableTransactionManagement
@AutoConfigureAfter({DataSourceConfiguration.class, MybatisAutoConfiguration.class})
public class MybatisConfiguration implements TransactionManagementConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(MybatisConfiguration.class);

    @Autowired
    private MybatisProperties properties;

    @Autowired
    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Autowired
    private DataSource dataSource;

    @Bean(name = "sessionFactory")
    public SqlSessionFactory sqlSessionFactoryBean() throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        if (logger.isInfoEnabled()) {
            logger.info("Mybatis SqlSessionFactoryBean set datasource [" + dataSource + "]");
        }
        factory.setDataSource(dataSource);
        if (logger.isInfoEnabled()) {
            logger.info("mybatis properties [" + this.dataSource +"]");
        }
        if (StringUtils.hasText(properties.getConfigLocation())) {
            factory.setConfigLocation(resourceLoader.getResource(properties.getConfigLocation()));
        } else {
            factory.setTypeAliasesPackage(properties.getTypeAliasesPackage());
            factory.setTypeHandlersPackage(properties.getTypeHandlersPackage());
            factory.setMapperLocations(properties.resolveMapperLocations());

        }
        return factory.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean
    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }
}
