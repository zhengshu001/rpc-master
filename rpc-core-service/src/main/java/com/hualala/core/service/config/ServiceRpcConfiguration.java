package com.hualala.core.service.config;

import com.hualala.core.client.BaseRpcClient;
import com.hualala.core.config.message.MessageInfo;
import com.hualala.core.grpc.client.ClientScannerConfigurator;
import com.hualala.core.grpc.client.GrpcClientExecutor;
import com.hualala.core.grpc.service.ServiceExecutor;
import com.hualala.core.grpc.service.ServiceScannerConfigurator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Created by xiangbin on 2016/10/21.
 */
@Configuration
public class ServiceRpcConfiguration {

    private static final String[] ERROR_MESSAGE_PATH = {"classpath:core","classpath:message"};
    /**
     * Grpc 客户端执行执行
     * @return
     */
    @Bean
    public GrpcClientExecutor configClientExecutor() {
        return new GrpcClientExecutor();
    }

    @Bean
    public ServiceExecutor configServiceExecutor() {
        return new ServiceExecutor();
    }

    @Bean
    public ClientScannerConfigurator configClientScanner() {
        return new ClientScannerConfigurator();
    }

    @Bean
    public ServiceScannerConfigurator configServiceScanner() {
        return new ServiceScannerConfigurator();
    }

    @Bean
    public BaseRpcClient configRpcClient() {
        return new BaseRpcClient();
    }

    @Bean
    public MessageInfo getMessageInfo() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(ERROR_MESSAGE_PATH);
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        return new MessageInfo(messageSource);
    }
}
