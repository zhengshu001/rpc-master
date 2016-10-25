package com.hualala.core.service.config.mybatis;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

import java.util.Properties;


@Intercepts({@Signature(type=Executor.class,method="update",args={MappedStatement.class, Object.class})})
public class ParamUpdateInterceptor extends ParamInterceptor {

	public void setProperties(Properties properties) {
		
	}

	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

}
