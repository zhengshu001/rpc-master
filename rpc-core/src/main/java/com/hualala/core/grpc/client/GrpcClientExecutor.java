package com.hualala.core.grpc.client;

import com.google.protobuf.GeneratedMessage;
import com.hualala.core.Constants;
import com.hualala.core.ErrorInfo;
import com.hualala.core.config.message.MessageInfo;
import com.hualala.core.grpc.GrpcData;
import com.hualala.core.grpc.GrpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by xiangbin on 2016/10/18.
 */
public class GrpcClientExecutor implements ApplicationContextAware{

    private Logger logger = LoggerFactory.getLogger(GrpcClientExecutor.class);
    private Logger exceptionLogger = LoggerFactory.getLogger(Constants.EXCEPTION_LOGGER);

    protected ApplicationContext appContext;

    @Autowired
    protected MessageInfo messageInfo;

    public GeneratedMessage rpcService(String traceID, GrpcData grpcData, GeneratedMessage message, Class<?> resClass) {
        try {
            GrpcClient grpcClient = appContext.getBean(grpcData.getClentName(), GrpcClient.class);
            if (grpcClient == null) {
                logger.info("not found client [" + grpcData.getClentName() + "] grpcClient");
                return GrpcUtils.resultToGrpcMessage(messageInfo.getResultMessage(ErrorInfo.RPC_CLIENT_SERVICE_NOT_FOUND).setTraceID(traceID), resClass);
            }
            Object futureStub = grpcClient.getFutureStub(grpcData.getGrpcServiceOuter());
            Optional execMethodOptional = Arrays.asList(ReflectionUtils.getAllDeclaredMethods(futureStub.getClass())).stream().filter((method) ->
                    (method.getName().equals(grpcData.getMethodName()) && method.getParameterCount() == 1)).findFirst();
            if (!execMethodOptional.isPresent()) {
                logger.info("not found client [" + grpcClient + "] methodName [" + grpcData.getMethodName() + "] future");
                return GrpcUtils.resultToGrpcMessage(messageInfo.getResultMessage(ErrorInfo.RPC_CLIENT_SERVICE_METHOD_NOT_FOUND).setTraceID(traceID), resClass);
            }
            Method execMethod = (Method)execMethodOptional.get();
            Object resFutureStub = execMethod.invoke(futureStub, new Object[] {message});
            Optional execGetOptional = Arrays.asList(ReflectionUtils.getAllDeclaredMethods(resFutureStub.getClass())).stream().filter((method) ->
                    (method.getName().equals("get") && method.getParameterCount() == 0)
            ).findFirst();

            if (!execGetOptional.isPresent()) {
                logger.info("client [" + grpcClient + "] methodName [" + grpcData.getMethodName() + "] client service error");
                return GrpcUtils.resultToGrpcMessage(messageInfo.getResultMessage(ErrorInfo.RPC_CLIENT_SERVICE_PARAMETER_ERROR).setTraceID(traceID), resClass);
            }
            Method execGetMethod = (Method)execGetOptional.get();
            GeneratedMessage resMessage = (GeneratedMessage)execGetMethod.invoke(resFutureStub, new Object[] {});
            return resMessage;
        } catch (Exception e) {
            e.printStackTrace();
            exceptionLogger.error("client remote service error", e);
            return GrpcUtils.resultToGrpcMessage(messageInfo.getResultMessage(ErrorInfo.RPC_CLIENT_SERVICE_ERROR).setTraceID(traceID), resClass);
        }
    }

    public void setMessageInfo(MessageInfo messageInfo) {
        this.messageInfo = messageInfo;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }
}
