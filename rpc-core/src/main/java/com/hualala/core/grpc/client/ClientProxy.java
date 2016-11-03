package com.hualala.core.grpc.client;

import com.google.protobuf.GeneratedMessage;
import com.hualala.core.Constants;
import com.hualala.core.ErrorInfo;
import com.hualala.core.base.RequestInfo;
import com.hualala.core.base.ResultInfo;
import com.hualala.core.config.message.MessageInfo;
import com.hualala.core.grpc.GrpcData;
import com.hualala.core.grpc.GrpcUtils;
import com.hualala.core.grpc.TraceThreadLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 端执行RPC服务的代理
 * Created by xiangbin on 2016/10/18.
 */
public class ClientProxy implements InvocationHandler {

    private Logger logger = LoggerFactory.getLogger(ClientProxy.class);
    private Logger exceptionLogger = LoggerFactory.getLogger(Constants.EXCEPTION_LOGGER);

    private MessageInfo messageInfo;
    private GrpcClientExecutor grpcClientExecutor;
    private Class<?> rpcInterface;
    private Class<?> grpcServiceImpl;
    private Class<?> grpcServiceOuter;

    private Validator validator;

    private Map<String, Optional> grpcDataCache = new ConcurrentHashMap<>();

    public ClientProxy(Class<?> rpcInterface, Class<?> grpcServiceOuter, Class<?> grpcServiceImpl, GrpcClientExecutor grpcClientExecutor, MessageInfo messageInfo) {
        this.rpcInterface = rpcInterface;
        this.grpcServiceOuter = grpcServiceOuter;
        this.grpcServiceImpl = grpcServiceImpl;
        this.grpcClientExecutor = grpcClientExecutor;
        this.messageInfo = messageInfo;
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args.length == 1 && args[0] instanceof RequestInfo && ResultInfo.class.isAssignableFrom(method.getReturnType())) {
            long startTime = System.currentTimeMillis();
            RequestInfo requestInfo = (RequestInfo)args[0];
            if (requestInfo.getTraceID() == null) {
                if (TraceThreadLocal.getValue() != null) {
                    requestInfo.setTraceID(TraceThreadLocal.getValue());
                } else {
                    requestInfo.setTraceID(UUID.randomUUID().toString());
                }
            }
            String traceID = requestInfo.getTraceID();
            logger.info("traceID [" + traceID + "] start client proxy ...");
            Set<ConstraintViolation<RequestInfo>> constraintViolations = validator.validate(requestInfo);
            if (constraintViolations.size() > 0) {
                String validateErrorMessage = constraintViolations.stream().map(constraintViolation->
                    constraintViolation.getMessage()
                ).collect(Collectors.joining(";"));
                logger.info("traceID [" + requestInfo.getTraceID() + "] client validate error [" + validateErrorMessage + "]");
                return GrpcUtils.resultInfoToBean(messageInfo.getResultMessage(ErrorInfo.CLIENT_PROXY_VALIDATE_ERROR, new Object[] {validateErrorMessage}), method.getReturnType());
            }
            String methodName = method.getName();
            logger.info("traceID [" + requestInfo.getTraceID() + "] client client [" + rpcInterface.getSimpleName() + "/" + methodName + "] parameterType [" + args[0].getClass() + "] returnType [" + method.getReturnType() + "]");
            String cacheKey = rpcInterface.getName() + "." + methodName;
            Optional<GrpcData> optional = grpcDataCache.computeIfAbsent(cacheKey, (key) ->
                GrpcUtils.parseGrpcServiceInfo(rpcInterface, methodName)
            );
            if (!optional.isPresent()) {
                logger.error("not found " + rpcInterface.getSimpleName() + "/" + methodName + " grpc service in [" + grpcServiceImpl.getName() + "]");
                return GrpcUtils.resultInfoToBean(messageInfo.getResultMessage(ErrorInfo.CLIENT_PROXY_GRPC_SERVICE_NOT_FOUND),method.getReturnType());
            }
            GrpcData grpcData = optional.get();
            if (logger.isInfoEnabled()) {
                logger.info("grpc class info [" + grpcData.getGrpcServiceImpl() + "/" + grpcData.getMethodName()  + "]");
            }
            ResultInfo resultInfo = new ResultInfo(messageInfo.getMessageValue(ErrorInfo.CLIENT_PROXY_RPC_DEFAULT_ERROR));
            try {
                GeneratedMessage requestMessage = GrpcUtils.requestToGrpcMessage(requestInfo, grpcData.getGrpcParameterType());
                GeneratedMessage resultMessage = grpcClientExecutor.rpcService(traceID, grpcData, requestMessage, grpcData.getGrpcReturnType());
                resultInfo = GrpcUtils.grpcMessageToResultBean(resultMessage, grpcData.getRpcReturnType());
                return resultInfo;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                exceptionLogger.error("client traceID [" + requestInfo.getTraceID() + "] client proxy execute error", throwable);
                return GrpcUtils.resultInfoToBean(messageInfo.getResultMessage(ErrorInfo.CLIENT_PROXY_EXEC_ERROR), grpcData.getRpcReturnType());
            } finally {
                String logStr = "client traceID [" + requestInfo.getTraceID() + "] client cost [" + (System.currentTimeMillis() - startTime) + "]ms, returnCode [" + resultInfo.getCode() + "] returnMessage [" + resultInfo.getMessage() + "]";
                if (resultInfo.success()) {
                    logger.info(logStr);
                } else {
                    logger.error(logStr);
                }
            }
        }
        return method.invoke(proxy, args);
    }
}
