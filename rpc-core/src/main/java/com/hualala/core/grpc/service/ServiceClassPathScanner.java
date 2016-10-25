package com.hualala.core.grpc.service;

import com.hualala.core.grpc.GrpcUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

/**
 * Created by xiangbin on 2016/10/18.
 */

public class ServiceClassPathScanner extends ClassPathBeanDefinitionScanner {

    private DefaultListableBeanFactory beanFactory;
    public ServiceClassPathScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
        beanFactory = (DefaultListableBeanFactory)registry;
    }


    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (beanDefinitions.isEmpty()) {
            logger.warn("No grpc was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
        } else {
            for (BeanDefinitionHolder holder : beanDefinitions) {
                GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
                String beanClassName = definition.getBeanClassName();
                if (logger.isInfoEnabled()) {
                    logger.info("Creating ServiceFactoryBean with name '" + holder.getBeanName() + "' and '" +  beanClassName + "' RpcInterface");
                }

                try {
                    if (beanClassName.endsWith(GrpcUtils.GRPC_SERVICE_IMPL_SUFFIX)) {
                        String rpcInterface = GrpcUtils.parseGrpcInterface(beanClassName);
                        String grpcServiceOuter = GrpcUtils.parseGrpcServiceOuter(beanClassName);
                        if (logger.isInfoEnabled()) {
                            logger.info("Creating ServiceFactoryBean rpcInterface [" + rpcInterface + "] grpcServiceImpl [" +  beanClassName + "] grpcServiceOuter [" + grpcServiceOuter + "]");
                        }
                        definition.getPropertyValues().add("rpcInterface", rpcInterface);
                        definition.getPropertyValues().add("grpcServiceImpl", beanClassName);
                        definition.getPropertyValues().add("grpcServiceOuter", grpcServiceOuter);
                        definition.setBeanClass(ServiceFactoryBean.class);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return beanDefinitions;
    }
    @Override
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
        //过滤掉已经注册过的服务实现
        return true;
//        try {
//            boolean result = super.checkCandidate(beanName, beanDefinition);
//            if (!result) {
//                return result;
//            }
//            Class<?> beanClass = ((AbstractBeanDefinition)beanDefinition).resolveBeanClass(beanDefinition.getClass().getClassLoader());
//            if (beanFactory.getBeanNamesForType(beanClass).length > 0) {
//                if (logger.isInfoEnabled()) {
//                    logger.info("client class '" + beanClass + "' already impl, no need proxy");
//                }
//                return false;
//            }
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
    }

    @Override
    protected boolean isCandidateComponent(MetadataReader metadataReader) throws IOException {
        return super.isCandidateComponent(metadataReader);
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return (beanDefinition.getMetadata().isAbstract() && beanDefinition.getMetadata().isIndependent());
    }
}
