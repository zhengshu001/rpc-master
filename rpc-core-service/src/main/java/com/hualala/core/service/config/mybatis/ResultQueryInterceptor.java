package com.hualala.core.service.config.mybatis;

import com.hualala.core.base.DataMap;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Intercepts({@Signature(type=Executor.class,method="query",args={MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class ResultQueryInterceptor extends ParamInterceptor {

	public Object intercept(Invocation invocation) throws Throwable {
		MappedStatement mappedStatement = (MappedStatement)invocation.getArgs()[0];
		Object object = super.intercept(invocation);
		List<ResultMap> resultMaps = mappedStatement.getResultMaps();
		if (resultMaps.size() == 1) {
			ResultMap resultMap = resultMaps.get(0);
			if (resultMap.getType() == DataMap.class) {
				List<DataMap> dataMapList = transResult(object);
				return dataMapList;
			}
		}
		return object;
	}

	@Override
	protected Map<String, Object> transBeforeParams(DataMap dataMap) {
		Map<String, Object> params = dataMap.toMap();
		return params;
	}

	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	public void setProperties(Properties properties) {
		
	}

	protected List<DataMap> transResult(Object object) {
		List<DataMap> retList = new ArrayList<DataMap>();
		if (object instanceof Map) {
			DataMap dataMap = new DataMap().putAll((Map)object);
			retList.add(dataMap);
		} else if ((object instanceof List)) {
			List<Map<String, Object>> list = (List<Map<String,Object>>)object;
			for (Map<String, Object> resultMap : list) {
				if (resultMap != null) {
					retList.add(new DataMap().putAll(resultMap));
				} else {
					retList.add(null);
				}
			}
		}
 		return retList;
	}

}
