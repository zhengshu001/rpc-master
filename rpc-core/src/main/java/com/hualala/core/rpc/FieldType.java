package com.hualala.core.rpc;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Java对应的Grpc类型
 * Created by xiangbin on 2016/10/26.
 */
public enum FieldType {

    DOUBLE("Double", "double", "WIRETYPE_FIXED64", ReflectionUtils.findMethod(Double.class, "doubleValue") , com.google.protobuf.WireFormat.FieldType.DOUBLE, 2, "0d", ReflectionUtils.findMethod(CodedOutputStream.class, "computeDoubleSize", int.class, double.class), ReflectionUtils.findMethod(CodedOutputStream.class, "computeDoubleSizeNoTag", double.class), ReflectionUtils.findMethod(CodedOutputStream.class, "writeDouble", int.class, double.class), ReflectionUtils.findMethod(CodedOutputStream.class, "writeDoubleNoTag", double.class), ReflectionUtils.findMethod(CodedInputStream.class, "readDouble"), com.google.protobuf.WireFormat.FieldType.STRING),
    FLOAT("Float", "float", "WIRETYPE_FIXED32", ReflectionUtils.findMethod(Float.class, "floatValue"), com.google.protobuf.WireFormat.FieldType.FLOAT, 2, "0f",ReflectionUtils.findMethod(CodedOutputStream.class, "computeFloatSize", int.class, float.class), ReflectionUtils.findMethod(CodedOutputStream.class, "computeFloatSizeNoTag", float.class),ReflectionUtils.findMethod(CodedOutputStream.class, "writeFloat", int.class, float.class), ReflectionUtils.findMethod(CodedOutputStream.class, "writeFloatNoTag", float.class), ReflectionUtils.findMethod(CodedInputStream.class, "readFloat"), com.google.protobuf.WireFormat.FieldType.STRING),
    INT64("Long", "int64", "WIRETYPE_VARINT", ReflectionUtils.findMethod(Long.class, "longValue"), com.google.protobuf.WireFormat.FieldType.INT64, 2, "0L",ReflectionUtils.findMethod(CodedOutputStream.class, "computeInt64Size", int.class, long.class), ReflectionUtils.findMethod(CodedOutputStream.class, "computeInt64SizeNoTag", long.class),ReflectionUtils.findMethod(CodedOutputStream.class, "writeInt64", int.class, long.class), ReflectionUtils.findMethod(CodedOutputStream.class, "writeInt64NoTag", long.class), ReflectionUtils.findMethod(CodedInputStream.class, "readInt64"), com.google.protobuf.WireFormat.FieldType.STRING),
    INT32("Integer", "int32", "WIRETYPE_VARINT", ReflectionUtils.findMethod(Integer.class, "intValue"), com.google.protobuf.WireFormat.FieldType.INT32, 2, "0",ReflectionUtils.findMethod(CodedOutputStream.class, "computeInt32Size", int.class, int.class), ReflectionUtils.findMethod(CodedOutputStream.class, "computeInt32SizeNoTag", int.class),ReflectionUtils.findMethod(CodedOutputStream.class, "writeInt32", int.class, int.class), ReflectionUtils.findMethod(CodedOutputStream.class, "writeInt32NoTag", int.class), ReflectionUtils.findMethod(CodedInputStream.class, "readInt32"), com.google.protobuf.WireFormat.FieldType.STRING),
    BOOL("Boolean", "bool", "WIRETYPE_VARINT", ReflectionUtils.findMethod(Boolean.class, "booleanValue"), com.google.protobuf.WireFormat.FieldType.BOOL,  2, "false",ReflectionUtils.findMethod(CodedOutputStream.class, "computeStringSize", int.class, boolean.class),ReflectionUtils.findMethod(CodedOutputStream.class, "computeStringSizeNoTag", boolean.class), ReflectionUtils.findMethod(CodedOutputStream.class, "writeDouble", int.class, double.class), ReflectionUtils.findMethod(CodedOutputStream.class, "writeDoubleNoTag", double.class), ReflectionUtils.findMethod(CodedInputStream.class, "readBool"), com.google.protobuf.WireFormat.FieldType.BOOL),
    STRING("String", "string", "WIRETYPE_LENGTH_DELIMITED", null, com.google.protobuf.WireFormat.FieldType.STRING, 0, "", ReflectionUtils.findMethod(CodedOutputStream.class, "computeStringSize", int.class, String.class), ReflectionUtils.findMethod(CodedOutputStream.class, "computeStringSizeNoTag", String.class),ReflectionUtils.findMethod(CodedOutputStream.class, "writeString", int.class, String.class),ReflectionUtils.findMethod(CodedOutputStream.class, "writeStringNoTag", String.class), ReflectionUtils.findMethod(CodedInputStream.class, "readString"), com.google.protobuf.WireFormat.FieldType.STRING),
    OBJECT("Object", "object", "WIRETYPE_LENGTH_DELIMITED", null, com.google.protobuf.WireFormat.FieldType.MESSAGE, 0, (String)null, ReflectionUtils.findMethod(CodedOutputStream.class, "computeStringSize"), ReflectionUtils.findMethod(CodedOutputStream.class, "computeStringSizeNoTag") ,ReflectionUtils.findMethod(CodedOutputStream.class, "readDouble"),  ReflectionUtils.findMethod(CodedInputStream.class, "readInt32"),  ReflectionUtils.findMethod(CodedInputStream.class, "readInt32"), com.google.protobuf.WireFormat.FieldType.MESSAGE),
    ENUM("Enum", "enum", "WIRETYPE_VARINT", ReflectionUtils.findMethod(Enum.class, "ordinal"), com.google.protobuf.WireFormat.FieldType.ENUM, 0,(String)null,  ReflectionUtils.findMethod(CodedOutputStream.class, "computeEnumSize", int.class, int.class), ReflectionUtils.findMethod(CodedOutputStream.class, "computeEnumSizeNoTag", int.class), ReflectionUtils.findMethod(CodedOutputStream.class, "writeEnum", int.class, int.class), ReflectionUtils.findMethod(CodedOutputStream.class, "writeEnumNoTag", int.class), ReflectionUtils.findMethod(CodedInputStream.class, "readEnum"), com.google.protobuf.WireFormat.FieldType.ENUM);

