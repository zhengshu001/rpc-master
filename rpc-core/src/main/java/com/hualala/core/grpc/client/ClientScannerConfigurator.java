package com.hualala.core.grpc.client;

import com.hualala.core.grpc.GrpcUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * 端GRPC业务的扫描处理,查找所有Grpc提供的服务信息
 * Created by xiangbin on 2016/10/18.
 */
public class ClientScannerConfigurator  implements BeanFactoryAware, BeanDefinitionRegistryPostProcessor {

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        ClientClassPathScanner scanner = new ClientClassPathScanner(registry);
        scanner.setBeanNameGenerator(new AnnotationBeanNameGenerator() {
            @Override
            public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
                return GrpcUtils.clientInterfaceBeanName(definition.getBeanClassName());
            }
        });
        scanner.addIncludeFilter(new TypeFilter() {
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                String className = metadataReader.getClassMetadata().getClassName();
                boolean result = metadataReader.getClassMetadata().isAbstract();
                int index = className.indexOf(GrpcUtils.GRPC_SERVICE_IMPL_SUFFIX);
                return (result && index > -1);
            }
        });
        List ex = AutoConfigurationPackages.get(this.beanFactory);
        scanner.scan(StringUtils.toStringArray(ex));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
