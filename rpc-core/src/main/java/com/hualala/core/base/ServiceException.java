package com.hualala.core.base;

/**
 * Created by xiangbin on 2016/10/12.
 */
public class ServiceException extends RuntimeException {

    private final String errorCode;
    private String errorMessage;
    private Object[] params;

    public ServiceException(String errorCode) {
        this(errorCode, null);
    }

    public ServiceException(String errorCode, String errorMessage) {
        this(errorCode, errorMessage, null);
    }

    public ServiceException(String errorCode, String errorMessage, Object[] params) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.params = params;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public Object[] getParams() {
        return params;
    }

}
