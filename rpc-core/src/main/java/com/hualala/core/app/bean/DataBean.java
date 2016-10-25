package com.hualala.core.app.bean;

import java.util.List;
import java.util.Map;

/**
 * Created by xiangbin on 2016/6/12.
 */
public class DataBean {
    public static final String ELE_NAME = "data";
    public static final String ATTR_KEY = "key";
    public static final String ATTR_VALUE = "value";
    public static final String ATTR_SOURCE = "source";
    public static final String ATTR_EXPRESSION = "expression";
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_EXTRA = "extra";
    public static final String ATTR_DELETE = "delete";
    public static final String ELE_MAP = "map";
    public static final String ELE_LIST = "list";
    private final  String key;
    private String value;
    private String source;
    private String expression;
    private Type type = Type.ORIG;
    private String extra;
    private MapBean mapBean;
    private ListBean listBean;
    private boolean delete;

    private List<DictBean> dictBeens;
    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public enum Type {
        ORIG(String.class),
        INT(int.class),
        DOUBLE(double.class),
        DATE(String.class),
        JSON(String.class),
        UUID(String.class),
        MAP(Map.class),
        LIST(List.class);
        private Class value;
        private Type(Class value) {
            this.value = value;
        }
        public Class<?> getValue() {
            return value;
        }
    }

    public MapBean getMapBean() {
        return mapBean;
    }

    public void setMapBean(MapBean mapBean) {
        this.mapBean = mapBean;
    }

    public ListBean getListBean() {
        return listBean;
    }

    public void setListBean(ListBean listBean) {
        this.listBean = listBean;
    }

    public DataBean(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getKey() {
        return key;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public void setDictBeens(List<DictBean> dictBeens) {
        this.dictBeens = dictBeens;
    }

    public List<DictBean> getDictBeens() {
        return this.dictBeens;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("key [" + key + "] value [");
        if (this.value != null) {
            sb.append("value:"+this.value);
        } else if (this.source != null) {
            sb.append("source:" + this.source);
        } else if (this.expression != null) {
            sb.append("expression:" + this.expression);
        } else if (this.mapBean != null) {
            sb.append("map:" + this.mapBean.toString());
        } else if (this.listBean != null) {
            sb.append("list:" + this.listBean.toString());
        }
        sb.append("]");
        if (this.type != null) {
            sb.append(" type [" + this.type.toString() + "]");
        }
        if (this.extra != null) {
            sb.append(" extra [" + this.extra + "]");
        }
        return sb.toString();
    }
}
