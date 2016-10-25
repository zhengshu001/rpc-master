package com.hualala.core.app.bean;

public class FilterBean {

	public static final String ATTR_ID = "uri";
	public static final String ATTR_SERVICE = "service";
	public static final String ATTR_METHOD = "method";
	public static final String ELE_REQUEST = "request";
	public static final String ELE_RESPONSE = "response";


	private String service;
	private boolean result;
	private String method;
	private RequestBean request;
	private ResponseBean response;

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public RequestBean getRequest() {
		return request;
	}

	public void setRequest(RequestBean request) {
		this.request = request;
	}

	public ResponseBean getResponse() {
		return response;
	}

	public void setResponse(ResponseBean response) {
		this.response = response;
	}
}
