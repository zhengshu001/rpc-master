package com.hualala.core.client;

import com.hualala.core.grpc.GrpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Created by xiangbin on 2016/9/28.
 */
public class BaseRpcClient {

    @Autowired
    private ApplicationContext applicationContext;

    private Logger logger = LoggerFactory.getLogger(BaseRpcClient.class);

    public <T> T getRpcClient(Class<T> rpcInterfaceClass) {
        return applicationContext.getBean(GrpcUtils.clientInterfaceBeanName(rpcInterfaceClass), rpcInterfaceClass);
    }

//    private ResultInfo rpcService(Class<?> serviceClass, String methodName, Object reqData, Class<?> resClass) {
//        try {
//            String aa = ThreadLocalCache.getValue();
//            String traceID = Optional.ofNullable(aa).orElseGet(()->UUID.randomUUID().toString());
//            Optional optional = GrpcUtils.parseRpcService(serviceClass, methodName);
//            if (!optional.isPresent()) {
//                return messageInfo.getResultMessage(ErrorInfo.RPC_SERVICE_NOT_EXIST);
//            }
//            RpcServiceData rpcServiceData = (RpcServiceData)optional.get();
//            GeneratedMessage reqMessage = GrpcUtils.modelToRpcMessage(traceID, reqData, rpcServiceData.getParameterType());
//            GeneratedMessage resMessage = super.rpcService(serviceClass, methodName, reqMessage, rpcServiceData.getReturnType());
//            return (ResultInfo)GrpcUtils.rpcMessageToModel(resMessage, resClass);
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("client remote service error", e);
//            return messageInfo.getResultMessage(ErrorInfo.RPC_CLIENT_SERVICE_ERROR);
//        }
//    }

}
