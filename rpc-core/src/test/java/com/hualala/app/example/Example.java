package com.hualala.app.example;

import com.hualala.core.base.RequestInfo;
import com.hualala.core.base.ResultInfo;
import com.hualala.core.rpc.FieldType;
import com.hualala.core.rpc.Protocol;
import lombok.Data;

import java.util.List;

/**
 * Created by xiangbin on 2016/10/26.
 */
public interface Example {

    HelloResData hello(HelloReqData reqData);

    ComplexResData helloComplex(ComplexReqData resData);

    @Data
    class ComplexReqData extends RequestInfo {
        @Protocol(fieldType = FieldType.STRING, order = 2, description = "请求字符列子")
        private String req;
        @Protocol(fieldType = FieldType.OBJECT, order = 3, description = "测试list")
        private List<DetailData> details;
        @Protocol(fieldType = FieldType.OBJECT, order = 4,description = "测试message")
        private DetailData reqDetail;
        @Protocol(fieldType = FieldType.STRING, order = 5, description = "测试list")
        private List<String> listString;
        @Protocol(fieldType = FieldType.INT, order = 6, description = "测试list")
        private List<Integer> listInt;
        @Protocol(fieldType = FieldType.ENUM, order = 7)
        private BankCodeEnum bankCodeEnum;
        @Protocol(fieldType = FieldType.LONG, order = 8)
        private long resLong;
        @Protocol(fieldType = FieldType.INT, order = 9)
        private Integer resInt;
        @Protocol(fieldType = FieldType.FLOAT, order = 10)
        private Float resFloat;
        @Protocol(fieldType = FieldType.DOUBLE, order = 11)
        private Double resDouble;
        @Protocol(fieldType = FieldType.LONG, order = 12)
        private List<Long> listLong;
        @Protocol(fieldType = FieldType.FLOAT, order = 13)
        private List<Float> listFloat;
        @Protocol(fieldType = FieldType.DOUBLE, order = 14)
        private List<Double> listDouble;
    }

    class DetailData {
        @Protocol(fieldType = FieldType.STRING, order = 1)
        private String itemID;
        @Protocol(fieldType = FieldType.STRING, order = 2)
        private String name;

        public String getItemID() {
            return itemID;
        }

        public DetailData setItemID(String itemID) {
            this.itemID = itemID;
            return this;
        }

        public String getName() {
            return name;
        }

        public DetailData setName(String name) {
            this.name = name;
            return this;
        }
    }

    @Data
    class ComplexResData extends ResultInfo {
        @Protocol(fieldType = FieldType.STRING, order = 2)
        private String res;
        @Protocol(fieldType = FieldType.OBJECT, order = 3)
        private List<DetailData> retDetails;
        @Protocol(fieldType = FieldType.ENUM, order = 4)
        private BankCodeEnum bankCode;
        @Protocol(fieldType = FieldType.OBJECT, order = 5)
        private DetailData detail;
        @Protocol(fieldType = FieldType.STRING, order = 6)
        private List<String> listString;
        @Protocol(fieldType = FieldType.INT, order = 7)
        private List<Integer> listInt;
    }

    enum BankCodeEnum {
        WEIXIN_WAP(10),         //微信公众号支付 111
        WEIXIN_QRCODE(1),      //微信二维码支付 112
        WEIXIN_SCAN(2),      //微信刷卡支付 113
        ALIPAY_WAP(3),        //支付宝微信里支付 121
        ALIPAY_QRCODE(4);      //支付宝二维码 122

        private final int value;

        BankCodeEnum(int value) { this.value = value; }

        public int value() { return this.value; }
    }

    @Data
    class HelloReqData {
        @Protocol(fieldType = FieldType.STRING, order = 1)
        String paramStr;
        @Protocol(fieldType = FieldType.INT, order = 2)
        int paramInt;
        @Protocol(fieldType = FieldType.LONG, order = 3)
        long paramLong;
    }
    @Data
    class HelloResData {
        @Protocol(fieldType = FieldType.STRING, order = 1)
        String paramStr;
        @Protocol(fieldType = FieldType.INT, order = 2)
        int paramInt;
        @Protocol(fieldType = FieldType.LONG, order = 3)
        long paramLong;
    }
}
