package com.hualala.core;

import com.alibaba.fastjson.JSON;
import com.baidu.bjf.remoting.protobuf.Codec;
import com.google.protobuf.*;
import com.hualala.app.example.Example;
import com.hualala.app.example.grpc.Common;
import com.hualala.app.example.grpc.ExampleData;
import com.hualala.core.grpc.GrpcUtils;
import com.hualala.core.rpc.FieldInfo;
import com.hualala.core.rpc.FieldType;
import com.hualala.core.rpc.ProtoFieldUtils;
import com.sun.org.apache.bcel.internal.classfile.Code;
import com.sun.org.apache.bcel.internal.classfile.InnerClass;
import lombok.Data;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.Enum;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by xiangbin on 2016/10/26.
 */
public class GrpcUtilsTest {

    @Test
    public void testRpcMessageToBean() throws Exception {
        ExampleData.HelloReqData.Builder hualalaReqData = ExampleData.HelloReqData.newBuilder();
        hualalaReqData.setParamStr("bbbbbbb");
        hualalaReqData.setParamLong(1234567);
        hualalaReqData.setParamInt(123);
        ExampleData.HelloReqData reqData = hualalaReqData.build();
//        byte[] bytes = reqData.toByteArray();
//        CodedInputStream input = CodedInputStream.newInstance(bytes, 0, bytes.length);
//        Example.HelloReqData helloReqData = grpcToBean(input, Example.HelloReqData.class);
        Example.HelloReqData helloReqData = GrpcUtils.grpcMessageToDataBean(reqData, Example.HelloReqData.class);
        System.out.println(JSON.toJSONString(helloReqData));
    }

    @Test
    public void testRpcMessageToComplexBean() throws Exception {
        ExampleData.ComplexReqData.Builder complexReqData = ExampleData.ComplexReqData.newBuilder();
        complexReqData.setReq("bbbbbbb");
        complexReqData.setResInt(1234567);
        complexReqData.setResDouble(12312312d);
        complexReqData.setResFloat(12.11f);
        complexReqData.setResDouble(10000.1234d);
        complexReqData.setBankCodeEnum(ExampleData.BankCodeEnum.ALIPAY_QRCODE);
        complexReqData.addDetails(ExampleData.DetailData.newBuilder().setName("name1").setItemID("itemID1"));
        complexReqData.addDetails(ExampleData.DetailData.newBuilder().setName("name2").setItemID("itemID2"));
        complexReqData.addDetails(ExampleData.DetailData.newBuilder().setName("name3").setItemID("itemID3"));
        complexReqData.setReqDetail(ExampleData.DetailData.newBuilder().setName("name").setItemID("itemID"));
        complexReqData.addListString("str1").addListString("str2").addListString("str3");
        complexReqData.addListInt(11).addListInt(22).addListInt(33);
        complexReqData.addListDouble(1000.11d).addListDouble(1000.22d).addListDouble(1000.33d).addListDouble(1000.44);
        complexReqData.addListFloat(11.11f).addListFloat(22.22f).addListFloat(33.33f).addListFloat(44.44f);
        complexReqData.addListLong(11111111).addListLong(2222222).addListLong(333333);
        complexReqData.setHeader(Common.RequestHeader.newBuilder().setTraceID("hhhhhh"));
        ExampleData.ComplexReqData reqData = complexReqData.build();
//        byte[] bytes = reqData.toByteArray();
//        ExampleData.ComplexReqData.parseFrom(bytes);
//        CodedInputStream input = CodedInputStream.newInstance(bytes, 0, bytes.length);
//        byte[] bytes = reqData.toByteArray();
//        CodedInputStream input = CodedInputStream.newInstance(bytes, 0, bytes.length);
//        Example.ComplexReqData data = grpcToBean(input, Example.ComplexReqData.class); //GrpcUtils.decode(reqData, Example.ComplexReqData.class);
        Example.ComplexReqData data = GrpcUtils.grpcMessageToDataBean(reqData, Example.ComplexReqData.class);
        System.out.println(JSON.toJSONString(data));
        Assert.assertEquals(1000.33d, data.getListDouble().get(2).doubleValue(), 0.001);
        Assert.assertEquals(22.22f, data.getListFloat().get(1), 0.001);
        Assert.assertEquals(11111111, data.getListLong().get(0).longValue(), 0.01);
        Assert.assertEquals(1234567, data.getResInt(), 0.1);
        Assert.assertEquals("name3", data.getDetails().get(2).getName());
        Assert.assertEquals("itemID", data.getReqDetail().getItemID());
        Assert.assertEquals("str2", data.getListString().get(1));
        Assert.assertEquals("hhhhhh", data.getTraceID());
    }
    @Test
    public void test() {
        System.out.print(13 << 3 | 1);
    }

