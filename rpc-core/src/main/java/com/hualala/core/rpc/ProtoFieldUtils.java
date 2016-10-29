package com.hualala.core.rpc;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by xiangbin on 2016/10/26.
 */
public class ProtoFieldUtils {

    public static List<FieldInfo> getAllProtoField(Class<?> clazz) {
        List<FieldInfo> fieldInfoList = Arrays.asList(clazz.getDeclaredFields()).stream().filter(field ->
            field.getAnnotation(Protocol.class) != null
        ).map(field -> {
            Protocol proto = field.getAnnotation(Protocol.class);
            return FieldInfo.valueOf(field, proto.fieldType(), proto.order(), proto.description());
        }).collect(Collectors.toList());

        if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
            fieldInfoList.addAll(getAllProtoField(clazz.getSuperclass()));
        }
        //fieldInfoList.sort((fieldInfo1, fieldInfo2) -> fieldInfo1.getOrder() - fieldInfo2.getOrder());
        return fieldInfoList;
    }

    public static List<FieldInfo> getProtoField(Class<?> clazz) {
        List<FieldInfo> fieldInfoList = Arrays.asList(clazz.getDeclaredFields()).stream().filter(field ->
                field.getAnnotation(Protocol.class) != null
        ).map(field -> {
            Protocol proto = field.getAnnotation(Protocol.class);
            return FieldInfo.valueOf(field, proto.fieldType(), proto.order(), proto.description());
        }).collect(Collectors.toList());
        //fieldInfoList.sort((fieldInfo1, fieldInfo2) -> fieldInfo1.getOrder() - fieldInfo2.getOrder());
        return fieldInfoList;
    }
}
