package com.hualala.core.grpc.service;

import com.hualala.core.config.message.MessageInfo;
import com.hualala.core.grpc.GrpcUtils;
import io.grpc.BindableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Arrays;
import java.util.Optional;


/**
 * Created by xiangbin on 2016/8/12.
 */
public class ServiceFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware, BeanNameAware {

    private Enhancer enhancer = new Enhancer();

    private Logger logger = LoggerFactory.getLogger(ServiceFactoryBean.class);

    //Grpc的实现类xxxxImplBase
    private Class<?> grpcServiceImpl;
    //Grpc的生成类xxxGrpc
    private Class<?> grpcServiceOuter;
    //实现的接口
    private Class<T> rpcInterface;

    private ApplicationContext appContext;

    @Override
    public T getObject() throws Exception {
        ServiceExecutor rpcExecutor = appContext.getBean(ServiceExecutor.class);
        MessageInfo messageInfo = appContext.getBean(MessageInfo.class);
        String[] rpcInterfaceBeanNames = appContext.getBeanNamesForType(rpcInterface);
        Optional rpcServiceOptional = Arrays.asList(rpcInterfaceBeanNames).stream().filter(beanName->
            !GrpcUtils.clientInterfaceBeanName(rpcInterface).equals(beanName)
        ).map(beanName-> appContext.getBean(beanName, rpcInterface)).findFirst();
        if (!rpcServiceOptional.isPresent()) {
            throw new IllegalArgumentException("not found rpcInterface [" + rpcInterface.getName() + "] implement");
        }
        Object rpcServiceImpl = rpcServiceOptional.get();
        ServiceProxy rpcProxy = new ServiceProxy(rpcInterface, rpcServiceImpl, grpcServiceOuter, grpcServiceImpl, rpcExecutor, messageInfo);
        enhancer.setSuperclass(grpcServiceImpl);
        enhancer.setInterfaces(new Class[] {BindableService.class});
        enhancer.setCallback(rpcProxy);
        return (T) enhancer.create();
    }

    @Override
    public Class<?> getObjectType() {
        return BindableService.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public Class<T> getRpcInterface() {
        return rpcInterface;
    }

    public void setRpcInterface(Class<T> rpcInterface) {
        this.rpcInterface = rpcInterface;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
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

    private String beanName;
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
}
