package com.hualala.core.config.message;

import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Created by xiangbin on 2016/8/27.
 */
public class MessageConfiguration {

    private static final String[] ERROR_MESSAGE_PATH = {"classpath:core","classpath:message"};

    @Bean
    public MessageInfo getMessageInfo() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(ERROR_MESSAGE_PATH);
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        return new MessageInfo(messageSource);
    }
}
