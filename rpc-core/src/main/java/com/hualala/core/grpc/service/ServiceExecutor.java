package com.hualala.core.grpc.service;

import com.hualala.core.Constants;
import com.hualala.core.ErrorInfo;
import com.hualala.core.base.RequestInfo;
import com.hualala.core.base.ResultInfo;
import com.hualala.core.base.ServiceException;
import com.hualala.core.config.message.MessageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by xiangbin on 2016/9/8.
 */
public class ServiceExecutor {

    private Logger logger = LoggerFactory.getLogger(ServiceExecutor.class);
    private Logger exceptionLogger = LoggerFactory.getLogger(Constants.EXCEPTION_LOGGER);
    @Autowired
    private MessageInfo messageInfo;

    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();;

    private String logSepartor = "$$";

    public ResultInfo invoke(Object serviceImpl, Method execMethod, RequestInfo requestInfo) {
        StringBuilder logSb = new StringBuilder().append(serviceImpl.getClass().getCanonicalName() + "@" + execMethod.getName()).append(logSepartor);
        logSb.append(requestInfo.getTraceID()).append(logSepartor);
        StringBuilder requestBodySb = new StringBuilder();
        requestBodySb.append(requestInfo.toString());
        logger.info(serviceImpl.getClass().getCanonicalName() + "@" + execMethod.getName() + logSepartor);
        long startTime = System.currentTimeMillis();

        Set<ConstraintViolation<RequestInfo>> constraintViolations = validator.validate(requestInfo);
        if (constraintViolations.size() > 0) {
            String validateErrorMessage = constraintViolations.stream().map(constraintViolation->
                    constraintViolation.getMessage()
            ).collect(Collectors.joining(";"));
            logger.info("service validate error [" + validateErrorMessage + "]");
            return messageInfo.getResultMessage(ErrorInfo.SERVICE_PROXY_VALIDATE_ERROR, new Object[] {validateErrorMessage});
        }
        ResultInfo resultInfo = new ResultInfo();
        try {
            resultInfo = (ResultInfo) ReflectionUtils.invokeMethod(execMethod, serviceImpl, requestInfo);
            if (resultInfo.getMessage() == null && resultInfo.getCode() != null) {
                resultInfo.setMessage(messageInfo.getMessageValue(resultInfo.getCode(), resultInfo.getMessageParams()));
            }
            logSb.append((System.currentTimeMillis() - startTime)).append(logSepartor).append(requestBodySb).append(logSepartor).append(resultInfo.toString());
            logger.info(logSb.toString());
            return resultInfo;
        } catch (ServiceException e) {
            String errorMessage = e.getErrorMessage();
            if (errorMessage == null || "".equals(errorMessage)) {
                errorMessage = messageInfo.getMessageValue(e.getErrorCode(), e.getParams());
            }
            resultInfo = new ResultInfo(e.getErrorCode(), errorMessage);
            return resultInfo;
        } catch (Throwable e) {
            e.printStackTrace();
            exceptionLogger.error("invoke method error", e);
            resultInfo = messageInfo.getResultMessage(ErrorInfo.SERVICE_EXECUTE_ERROR, new Object[]{e.getMessage()});
            return resultInfo;
        } finally {
            logSb.append((System.currentTimeMillis()-startTime)).append(logSepartor).append(requestBodySb).append(logSepartor).append(resultInfo.toString());
        }
    }
}
