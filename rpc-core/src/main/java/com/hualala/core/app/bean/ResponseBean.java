package com.hualala.core.app.bean;

import java.util.List;

/**
 * Created by xiangbin on 2016/9/27.
 */
public class ResponseBean {
    public static final String ATTR_INCLUDE = "include";
    private boolean include = true;
    private List<DataBean> dataBeens;

    public List<DataBean> getDataBeens() {
        return dataBeens;
    }

    public void setDataBeens(List<DataBean> dataBeens) {
        this.dataBeens = dataBeens;
    }

    public boolean isInclude() {
        return include;
    }

    public void setInclude(boolean include) {
        this.include = include;
    }
}
