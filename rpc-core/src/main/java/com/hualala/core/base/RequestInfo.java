package com.hualala.core.base;

import com.baidu.bjf.remoting.protobuf.FieldType;
import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

/**
 * Created by xiangbin on 2016/10/10.
 */
public class RequestInfo {

    @Protobuf(fieldType = FieldType.OBJECT, order = 1, required = true, description = "请求公共字段")
    private RequestHeader header;


    public String getTraceID() {
        if (this.header == null) {
            return null;
        }
        return this.header.traceID;
    }

    public void setTraceID(String traceID) {
        if (this.header == null) {
            this.header = new RequestHeader();
        }
        this.header.traceID = traceID;
    }



    public static class RequestHeader {
        @Protobuf(fieldType = FieldType.STRING, order = 1, required = true)
        private String traceID;
    }
}
