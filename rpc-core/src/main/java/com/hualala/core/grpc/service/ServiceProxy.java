package com.hualala.core.grpc.service;

import com.google.protobuf.GeneratedMessage;
import com.hualala.core.ErrorInfo;
import com.hualala.core.base.RequestInfo;
import com.hualala.core.base.ResultInfo;
import com.hualala.core.config.message.MessageInfo;
import com.hualala.core.grpc.GrpcData;
import com.hualala.core.grpc.GrpcUtils;
import com.hualala.core.grpc.TraceThreadLocal;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xiangbin on 2016/8/15.
 */
public class ServiceProxy implements MethodInterceptor,Serializable {

    private static final long serialVersionUID = -6424540398559729838L;
    private Logger logger = LoggerFactory.getLogger(ServiceProxy.class);
    private final ServiceExecutor executor;
    private final MessageInfo messageInfo;
    private final Class<?> grpcServiceOuter;
    private final Class<?> grpcServiceImpl;
    private final Class<?> rpcInterface;
    private final Object rpcService;
    private Map<String, Optional> grpcDataCache = new ConcurrentHashMap<>();

    public ServiceProxy(Class<?> rpcInterface, Object rpcService, Class<?> grpcServiceOuter, Class<?> grpcServiceImpl, ServiceExecutor executor, MessageInfo messageInfo) {
        this.rpcInterface = rpcInterface;
        this.rpcService = rpcService;
        this.executor = executor;
        this.grpcServiceOuter = grpcServiceOuter;
        this.grpcServiceImpl = grpcServiceImpl;
        this.messageInfo = messageInfo;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        if (objects.length == 2 && GeneratedMessage.class.isAssignableFrom(objects[0].getClass()) && StreamObserver.class.isAssignableFrom(objects[1].getClass())) {
            GeneratedMessage requestGrpcMessage = (GeneratedMessage) objects[0];
            StreamObserver streamObserver = (StreamObserver) objects[1];
            Optional<String> traceIDOptional = GrpcUtils.getRequestHeaderFieldValue(requestGrpcMessage, GrpcUtils.GRPC_TRACEID);
            String traceID = traceIDOptional.orElseGet(() -> UUID.randomUUID().toString());
            TraceThreadLocal.setValue(traceID);
            long startTime = System.currentTimeMillis();
            String grpcMethodName = method.getName();
            if (logger.isInfoEnabled()) {
                logger.info("service traceID [" + traceID + "] service grpc [" + o.getClass().getSimpleName() + "/" + grpcMethodName + "] parameterType [" + objects[0].getClass() + "," + objects[1].getClass() + "]");
            }
            String cacheKey = grpcServiceImpl.getName() + "." + grpcMethodName;
            Optional<GrpcData> grpcDataOptional = grpcDataCache.computeIfAbsent(cacheKey,
                    (key) -> GrpcUtils.parseGrpcServiceInfo(rpcInterface, grpcMethodName));
            if (!grpcDataOptional.isPresent()) {
                logger.error("not found grpc service " + o.getClass().getSimpleName() + "/" + grpcMethodName + " client interface [" + rpcInterface.getSimpleName() + "]");
                streamObserver.onNext(methodProxy.invokeSuper(o, objects));
                streamObserver.onCompleted();
                return null;
            }
            GrpcData grpcData = grpcDataOptional.get();
            if (logger.isInfoEnabled()) {
                logger.info("service info [" + rpcService.getClass().getName() + "/" + grpcMethodName + "] ");
            }
            ResultInfo resultInfo = new ResultInfo(messageInfo.getMessageValue(ErrorInfo.SERVICE_PROXY_RPC_DEFAULT_ERROR));
            try {
                RequestInfo paramValue = GrpcUtils.grpcMessageToRequest(requestGrpcMessage,  grpcData.getRpcParameterType());
                resultInfo = executor.invoke(rpcService, grpcData.getRpcExecMethod(), paramValue).setTraceID(traceID);
                Object rpcResData = GrpcUtils.resultToGrpcMessage(resultInfo, grpcData.getGrpcReturnType());
                streamObserver.onNext(rpcResData);
                streamObserver.onCompleted();
                return null;
            } catch (Throwable e) {
                e.printStackTrace();
                logger.error("service exec error", e);
                resultInfo = messageInfo.getResultMessage(ErrorInfo.RPC_SERVICE_ERROR_CODE).setTraceID(traceID);
                Object rpcResData = GrpcUtils.resultToGrpcMessage(resultInfo, grpcData.getGrpcReturnType());
                streamObserver.onNext(rpcResData);
                streamObserver.onCompleted();
                return null;
            } finally {
                String logStr = "service traceID [" + traceID + "] service cost [" + (System.currentTimeMillis() - startTime) + "]ms, returnCode [" + resultInfo.getCode() + "] returnMessage [" + resultInfo.getMessage() + "]";
                if (resultInfo.success()) {
                    logger.info(logStr);
                } else {
                    logger.error(logStr);
                }
            }
        } else {
            return methodProxy.invokeSuper(o, objects);
        }
    }
}
