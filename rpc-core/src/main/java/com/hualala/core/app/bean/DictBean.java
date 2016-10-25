package com.hualala.core.app.bean;

/**
 * Created by xiangbin on 2016/10/21.
 */
public class DictBean {

    public static final String ELE_NAME = "dict";
    public static final String ATTR_KEY = "key";
    public static final String ATTR_VALUE = "value";
    private final String key;
    private final String value;

    public DictBean(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }
    public String getValue() {
        return this.value;
    }

}
