package com.hualala.core.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * Created by xiangbin on 2016/6/25.
 */
@Configuration
public class BaseMVCConfiguration extends WebMvcConfigurerAdapter {

    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    }

    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        BaseMessageConverter resultConverter = new BaseMessageConverter();
        converters.add(0, resultConverter);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //registry.addInterceptor(new BaseInterceptor());
        super.addInterceptors(registry);
    }
}
