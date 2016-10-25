package com.hualala.core.api.config;

import com.hualala.core.client.BaseRpcClient;
import com.hualala.core.config.message.MessageInfo;
import com.hualala.core.grpc.client.ClientScannerConfigurator;
import com.hualala.core.grpc.client.GrpcClientExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Created by xiangbin on 2016/10/21.
 */
@org.springframework.context.annotation.Configuration
public class ApiRpcConfiguration {

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
    public ClientScannerConfigurator configClientScanner() {
        return new ClientScannerConfigurator();
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
