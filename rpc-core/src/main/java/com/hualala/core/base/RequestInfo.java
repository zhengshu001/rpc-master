package com.hualala.core.base;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import com.hualala.core.rpc.FieldType;
import com.hualala.core.rpc.Protocol;

/**
 * Created by xiangbin on 2016/10/10.
 */
public class RequestInfo {

    @Protocol(fieldType = FieldType.OBJECT, order = 1, description = "请求公共字段")
    @Protobuf(fieldType = com.baidu.bjf.remoting.protobuf.FieldType.OBJECT, order = 1, description = "请求公共字段")
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
        @Protocol(fieldType = FieldType.STRING, order = 1)
        @Protobuf(fieldType = com.baidu.bjf.remoting.protobuf.FieldType.OBJECT, order = 1)
        private String traceID;
    }
}
