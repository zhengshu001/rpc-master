package com.hualala.core.rpc;

import org.springframework.data.util.ReflectionUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by xiangbin on 2016/10/26.
 */
public class FieldInfo {
    private final Field field;
    private final int order;
    private final String description;
    private final FieldType fieldType;
    private final boolean isList;
    private Class<?> genericType;
    private boolean primitiveGenericType;

    FieldInfo(Field field, FieldType fieldType, int order, String description) {
        this.field = field;
        this.order = order;
        this.description = description;
        this.fieldType = fieldType;
        if (List.class.isAssignableFrom(field.getType())) {
            isList = true;
            Type type = field.getGenericType();
            if(type instanceof ParameterizedType) {
                ParameterizedType ptype = (ParameterizedType) type;
                Type[] actualTypeArguments = ptype.getActualTypeArguments();
                if (actualTypeArguments == null || actualTypeArguments.length != 1) {
                    throw new RuntimeException("List must use generic definiation like List<String>, please check  field name \'" + field.getName() + " at class " + field.getDeclaringClass().getName());
                }
                Type targetType = actualTypeArguments[0];
                if(targetType instanceof Class) {
                    this.genericType = (Class)targetType;
                    this.primitiveGenericType = ClassUtils.isPrimitiveOrWrapper(this.genericType);
                }
            }
        } else {
            if (fieldType == FieldType.OBJECT || fieldType == FieldType.ENUM) {
                this.genericType = field.getType();
            }
            isList = false;
        }
    }

    public boolean isPrimitiveType() {
        return fieldType == FieldType.INT || fieldType == FieldType.LONG || fieldType == FieldType.FLOAT || fieldType == FieldType.DOUBLE;
    }

    public boolean isPrimitiveGenericType() {return this.primitiveGenericType;}

    public Class<?> getGenericType() {
        return this.genericType;
    }

    public Field getField() {
        return field;
    }

    public String getName() {
        return field.getName();
    }

    public Class getType() {
        return field.getType();
    }

    public FieldType getFieldType() {
        return this.fieldType;
    }

    public int getOrder() {
        return this.order;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isList() {
        return this.isList;
    }

    public boolean hasDescription() {
        return !"".equals(this.description);
    }

    public int makeTag() {
        return this.order << 3 | fieldType.getWireType();
    }

    public int repeatedMakeTag() {
        return (this.order << 3 | fieldType.getRepeatedWireType());
    }

    public static FieldInfo valueOf(Field field, FieldType fieldType, int order, String description) {
        return new FieldInfo(field, fieldType, order, description);
    }

}
