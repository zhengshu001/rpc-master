package com.hualala.core.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hualala.core.app.bean.*;
import com.hualala.core.base.DataMap;
import com.hualala.core.grpc.GrpcData;
import com.hualala.core.grpc.GrpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by xiangbin on 2016/9/27.
 */
public class DataXmlFilter extends DataFilterSupport implements ApplicationContextAware{

    private static Logger logger = LoggerFactory.getLogger(DataXmlFilter.class);

    private ApplicationContext appContext;

    public Optional parseRpcData(String id) {
        if (!appContext.containsBean(id)) {
            logger.error("not found id [" + id + "] config filter");
            return Optional.empty();
        }
        FilterBean filterBean = appContext.getBean(id, FilterBean.class);
        if (logger.isDebugEnabled()) {
            logger.info("id [" + id + "] filterBean [" + filterBean + "]");
        }
        Class<?> rpcInterface = ClassUtils.resolveClassName(filterBean.getService(), ClassUtils.getDefaultClassLoader());
        Optional<GrpcData> optional = GrpcUtils.parseGrpcServiceInfo(rpcInterface, filterBean.getMethod());
        if (!optional.isPresent()) {
            return optional;
        }
        GrpcData grpcData = optional.get();
        grpcData.setReqFilter(filterBean.getRequest());
        grpcData.setResFilter(filterBean.getResponse());
        return optional;
    }

    public DataMap reqDataFilter(String id, Object configFilter, DataMap reqData) {
        if (configFilter == null) {
            if (logger.isInfoEnabled()) {
                logger.info("id [" + id + "] request no config");
            }
            return reqData;
        }

        RequestBean requestBean = (RequestBean)configFilter;
        DataMap newReqData = dataFilter(id, requestBean.getDataBeens(), requestBean.isInclude(), reqData);
        newReqData.setTraceID(reqData.getTraceID());
        return newReqData;
    }

    public DataMap resDataFilter(String id, Object configFilter, DataMap reqData, DataMap resData) {
        if (configFilter == null) {
            if (logger.isInfoEnabled()) {
                logger.info("id [" + id + "] response no config");
            }
            return resData;
        }
        ResponseBean responseBean = (ResponseBean)configFilter;
        DataMap newResData = dataFilter(id, responseBean.getDataBeens(), responseBean.isInclude(), resData, reqData);
        newResData.setTraceID(resData.getTraceID()).setCode(resData.getCode()).setMessage(resData.getMessage());
        return newResData;
    }

    private DataMap dataFilter(String id, List<DataBean> dataBeanList, boolean include, DataMap... reqData) {
        DataMap target = new DataMap();
        if (include) {
            target.putAll(reqData[0]);
        }
        ConfigurablePropertyResolver resolver = appContext.getBean(ConfigurablePropertyResolver.class);
        ConversionService conversionService = resolver.getConversionService();
        dataBeanList.stream().forEach(data -> {
            String field = data.getKey();
            if (data.isDelete()) {
                target.remove(field);
            } else if (data.getValue() != null && !"".equals(data.getValue())) {
                setField(field, target, parseDictValue(data.getValue(), data.getDictBeens()), data.getType(), conversionService);
            } else if (data.getSource() != null && !"".equals(data.getSource()) && reqData.length > 0) {
                Optional valueOptional = getFieldValue(data.getSource(), reqData[0]);
                if (valueOptional.isPresent()) {
                    setField(field, target, parseDictValue(valueOptional.get(), data.getDictBeens()), data.getType(), conversionService);
                }
            } else if (data.getExpression() != null && !"".equals(data.getExpression())) {
                Optional valueOptional = getExpressionValue(data.getExpression(), reqData);
                if (valueOptional.isPresent()) {
                    setField(field, target, parseDictValue(valueOptional.get(), data.getDictBeens()), data.getType(), conversionService);
                }
            } else {
                Optional valueOptional = getExtraValue(data.getType(), data.getExtra());
                if (valueOptional.isPresent()) {
                    setField(field, target, parseDictValue(valueOptional.get(), data.getDictBeens()), data.getType(), conversionService);
                }
            }
        });
        return target;
    }

    private Object parseDictValue(Object value, List<DictBean> dictBeanList) {
        if (dictBeanList == null || dictBeanList.size() == 0) {
            return value;
        }
        Optional optional = dictBeanList.stream().filter(dictBean -> dictBean.getKey().equals(value.toString())).map(dictBean -> dictBean.getValue()).findFirst();
        if (optional.isPresent()) {
            return optional.get();
        }
        return value;
    }

    private Optional getExtraValue(DataBean.Type type, String extra) {
        if (type == DataBean.Type.UUID) {
            return Optional.of(UUID.randomUUID().toString());
        } else if (type == DataBean.Type.DATE) {
            String pattern = StringUtils.isEmpty(extra) ? "yyyyMMddHHmmss" : extra;
            return Optional.of(DateTimeFormatter.ofPattern(pattern).format(LocalDateTime.now()));
        } else if (type == DataBean.Type.JSON) {

        }
        return Optional.empty();
    }

    private void setField(String key, DataMap target, Object value, DataBean.Type type, ConversionService conversionService) {
        if (logger.isDebugEnabled()) {
            logger.debug("key [" + key + "] value [" + value + "] type [" + type + "]");
        }
        if (type == DataBean.Type.ORIG) {
            target.put(key, value);
        } else if (type == DataBean.Type.JSON) {
            if (StringUtils.isEmpty(value)) {
                return;
            }
            Object objects = JSON.parse(value.toString());
            if (objects instanceof JSONArray) {
                target.put(key, toDataMapList((JSONArray)objects));
            } else if (objects instanceof  JSONObject){
                target.put(key, toDataMap((JSONObject)objects));
            }
        } else {
            if (conversionService.canConvert(value.getClass(), type.getValue())) {
                target.put(key, conversionService.convert(value, TypeDescriptor.valueOf(value.getClass()), TypeDescriptor.valueOf(type.getValue())));
            }
        }
    }

    private List<Object> toDataMapList(JSONArray jsonArray) {
        List<Object> results = new ArrayList<>(jsonArray.size());
        jsonArray.stream().forEach(object -> {
            if (object instanceof JSONArray) {
                object = toDataMapList((JSONArray) object);
            } else if (object instanceof JSONObject) {
                object = toDataMap((JSONObject) object);
            }
            results.add(object);
        });
        return results;
    }

    private DataMap toDataMap(JSONObject jsonObject) {
        DataMap dataMap = new DataMap();
        jsonObject.keySet().stream().forEach(key -> {
            Object object = jsonObject.get(key);
            if (object instanceof JSONArray) {
                object = toDataMapList((JSONArray)object);
            } else if (object instanceof JSONObject) {
                object = toDataMap((JSONObject)object);
            }
            dataMap.put(key, object);
        });
        return dataMap;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }
}