    @Test
    public void SimpleBeanToRpc() throws Exception {
        Example.HelloReqData helloReqData = new Example.HelloReqData();
        helloReqData.setParamInt(123);
        helloReqData.setParamStr("bbbbbb");
        helloReqData.setParamLong(1234567);
        System.out.println(JSON.toJSONString(helloReqData));
        List<FieldInfo> fieldInfoList = ProtoFieldUtils.getAllProtoField(helloReqData.getClass());
//        Predicate<FieldInfo> drinkingAge = (it) -> {
//            ReflectionUtils.makeAccessible(it.getField());
//            return ReflectionUtils.getField(it.getField(), helloReqData) != null;
//        };
       // int size = computeByteSize(fieldInfoList, helloReqData);
//        byte[] bytes = encode(helloReqData, Example.HelloReqData.class);
//        CodedOutputStream output = CodedOutputStream.newInstance(bytes);
//        fieldInfoList.stream().forEach(fieldInfo -> {
//            ReflectionUtils.makeAccessible(fieldInfo.getField());
//            Object value = ReflectionUtils.getField(fieldInfo.getField(), helloReqData);
//            if (value != null) {
//                Method encodeMethod = fieldInfo.getFieldType().getEncodeMethod();
//                ReflectionUtils.invokeMethod(encodeMethod, output, new Object[]{fieldInfo.getOrder(), value});
//            }
//        });
//        ExampleData.HelloReqData rpcReqData = ExampleData.HelloReqData.parseFrom(bytes);
        ExampleData.HelloReqData rpcReqData = GrpcUtils.dataBeanToGrpcMessage(helloReqData, ExampleData.HelloReqData.class);
        System.out.println(rpcReqData);
        Assert.assertEquals(123, rpcReqData.getParamInt());
        Assert.assertEquals("bbbbbb", rpcReqData.getParamStr());
        Assert.assertEquals(1234567, rpcReqData.getParamLong());
    }

