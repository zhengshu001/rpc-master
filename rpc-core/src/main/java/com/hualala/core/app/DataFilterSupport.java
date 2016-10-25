package com.hualala.core.app;

import com.hualala.core.base.DataMap;
import ognl.Ognl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by xiangbin on 2016/9/27.
 */
public class DataFilterSupport {

    protected Optional getExpressionValue(String expression, DataMap... reqData) {
        try {
            if (reqData.length == 1) {
                Map<String, Object> context = reqData[0].toMap();
                return Optional.ofNullable(Ognl.getValue(expression, context, reqData[0]));
            } else if (reqData.length == 2){
                Map<String, Object> context = new HashMap<>();
                context.put("res", reqData[0]);
                context.put("req", reqData[1]);
                return Optional.ofNullable(Ognl.getValue(expression, context, reqData[0]));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
        return Optional.empty();
    }


    protected Optional getFieldValue(String fieldName, DataMap reqData) {
        return Optional.ofNullable(reqData.get(fieldName));
    }

}
