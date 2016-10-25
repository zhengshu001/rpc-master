package com.hualala.core.service.config.mybatis;

import com.hualala.core.base.DataMap;
import com.hualala.core.utils.DateUtils;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class ParamInterceptor implements Interceptor {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@SuppressWarnings("unchecked")
	public Object intercept(Invocation invocation) throws Throwable {
		if (invocation.getArgs()[1] instanceof DataMap) {
			DataMap sourceDs = (DataMap)invocation.getArgs()[1];
			Map<String, Object> params = transBeforeParams(sourceDs);
			invocation.getArgs()[1] = params;
			Object result = invocation.proceed();
			invocation.getArgs()[1] = transAfterParams(sourceDs, params);
			return result;
		} else {
			Object result = invocation.proceed();
			return result;
		}
	}

	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	protected Map<String, Object> transBeforeParams(DataMap dataMap) {
		Map<String, Object> params = dataMap.toMap();
		if (!params.containsKey("action")) {
			params.put("action", "0");
		}
		String dateTime = DateUtils.getCurrentDateTime();
		if (!params.containsKey("actionTime")) {
			params.put("actionTime", dateTime);
		}
		if (!params.containsKey("createTime")) {
			params.put("createTime", dateTime);
		}
		params.put("currentTime", dateTime);
		return params;
	}

	protected DataMap transAfterParams(DataMap dataMap, Map<String, Object> params) {
		for (String key : params.keySet()) {
			if (!dataMap.contains(key)) {
				dataMap.put(key, params.get(key));
			}
		}
		return dataMap;
	}

}