    @Test
    public void complexBeanToRpc() throws Exception {
        Example.ComplexReqData helloResData = new Example.ComplexReqData();
        helloResData.setReq("bbbbbbb");
        helloResData.setResInt(1234567);
        helloResData.setResDouble(12312312d);
        helloResData.setResFloat(12.11f);
        helloResData.setResDouble(10000.1234d);
        helloResData.setBankCodeEnum(Example.BankCodeEnum.ALIPAY_QRCODE);
        List<Example.DetailData> detailDatas = new ArrayList<>();
        Example.DetailData d1 = new Example.DetailData();
        d1.setItemID("itemID1");
        d1.setName("name1");
        detailDatas.add(d1);

        Example.DetailData d3 = new Example.DetailData();
        d3.setItemID("itemID3");
        d3.setName("name3");
        detailDatas.add(d3);
        Example.DetailData d2 = new Example.DetailData();
        d2.setItemID("itemID2");
        d2.setName("name2");
        detailDatas.add(d2);



        helloResData.setDetails(detailDatas);

        Example.DetailData d = new Example.DetailData();
        d.setItemID("itemID");
        d.setName("name");

        helloResData.setReqDetail(d);
        List<String> listString = new ArrayList<>();
        listString.add("str1");
        listString.add("str2");
        listString.add("str3");
        helloResData.setListString(listString);
//
        List<Integer> listInt = new ArrayList<>();
        listInt.add(11);
        listInt.add(22);
        listInt.add(33);
        helloResData.setListInt(listInt);
////
        List<Double> listDouble = new ArrayList<>();
        listDouble.add(1000.11d);
        listDouble.add(1000.22d);
        listDouble.add(1000.33d);
        listDouble.add(1000.44d);
        helloResData.setListDouble(listDouble);

        List<Float> listFloat = new ArrayList<>();
        listFloat.add(10.11f);
        listFloat.add(10.22f);
        helloResData.setListFloat(listFloat);

        List<Long> listLong = new ArrayList<>();
        listLong.add(10000000001l);
        listLong.add(10000000002l);
        listLong.add(10000000003l);
        listLong.add(10000000004l);
        listLong.add(10000000005l);
        helloResData.setListLong(listLong);

//        helloResData.setHeader(Common.RequestHeader.newBuilder().setTraceID("hhhhhh"));
//        ExampleData.ComplexReqData reqData = complexReqData.build();
        System.out.println(JSON.toJSONString(helloResData));
        byte[] bytes = encode(helloResData, Example.ComplexReqData.class);

        ExampleData.ComplexReqData rpcReqData = GrpcUtils.dataBeanToGrpcMessage(helloResData, ExampleData.ComplexReqData.class);

        //ExampleData.ComplexReqData rpcReqData = ExampleData.ComplexReqData.parseFrom(bytes);
        System.out.println(rpcReqData);

    }

    @Data
    private class EncodeData {
        private final FieldInfo fieldInfo;
        private Object value;
        private Object extra;
        private int dataSize;
        public EncodeData(FieldInfo fieldInfo) {
            this.fieldInfo = fieldInfo;
        }
    }


    private byte[] encode(Object object, Class<?> clazz) {
        List<FieldInfo> fieldInfoList = ProtoFieldUtils.getProtoField(clazz);
        List<EncodeData> encodeDataList = new ArrayList<>();
        int size = computeSerializedSize(object, fieldInfoList, encodeDataList);
        byte[] bytes = new byte[size];
        CodedOutputStream output = CodedOutputStream.newInstance(bytes);
        encodeOutput(encodeDataList, output);
        return bytes;
    }

    private int computeSerializedSize(Object object, List<FieldInfo> fieldInfoList, List<EncodeData> encodeDataList) {
        return fieldInfoList.stream().mapToInt(fieldInfo -> {
            ReflectionUtils.makeAccessible(fieldInfo.getField());
            Object value = ReflectionUtils.getField(fieldInfo.getField(), object);
            int size = 0;
            if (value != null) {
                EncodeData encodeData = new EncodeData(fieldInfo);
                encodeData.setValue(value);
                if (fieldInfo.isList()) {
                    size = computeListFieldSize(fieldInfo, (List)value, encodeData);
                } else {
                    size = computeFieldSize(fieldInfo, value, encodeData);
                }
                encodeDataList.add(encodeData);
            }
            return size;
        }).sum();
    }

