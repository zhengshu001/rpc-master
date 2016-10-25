package com.hualala.core.service.config.mybatis;

import com.hualala.core.base.DataMap;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;

import java.util.HashMap;
import java.util.List;

/**
 * Created by xiangbin on 2016/9/6.
 */
public class MybatisObjectFactory extends DefaultObjectFactory  {

    private static final long serialVersionUID = 1L;

    @Override
    public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
        if (type == DataMap.class) {
            return (T)new HashMap<String, Object>();
        }
        return super.create(type, constructorArgTypes, constructorArgs);
    }
}
