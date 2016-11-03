package com.hualala.core.base;

import com.hualala.core.rpc.FieldType;
import com.hualala.core.rpc.Protocol;
import lombok.Data;

/**
 * Created by xiangbin on 2016/11/3.
 */
public class RequestPageInfo extends RequestInfo {

    @Protocol(fieldType = FieldType.OBJECT, order = 2, description = "查询分页信息")
    private RequestPageHeader pageHeader;

    public RequestPageInfo() {
        this.pageHeader = new RequestPageHeader();
    }

    public void setPageNo(int pageNo) {
        this.pageHeader.pageNo = pageNo;
    }

    public void setPageSize(int pageSize) {
        this.pageHeader.pageSize = pageSize;
    }

    public int getPageNo() {
        return this.pageHeader.getPageNo();
    }

    public int getPageSize() {
        return this.pageHeader.getPageSize();
    }

    @Data
    public static class RequestPageHeader {
        @Protocol(fieldType = FieldType.INT, order = 1, description = "开始页")
        private int pageNo;
        @Protocol(fieldType = FieldType.INT, order = 2, description = "每页大小")
        private int pageSize;
    }

}