    private int computeListFieldSize(FieldInfo fieldInfo, List<Object> valueList, EncodeData encodeData) {
        int size = 0;
        if (fieldInfo.isPrimitiveType()) {
            int dataSize =  valueList.stream().mapToInt(o -> computePrimitiveSize(fieldInfo, o)).sum();
            size = CodedOutputStream.computeTagSize(fieldInfo.getOrder()) +  CodedOutputStream.computeInt32SizeNoTag(dataSize) + dataSize;
            encodeData.setValue(valueList);
            encodeData.setDataSize(dataSize);
        } else if (fieldInfo.getFieldType() == FieldType.OBJECT) {
            List<List<EncodeData>> encodeList = new ArrayList<>();
            List<Integer> extraList = new ArrayList<>();
            size = valueList.stream().mapToInt(o -> {
                List<FieldInfo> subFieldList = ProtoFieldUtils.getProtoField(fieldInfo.getGenericType());
                List<EncodeData> subEncodeDataList = new ArrayList<>();
                int dataSize = computeSerializedSize(o, subFieldList, subEncodeDataList);
                encodeList.add(subEncodeDataList);
                extraList.add(dataSize);
                return CodedOutputStream.computeTagSize(fieldInfo.getOrder()) +  CodedOutputStream.computeInt32SizeNoTag(dataSize) + dataSize;
            }).sum();
            encodeData.setValue(encodeList);
            encodeData.setExtra(extraList);
        } else if (fieldInfo.getFieldType() == FieldType.STRING) {
            size = valueList.stream().mapToInt(o -> CodedOutputStream.computeStringSize(fieldInfo.getOrder(), (String)o)).sum();
        }
        return size;
    }


    private int computeFieldSize(FieldInfo fieldInfo, Object value, EncodeData encodeData) {
        int size = 0;
        if (fieldInfo.isPrimitiveType()) {
            size = CodedOutputStream.computeTagSize(fieldInfo.getOrder()) +  computePrimitiveSize(fieldInfo, value);
        } else if (fieldInfo.getFieldType() == FieldType.ENUM){
            size = CodedOutputStream.computeEnumSize(fieldInfo.getOrder(), ((Enum) value).ordinal());
        } else if (fieldInfo.getFieldType() == FieldType.OBJECT) {
            List<FieldInfo> subFieldList = ProtoFieldUtils.getProtoField(fieldInfo.getField().getType());
            List<EncodeData> subEncodeDataList = new ArrayList<>();
            int dataSize = computeSerializedSize(value, subFieldList, subEncodeDataList);
            size = CodedOutputStream.computeTagSize(fieldInfo.getOrder()) + CodedOutputStream.computeUInt32SizeNoTag(dataSize) + dataSize;
            encodeData.setValue(subEncodeDataList);
            encodeData.setDataSize(dataSize);
        } else if (fieldInfo.getFieldType() == FieldType.STRING){
            size =  CodedOutputStream.computeStringSize(fieldInfo.getOrder(), (String)value);
        }
        return size;
    }

