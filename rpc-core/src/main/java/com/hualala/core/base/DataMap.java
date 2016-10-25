package com.hualala.core.base;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class DataMap extends ResultInfo {

	private final Map<String, Object> datas = new HashMap<>();
	
	public DataMap() {
		super(SUCCESS_CODE, SUCCESS_MESSAGE);
	}

	public int size() {
		return datas.size();
	}

	public boolean isEmpty() {
		return this.datas.isEmpty();
	}

	public boolean contains(String key) {
		return this.datas.containsKey(key);
	}

	public void clear() {
		this.datas.clear();
	}

	public Object get(String key) {
		return this.datas.get(key);
	}

	public Set<String> keySet() {
		return this.datas.keySet();
	}

	public DataMap put(String key, Object value) {
		this.datas.put(key, value);
		return this;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> ret = new HashMap();
		this.datas.keySet().stream().forEach((key) -> {
			Object value = this.datas.get(key);
			if (value instanceof DataMap) {
				ret.put(key, ((DataMap)value).toMap());
			} else if (value instanceof List) {
				ret.put(key, ((List)value).stream().map(object -> {
					if (object instanceof DataMap) {
						return ((DataMap)object).toMap();
					} else {
						return object;
					}
				}).collect(Collectors.toList()));
			} else {
				ret.put(key, value);
			}
		});
		return ret;
	}

	@Override
	public String toJson() {
		Map<String, Object> map = super.toMap();
		map.put("data", toMap());
		return new JSONObject(map).toJSONString();
	}


	public DataMap putAll(Map<String, Object> dataMap) {
		this.datas.putAll(dataMap);
		return this;
	}

	public DataMap putAll(DataMap dataMap) {
		this.datas.putAll(dataMap.datas);
		return this;
	}

	public Object remove(String key) {
		return this.datas.remove(key);
	}
}
