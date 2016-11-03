package com.hualala.core.base;

import com.hualala.core.rpc.FieldType;
import com.hualala.core.rpc.Protocol;
import lombok.Data;

/**
 * Created by xiangbin on 2016/11/3.
 */
public class ResultPageInfo extends ResultInfo{

    @Protocol(fieldType = FieldType.OBJECT, order = 2, description = "查询分页信息")
    private ResultPageHeader pageHeader;

    public ResultPageInfo() {
        this.pageHeader = new ResultPageInfo.ResultPageHeader();
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

    public int getPageTotal() {
        return this.pageHeader.getPageTotal();
    }

    public void setPageTotal(int pageTotal) {
        this.pageHeader.setPageTotal(pageTotal);
    }

    public int getTotalSize() {
        return this.pageHeader.getTotalSize();
    }

    public void setTotalSize(int totalSize) {
        this.pageHeader.setTotalSize(totalSize);
    }

    @Data
    public static class ResultPageHeader {
        @Protocol(fieldType = FieldType.INT, order = 1, description = "开始页")
        private int pageNo;
        @Protocol(fieldType = FieldType.INT, order = 2, description = "每页大小")
        private int pageSize;
        @Protocol(fieldType = FieldType.INT, order = 3, description = "总页数")
        private int pageTotal;
        @Protocol(fieldType = FieldType.INT, order = 4, description = "总记录数")
        private int totalSize;

    }
}
