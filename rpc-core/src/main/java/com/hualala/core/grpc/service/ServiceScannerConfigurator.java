package com.hualala.core.grpc.service;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * 服务GRPC业务的扫描处理,查找所有Grpc提供的服务信息
 * Created by xiangbin on 2016/10/18.
 */
public class ServiceScannerConfigurator implements BeanFactoryAware, BeanDefinitionRegistryPostProcessor {

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        ServiceClassPathScanner scanner = new ServiceClassPathScanner(registry);
        scanner.addIncludeFilter(new TypeFilter() {
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                String className = metadataReader.getClassMetadata().getClassName();
//                return (metadataReader.getClassMetadata().isInterface() &&
//                          className.indexOf("Builder") == -1 && className.indexOf("Client") == -1);
                boolean result = metadataReader.getClassMetadata().isAbstract();
                int index = className.indexOf("ImplBase");
                return (result && index > -1);
                // return true;
            }
        });
        List ex = AutoConfigurationPackages.get(this.beanFactory);
        scanner.scan(StringUtils.toStringArray(ex));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }


}
