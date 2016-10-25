package com.hualala.core.grpc.client;

import com.hualala.core.config.message.MessageInfo;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Proxy;

/**
 * 客户端的代理,所有端请求都需要这个代理处理
 * Created by xiangbin on 2016/10/18.
 */
public class ClientFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware, BeanNameAware {
    //服务的接口类
    private Class<T> rpcInterface;
    //Grpc的实现类xxxxImplBase
    private Class<?> grpcServiceImpl;
    //Grpc的生成类xxxGrpc
    private Class<?> grpcServiceOuter;

    private ApplicationContext appContext;
    private String beanName;

    @Override
    public T getObject() throws Exception {
        GrpcClientExecutor rpcExecutor = appContext.getBean(GrpcClientExecutor.class);
        MessageInfo messageInfo = appContext.getBean(MessageInfo.class);
        ClientProxy proxy = new ClientProxy(rpcInterface, grpcServiceOuter,  grpcServiceImpl, rpcExecutor, messageInfo);
        return (T)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] {rpcInterface}, proxy);
    }

    @Override
    public Class<?> getObjectType() {
        return rpcInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }

    public Class<T> getRpcInterface() {
        return rpcInterface;
    }

    public void setRpcInterface(Class<T> rpcInterface) {
        this.rpcInterface = rpcInterface;
    }

    public Class<?> getGrpcServiceImpl() {
        return grpcServiceImpl;
    }

    public void setGrpcServiceImpl(Class<?> grpcServiceImpl) {
        this.grpcServiceImpl = grpcServiceImpl;
    }

    public Class<?> getGrpcServiceOuter() {
        return grpcServiceOuter;
    }

    public void setGrpcServiceOuter(Class<?> grpcServiceOuter) {
        this.grpcServiceOuter = grpcServiceOuter;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
}