    private final String javaType;
    private final String type;
    private final String wireFormat;
    private Method toPrimitiveTypeMethod;
    private com.google.protobuf.WireFormat.FieldType internalFieldType;
    private com.google.protobuf.WireFormat.FieldType extendFieldType;
    private String defaultValue;
    private Method encodeMethod;
    private Method decodeMethod;
    private Method computeSizeMethod;
    private Method computeSizeNoTagMethod;
    private Method encodeNoTagMethod;
    private int repeatedWireType;

    private boolean checkOtherTag;

    public Method getEncodeMethod() {
        return this.encodeMethod;
    }

    public Method getComputeSizeMethod() {
        return this.computeSizeMethod;
    }

    public Method getComputeSizeNoTagMethod() {
        return this.computeSizeNoTagMethod;
    }

    public Method getEncodeNoTagMethod() {
        return this.encodeNoTagMethod;
    }

    public int getWireType() {
        return this.internalFieldType.getWireType();
    }

    public int getExtendWireType() {
        return this.extendFieldType.getWireType();
    }

    public int getRepeatedWireType() {
        return this.repeatedWireType;
    }

    public com.google.protobuf.WireFormat.FieldType getInternalFieldType() {
        return this.internalFieldType;
    }

    public Method getDecodeMethod() {
        return decodeMethod;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Method getToPrimitiveTypeMethod() {
        return this.toPrimitiveTypeMethod;
    }

    public String getWireFormat() {
        return this.wireFormat;
    }

    public String getType() {
        return this.type;
    }

    public String getJavaType() {
        return this == ENUM ? Enum.class.getName():this.javaType;
    }

    private FieldType(String javaType, String type, String wireFormat, Method toPrimitiveTypeMethod,
                      com.google.protobuf.WireFormat.FieldType internalFieldType, int repeatedWireType,
                      String defaultValue, Method computeSizeMethod, Method computeSizeNoTagMethod, Method encodeMethod, Method encodeNoTagMethod, Method decodeMethod, com.google.protobuf.WireFormat.FieldType extendFieldType) {
        this.javaType = javaType;
        this.type = type;
        this.wireFormat = wireFormat;
        this.toPrimitiveTypeMethod = toPrimitiveTypeMethod;
        this.internalFieldType = internalFieldType;
        this.repeatedWireType = repeatedWireType;
        this.computeSizeMethod = computeSizeMethod;
        this.computeSizeNoTagMethod = computeSizeNoTagMethod;
        this.defaultValue = defaultValue;
        this.encodeMethod = encodeMethod;
        this.encodeNoTagMethod = encodeNoTagMethod;
        this.decodeMethod = decodeMethod;
        this.extendFieldType = extendFieldType;
    }
}
