package com.hualala.core.rpc;


/**
 * Java对应的Grpc类型
 * Created by xiangbin on 2016/10/26.
 */
public enum FieldType {

    DOUBLE("double", com.google.protobuf.WireFormat.FieldType.DOUBLE, 2),
    FLOAT("float", com.google.protobuf.WireFormat.FieldType.FLOAT, 2),
    LONG("int64", com.google.protobuf.WireFormat.FieldType.INT64, 2),
    INT("int32", com.google.protobuf.WireFormat.FieldType.INT32, 2),
    BOOL("bool", com.google.protobuf.WireFormat.FieldType.BOOL,  2),
    STRING("string", com.google.protobuf.WireFormat.FieldType.STRING, 0),
    OBJECT("message", com.google.protobuf.WireFormat.FieldType.MESSAGE, 0),
    ENUM("enum", com.google.protobuf.WireFormat.FieldType.ENUM, 0);

    private String type;
    private com.google.protobuf.WireFormat.FieldType internalFieldType;
    private int repeatedWireType;

    public String getType() {
        return type;
    }

    public int getWireType() {
        return this.internalFieldType.getWireType();
    }

    public int getRepeatedWireType() {
        return this.repeatedWireType;
    }

    FieldType(String type, com.google.protobuf.WireFormat.FieldType internalFieldType, int repeatedWireType) {
        this.type = type;
        this.internalFieldType = internalFieldType;
        this.repeatedWireType = repeatedWireType;
    }
}
