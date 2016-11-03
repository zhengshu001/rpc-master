package com.hualala.core.grpc.client;

import com.hualala.core.grpc.GrpcUtils;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiangbin on 2016/11/2.
 */
public class GrpcClient {

    private static final Logger logger = LoggerFactory.getLogger(GrpcClient.class);
    private String target;
    private ManagedChannel channel;
    private static final String FUTURE_STUB_STR = "FutureStub";
    private static final String BLOCKING_STUB_STR = "BlockingStub";
    private boolean isInit = false;

    public GrpcClient(String target) {
        this.target = target;
        logger.info("channel for target {}.", target);
        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext(true).build();
        isInit = true;
    }

    public GrpcClient() {

    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void init() {
        if (!isInit) {
            logger.info("channel for target {}.", target);
            this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext(true).build();
            isInit = true;
        }
    }


    public void clean() throws InterruptedException {
        this.channel.shutdown().awaitTermination(5L, TimeUnit.SECONDS);
    }

    public Object getFutureStub(Class grpcClass) throws Exception {
        return this.getStub(grpcClass, "newFutureStub");
    }

    public Object getBlockingStub(Class grpcClass) throws Exception {
        return this.getStub(grpcClass, "newBlockingStub");
    }

    private Object getStub(Class grpcClass, String stubMethodName) throws Exception {
        Method stubMethod = grpcClass.getMethod(stubMethodName, new Class[]{Channel.class});
        return stubMethod.invoke((Object)null, new Object[]{this.channel});
    }

    public Object getStub(Class<?> grpcClass, Class<?> stubClass) throws Exception {
        String name = stubClass.getName();
        int len = name.length();
        if("FutureStub".equals(name.substring(len - "FutureStub".length(), len))) {
            return this.getFutureStub(grpcClass);
        } else if("BlockingStub".equals(name.substring(len - "BlockingStub".length(), len))) {
            return this.getBlockingStub(grpcClass);
        } else {
            throw new IllegalArgumentException("不支持的Stub Class");
        }
    }
}
