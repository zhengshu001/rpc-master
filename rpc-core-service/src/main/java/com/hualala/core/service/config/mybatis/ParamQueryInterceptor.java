package com.hualala.core.service.config.mybatis;

import com.hualala.core.base.DataMap;
import org.apache.ibatis.plugin.Plugin;

import java.util.Map;
import java.util.Properties;

/**
 * Dataset设置参数
 * @author xiangbin
 *
 */
public class ParamQueryInterceptor extends ParamInterceptor {

	@Override
	public void setProperties(Properties properties) {

	}

	@Override
	protected Map<String, Object> transBeforeParams(DataMap dataMap) {
		Map<String, Object> params = dataMap.toMap();
//		int pageNo = dataMap.getIntValue(DataMap.PAGE_NO, 0);
//		int pageSize = dataMap.getIntValue(DataMap.PAGE_SIZE,0);
//		if (pageNo >= 0 && pageSize > 0) {
//			params.put("pageOffset", ((pageNo) - 1) * pageSize);
//			params.put("pageSize", pageSize);
//		}
		return params;
	}

	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}
}
