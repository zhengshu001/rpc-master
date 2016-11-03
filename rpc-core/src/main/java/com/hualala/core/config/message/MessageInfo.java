package com.hualala.core.config.message;

import com.hualala.core.base.ResultInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Created by xiangbin on 2016/8/27.
 */
public class MessageInfo {

    private ReloadableResourceBundleMessageSource messageSource;

    private static final String[] ERROR_MESSAGE_PATH = {"classpath:core","classpath:message"};

    public MessageInfo() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(ERROR_MESSAGE_PATH);
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        this.messageSource = messageSource;
    }

    public MessageInfo(ReloadableResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public ResultInfo getResultMessage(String errorCode) {
        return getResultMessage(errorCode, null);
    }

    public ResultInfo getResultMessage(String errorCode, Object[] objects) {
        String errorMessage = messageSource.getMessage(errorCode, objects, null);
        if (!StringUtils.isNotBlank(errorMessage)) {
            errorMessage = errorCode;
        }
        return new ResultInfo(errorCode, errorMessage);
    }

    public String getMessageValue(String errorCode, Object[] objects) {
        String errorMessage = messageSource.getMessage(errorCode, objects, null);
        return errorMessage;
    }

    public String getMessageValue(String errorCode) {
        return getMessageValue(errorCode, null);
    }

}
