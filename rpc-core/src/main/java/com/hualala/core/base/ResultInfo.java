package com.hualala.core.base;

import com.alibaba.fastjson.JSONObject;
import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import com.hualala.commons.mybatis.item.BaseItem;
import com.hualala.core.rpc.FieldType;
import com.hualala.core.rpc.Protocol;
import org.springframework.util.ReflectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ResultInfo extends BaseItem {

	protected static String SUCCESS_CODE = "000";
	protected static String SUCCESS_MESSAGE = "SUCCESS";
	@Protobuf(fieldType = com.baidu.bjf.remoting.protobuf.FieldType.OBJECT, order = 1, description = "响应公共字段")
	@Protocol(fieldType = FieldType.OBJECT, order = 1, description = "响应公共字段")
	private ResultHeader result;

	private Object[] params;

	public ResultInfo() {
		this(SUCCESS_CODE);
	}

	public ResultInfo(String code) {
		this.result = new ResultHeader();
		this.result.code = code;
		this.result.success = SUCCESS_CODE.equals(this.result.code);
	}

	public ResultInfo(String code, String message) {
		this(code);
		this.result.message = message;
	}

	public ResultInfo(String code, Object[] params) {
		this(code);
		this.params = params;
	}

	public String getCode() {
		return this.result.code;
	}

	public String getMessage() {
		return this.result.message;
	}

	public boolean success() {
		return SUCCESS_CODE.equals(this.result.success);
	}

	public ResultInfo setCode(String code) {
		this.result.code = code;
		return this;
	}

	public ResultInfo setMessage(String message) {
		this.result.message = message;
		return this;
	}

	public Object[] getMessageParams() {
		return this.params;
	}
	public String getTraceID() {
		return this.result.traceID;
	}

	public ResultInfo setTraceID(String traceID) {
		this.result.traceID = traceID;
		return this;
	}

	public <T extends ResultInfo> T setResultInfo(String code) {
		return setResultInfo(code, new Object[]{});
	}

	public <T extends ResultInfo> T setResultInfo(String code, Object[] params) {
		this.result.code = code;
		this.params = params;
		this.result.success = SUCCESS_CODE.equals(this.result.code);
		return (T)this;
	}

	public <T extends ResultInfo> T setResultInfo(String code, String message) {
		this.result.code = code;
		this.result.message = message;
		this.result.success = SUCCESS_CODE.equals(this.result.code);
		return (T)this;
	}

	public <T extends ResultInfo> T setResultInfo(Object[] params) {
		this.params = params;
		return (T)this;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ResultInfo)) {
			return false;
		}
		ResultInfo impl = (ResultInfo)obj;
		return this.result.code.equals(impl.result.code) && this.result.message.equals(this.result.message);
	}

	public Map<String, Object> toMap() {
		Map<String,Object> map = new HashMap();
		map.put("code", this.result.code);
		map.put("message", this.result.message);
		map.put("traceID", this.result.traceID);
		return map;
	}

	public String toJson() {
		Map<String,Object> map = toMap();
		Arrays.asList(this.getClass().getDeclaredFields()).stream().forEach((field -> {
			ReflectionUtils.makeAccessible(field);
			if ("SUCCESS_CODE".equals(field.getName())) {

			} else if ("SUCCESS_MESSAGE".equals(field.getName())) {

			} else if ("result".equals(field.getName())) {

			} else {
				map.put(field.getName(), ReflectionUtils.getField(field, this));
			}
		}));
		return new JSONObject(map).toJSONString();
	}

	@Override
	public String toString() {
		return toJson();
	}

	public static class ResultHeader {

		@Protobuf(fieldType = com.baidu.bjf.remoting.protobuf.FieldType.STRING, order = 1, required = true)
		@Protocol(fieldType = FieldType.STRING, order = 1)
		private String traceID;
		@Protocol(fieldType = FieldType.STRING, order = 2)
		@Protobuf(fieldType = com.baidu.bjf.remoting.protobuf.FieldType.STRING, order = 2, required = true)
		private String code;
		@Protocol(fieldType = FieldType.STRING, order = 3)
		@Protobuf(fieldType = com.baidu.bjf.remoting.protobuf.FieldType.STRING, order = 3, required = true)
		private String message;
		@Protocol(fieldType = FieldType.BOOL, order = 4)
		@Protobuf(fieldType = com.baidu.bjf.remoting.protobuf.FieldType.BOOL, order = 4, required = true)
		private boolean success;

	}

}