    private void writeData(FieldInfo fieldInfo, Object value, CodedOutputStream output) {
        try {
            List<EncodeData> encodeDataList = (List)value;
            encodeOutput(encodeDataList, output);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void writeString(FieldInfo fieldInfo, Object value, CodedOutputStream output) {
        try {
            if (fieldInfo.getFieldType() == FieldType.STRING) {
                output.writeString(fieldInfo.getOrder(), (String)value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writePrimitiveNoTag(FieldInfo fieldInfo, Object o, CodedOutputStream output) {
        try {
            if (fieldInfo.getFieldType() == FieldType.INT) {
                output.writeInt32NoTag((int)o);
            } else if (fieldInfo.getFieldType() == FieldType.LONG) {
                output.writeInt64NoTag((long)o);
            } else if (fieldInfo.getFieldType() == FieldType.FLOAT) {
                output.writeFloatNoTag((float)o);
            } else if (fieldInfo.getFieldType() == FieldType.DOUBLE) {
                output.writeDoubleNoTag((double)o);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writePrimitive(FieldInfo fieldInfo, Object o, CodedOutputStream output) {
        try {
            if (fieldInfo.getFieldType() == FieldType.INT) {
                output.writeInt32(fieldInfo.getOrder(), (int) o);
            } else if (fieldInfo.getFieldType() == FieldType.LONG) {
                output.writeInt64(fieldInfo.getOrder(), (long) o);
            } else if (fieldInfo.getFieldType() == FieldType.FLOAT) {
                output.writeFloat(fieldInfo.getOrder(), (float) o);
            } else if (fieldInfo.getFieldType() == FieldType.DOUBLE) {
                output.writeDouble(fieldInfo.getOrder(), (double) o);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private int computePrimitiveSize(FieldInfo fieldInfo, Object o) {
        if (fieldInfo.getFieldType() == FieldType.INT) {
            return CodedOutputStream.computeInt32SizeNoTag((int) o);
        } else if (fieldInfo.getFieldType() == FieldType.LONG) {
            return CodedOutputStream.computeInt64SizeNoTag((long) o);
        } else if (fieldInfo.getFieldType() == FieldType.FLOAT) {
            return CodedOutputStream.computeFloatSizeNoTag((float)o);
        } else if (fieldInfo.getFieldType() == FieldType.DOUBLE) {
            return CodedOutputStream.computeDoubleSizeNoTag((double)o);
        } else {
            return 0;
        }
    }

    private void encodeOutput(List<EncodeData> encodeDataList, CodedOutputStream output) {
        encodeDataList.stream().forEach(encodeData -> {
            try {
                FieldInfo fieldInfo = encodeData.fieldInfo;
                Object value = encodeData.getValue();
                if (fieldInfo.isList()) {
                    writeListFieldData(fieldInfo, encodeData, (List)encodeData.getValue(), output);
                } else {
                    writeFieldData(fieldInfo, encodeData, output);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void writeListFieldData(FieldInfo fieldInfo, EncodeData encodeData, List valueList, CodedOutputStream output) {
        try {
            if (fieldInfo.isPrimitiveGenericType()) {
                output.writeRawVarint32(fieldInfo.repeatedMakeTag());
                output.writeRawVarint32(encodeData.dataSize);
                valueList.stream().forEach(o -> writePrimitiveNoTag(fieldInfo, o, output));
            } else if (fieldInfo.getFieldType() == FieldType.STRING) {
                valueList.stream().forEach(o -> writeString(fieldInfo, o, output));
            } else if (fieldInfo.getFieldType() == FieldType.OBJECT) {
                List<List<EncodeData>> dataList = (List<List<EncodeData>>)valueList;
                List<Integer> dataSizeList = (List<Integer>)encodeData.getExtra();
                for (int i = 0; i < dataList.size(); i++) {
                    try {
                        output.writeRawVarint32(fieldInfo.makeTag());
                        output.writeRawVarint32(dataSizeList.get(i));
                        writeData(fieldInfo, dataList.get(i), output);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } else if (fieldInfo.getFieldType() == FieldType.ENUM) {
                // output.writeEnum(fieldInfo.getOrder(), ((Enum)value).ordinal());
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void writeFieldData(FieldInfo fieldInfo, EncodeData encodeData, CodedOutputStream output) {
        try {
            if (fieldInfo.isPrimitiveType()) {
                writePrimitive(fieldInfo, encodeData.getValue(), output);
            } else if (fieldInfo.getFieldType() == FieldType.ENUM) {
                output.writeEnum(fieldInfo.getOrder(), ((Enum)encodeData.getValue()).ordinal());
            } else if (fieldInfo.getFieldType() == FieldType.OBJECT) {
                output.writeTag(fieldInfo.getOrder(), 2);
                output.writeUInt32NoTag(encodeData.dataSize);
                encodeOutput((List<EncodeData>) encodeData.getValue(), output);
            } else if (fieldInfo.getFieldType() == FieldType.STRING){
                output.writeString(fieldInfo.getOrder(), (String)encodeData.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void encode(EncodeData encodeData, CodedOutputStream output) throws IOException {
        FieldInfo fieldInfo = encodeData.fieldInfo;
        Object value = encodeData.getValue();
        if (fieldInfo.isList()) {
            if (fieldInfo.isPrimitiveGenericType()) {
                Method encodeMethod = fieldInfo.getFieldType().getEncodeNoTagMethod();
                output.writeRawVarint32(fieldInfo.repeatedMakeTag());
                output.writeRawVarint32(encodeData.dataSize);
                ((List)value).stream().forEach(o ->
                        ReflectionUtils.invokeMethod(encodeMethod, output, new Object[]{o}));
            } else {
                Method encodeMethod = fieldInfo.getFieldType().getEncodeMethod();
                ((List)value).stream().forEach(o ->
                        ReflectionUtils.invokeMethod(encodeMethod, output, new Object[]{fieldInfo.getOrder(), o}));
            }
        } else {
            Method encodeMethod = fieldInfo.getFieldType().getEncodeMethod();
            if (fieldInfo.getFieldType() == FieldType.ENUM) {
                ReflectionUtils.invokeMethod(encodeMethod, output, new Object[]{fieldInfo.getOrder(), ((Enum)value).ordinal()});
            } else if (fieldInfo.getFieldType() == FieldType.OBJECT) {
                output.writeTag(fieldInfo.getOrder(), 2);
                output.writeUInt32NoTag(encodeData.dataSize);
                encodeOutput((List<EncodeData>) encodeData.getValue(), output);
            } else {
                ReflectionUtils.invokeMethod(encodeMethod, output, new Object[]{fieldInfo.getOrder(), value});
            }
        }
    }

    private int computeByteSize(List<FieldInfo> fieldInfoList, Object object, List<EncodeData> dataList) {
        return fieldInfoList.stream().mapToInt(fieldInfo -> {
            ReflectionUtils.makeAccessible(fieldInfo.getField());
            Object value = ReflectionUtils.getField(fieldInfo.getField(), object);
            int size = 0;
            if (value != null) {
                EncodeData encodeData = new EncodeData(fieldInfo);
                dataList.add(encodeData);
                if (fieldInfo.isList()) {
                    if (fieldInfo.isPrimitiveGenericType()) {
                        int dataSize =  ((List)value).stream().mapToInt(o -> {
                            Method computeSizeMethod = fieldInfo.getFieldType().getComputeSizeNoTagMethod();
                            return (int) ReflectionUtils.invokeMethod(computeSizeMethod, null, new Object[]{o});
                        }).sum();
                        encodeData.setDataSize(dataSize);
                        size += dataSize;
                        size += 1 +  CodedOutputStream.computeInt32SizeNoTag(dataSize);
                    } else if (fieldInfo.getFieldType() == FieldType.OBJECT) {
                        size = ((List)value).stream().mapToInt(o -> {
                            List<FieldInfo> subFieldList = ProtoFieldUtils.getProtoField(fieldInfo.getField().getType());
                            List<EncodeData> subEncodeData = new ArrayList<>();
                            return computeByteSize(subFieldList, o, subEncodeData);
                        }).sum();
                    } else {
                        size = ((List)value).stream().mapToInt(o -> {
                            Method computeSizeMethod = fieldInfo.getFieldType().getComputeSizeMethod();
                            return (int) ReflectionUtils.invokeMethod(computeSizeMethod, null, new Object[]{fieldInfo.getOrder(), o});
                            }).sum();
                    }
                } else {
                    Method computeSizeMethod = fieldInfo.getFieldType().getComputeSizeMethod();
                    if (fieldInfo.getFieldType() == FieldType.ENUM) {
                        size = (int) ReflectionUtils.invokeMethod(computeSizeMethod, null, new Object[]{fieldInfo.getOrder(), ((Enum) value).ordinal()});
                    } else if (fieldInfo.getFieldType() == FieldType.OBJECT) {
                        List<FieldInfo> subFieldList = ProtoFieldUtils.getProtoField(fieldInfo.getField().getType());
                        List<EncodeData> subEncodeData = new ArrayList<>();
                        int dataSize = computeByteSize(subFieldList, value, subEncodeData);
                        encodeData.setDataSize(dataSize);
                        size = CodedOutputStream.computeTagSize(fieldInfo.getOrder()) + CodedOutputStream.computeUInt32SizeNoTag(dataSize) + dataSize;
                        encodeData.setValue(subEncodeData);
                    } else {
                        size = (int) ReflectionUtils.invokeMethod(computeSizeMethod, null, new Object[]{fieldInfo.getOrder(), value});
                    }

                }
            }
            return size;
        }).sum();
    }


    public <T> T grpcToBean(CodedInputStream input, Class<T> clazz) throws Exception  {
        T object = BeanUtils.instantiate(clazz);
        List<FieldInfo> fieldInfoList = ProtoFieldUtils.getAllProtoField(clazz);
        boolean done = false;
        Codec codec = null;
        while (!done) {
            int tag = input.readTag();
            if (tag == 0) { break;}
            Optional<FieldInfo> optional = fieldInfoList.stream().filter(fieldInfo ->
                tag == fieldInfo.makeTag() || (fieldInfo.isList() && tag == fieldInfo.repeatedMakeTag())
            ).findFirst();
            if (optional.isPresent()) {
                FieldInfo fieldInfo = optional.get();
                if (fieldInfo.isList()) {
                    ReflectionUtils.makeAccessible(fieldInfo.getField());
                    List listObject = (List)ReflectionUtils.getField(fieldInfo.getField(), object);
                    if (listObject == null) {
                        listObject = new ArrayList();
                        ReflectionUtils.setField(fieldInfo.getField(), object, listObject);
                    }
                    if (fieldInfo.getFieldType() == FieldType.OBJECT) {
                        int length = input.readRawVarint32();
                        final int oldLimit = input.pushLimit(length);
                        Object value = grpcToBean(input, fieldInfo.getGenericType());
                        listObject.add(value);
                        input.checkLastTagWas(0);
                        input.popLimit(oldLimit);
                    } else {
                        if (ClassUtils.isPrimitiveWrapper(fieldInfo.getGenericType())) {
                            int length = input.readRawVarint32();
                            int limit = input.pushLimit(length);
                            while (input.getBytesUntilLimit() > 0) {
                                listObject.add(ReflectionUtils.invokeMethod(fieldInfo.getFieldType().getDecodeMethod(), input));
                            }
                            input.popLimit(limit);
                        } else {
                            listObject.add(ReflectionUtils.invokeMethod(fieldInfo.getFieldType().getDecodeMethod(), input));
                        }
                    }
                } else if (fieldInfo.getFieldType() == FieldType.OBJECT) {
                    int length = input.readRawVarint32();
                    final int oldLimit = input.pushLimit(length);
                    Object value = grpcToBean(input, fieldInfo.getGenericType());
                    ReflectionUtils.makeAccessible(fieldInfo.getField());
                    ReflectionUtils.setField(fieldInfo.getField(), object, value);
                    input.checkLastTagWas(0);
                    input.popLimit(oldLimit);
                } else if (fieldInfo.getFieldType() == FieldType.ENUM) {
                    Enum[] enums = ((Class<Enum>)fieldInfo.getField().getType()).getEnumConstants();
                    Object value = enums[input.readEnum()];
                    ReflectionUtils.makeAccessible(fieldInfo.getField());
                    ReflectionUtils.setField(fieldInfo.getField(), object, value);
                } else {
                    Object value = ReflectionUtils.invokeMethod(fieldInfo.getFieldType().getDecodeMethod(), input);
                    ReflectionUtils.makeAccessible(fieldInfo.getField());
                    ReflectionUtils.setField(fieldInfo.getField(), object, value);
                }
                continue;
            }
            input.skipField(tag);
        }
        return object;
    }

    public static int makeTag(final int fieldNumber, final WireFormat.FieldType fieldType) {
        return (fieldNumber << 3) | fieldType.getWireType();
    }
}
