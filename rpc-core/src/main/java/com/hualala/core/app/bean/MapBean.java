package com.hualala.core.app.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiangbin on 2016/6/27.
 */
public class MapBean {

    private boolean original = true;
    List<DataBean> datas = new ArrayList<>();

    public MapBean() {

    }

    public MapBean(List<DataBean> datas) {
        this.datas.addAll(datas);
    }

    public boolean isOriginal() {
        return original;
    }

    public void setOriginal(boolean original) {
        this.original = original;
    }

    public List<DataBean> getDatas() {
        return datas;
    }

    public void setDatas(List<DataBean> datas) {
        this.datas = datas;
    }

    public void addDataBean(DataBean dataBean) {
        this.datas.add(dataBean);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("original [" + original + "] data [");
        for (DataBean dataBean : this.datas) {
            sb.append(dataBean.toString()).append(",");
        }
        sb.append("]");
        return super.toString();
    }
}
