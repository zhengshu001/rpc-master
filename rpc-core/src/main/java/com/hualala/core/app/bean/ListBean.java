package com.hualala.core.app.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiangbin on 2016/6/26.
 */
public class ListBean {

    public ListBean() {

    }

    public ListBean(List<DataBean> datas) {
        this.datas.addAll(datas);
    }

    private boolean original;

    private int count = 1;

    private String index = "index";

    List<DataBean> datas = new ArrayList<>();

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
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

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("original [" + original + "] count [" + count + "] data [");
        for (DataBean dataBean : this.datas) {
            sb.append(dataBean.toString()).append(",");
        }
        sb.append("]");
        return super.toString();
    }
}
