package com.hualala.core.grpc;

import com.baidu.bjf.remoting.protobuf.EnumReadable;
import com.baidu.bjf.remoting.protobuf.FieldType;
import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import com.baidu.bjf.remoting.protobuf.utils.FieldInfo;
import com.baidu.bjf.remoting.protobuf.utils.FieldUtils;
import com.baidu.bjf.remoting.protobuf.utils.ProtobufProxyUtils;
import com.hualala.core.base.RequestInfo;
import com.hualala.core.base.ResultInfo;
import org.springframework.util.ReflectionUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by xiangbin on 2016/10/19.
 */
public class ProtoIDLGenerator {

    public static void getIDL(Class<?> rpcInterface, String protoName) {
        getIDL(rpcInterface, protoName, false);
    }

    public static void getIDL(Class rpcInterface, String protoName,boolean ignoreCommon) {
        StringBuilder code = new StringBuilder();
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(rpcInterface);
        if (methods.length == 0) {
            return;
        }
        code.append("syntax = \"proto3\"").append(";\n").append("\n");
        code.append("option java_package = \"").append(rpcInterface.getPackage().getName()).append(".grpc").append("\";\n");
        String dataName = rpcInterface.getSimpleName();
        if (dataName.endsWith("Service")) {
            dataName = dataName.substring(0, dataName.indexOf("Service"));
        }
        code.append("option java_outer_classname = \"").append(dataName).append("Data").append("\";\n");
        code.append("import \"commons.proto\"").append(";\n").append("\n");
        Set<Class<?>> cachedTypes = new HashSet<>();
        Set<Class<?>> cachedEnumTypes = new HashSet<>();
        StringBuilder service = new StringBuilder();
        service.append("service ").append(rpcInterface.getSimpleName()).append("{").append("\n");
        Arrays.asList(methods).stream().forEach(rpcMethod -> {
            Class<?> requestDataClass = rpcMethod.getParameterTypes()[0];
            String requestMessage = getIDL(requestDataClass, cachedTypes, cachedEnumTypes);
            code.append(requestMessage);
            Class<?> responseDataClass = rpcMethod.getReturnType();
            String responseMessage = getIDL(responseDataClass, cachedTypes, cachedEnumTypes);
            code.append(responseMessage);
            service.append("    ").append("rpc ").append(rpcMethod.getName())
                    .append("(").append(requestDataClass.getSimpleName()).append(") returns ")
                    .append("(").append(responseDataClass.getSimpleName()).append(") {}").append("\n");
        });
        service.append("}").append("\n");
        code.append(service.toString());
        String codeStr = code.toString();
        System.out.println(codeStr);
        outputProto(codeStr, protoName);

        //生成common.proto文件
        if (!ignoreCommon) {
            StringBuilder comm = new StringBuilder();
            comm.append("syntax = \"proto3\"").append(";\n").append("\n");
            comm.append("option java_package = \"").append(rpcInterface.getPackage().getName()).append(".grpc").append("\";\n");
            comm.append("option java_outer_classname = \"").append("Common").append("\";\n");

            comm.append("message RequestHeader {").append("\n");
            comm.append("   ").append("string traceID=1;").append("\n");
            comm.append("}\n");

            comm.append("message ResultHeader {").append("\n");
            comm.append("   ").append("string traceID=1;").append("\n");
            comm.append("   ").append("string code=2;").append("\n");
            comm.append("   ").append("string message=3;").append("\n");
            comm.append("   ").append("bool success=4;").append("\n");
            comm.append("}\n");

            String commStr = comm.toString();
            System.out.println();
            System.out.println();
            System.out.println(commStr);
            outputProto(commStr, "commons");
        }
    }

    private static void outputProto(String protoStr, String protoName) {
        String protoFile = "src/main/proto";

        Path protoPath = Paths.get(protoFile, protoName + ".proto");
        try {
            if (Files.notExists(protoPath)) {
                Files.createDirectories(protoPath);
            }
            Files.deleteIfExists(protoPath);
            try (BufferedWriter bw = Files.newBufferedWriter(protoPath)) {
                bw.write(protoStr, 0, protoStr.length());
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * get IDL content from class.
     * @param cls target class to parse for IDL message.
     * @param cachedTypes if type already in set will not generate IDL. if a new type found will add to set
     * @param cachedEnumTypes if enum already in set will not generate IDL. if a new enum found will add to set
     * @return protobuf IDL content in string
     * @see Protobuf
     */
    private static String getIDL(final Class<?> cls, final Set<Class<?>> cachedTypes,
                                final Set<Class<?>> cachedEnumTypes) {
        Set<Class<?>> types = cachedTypes;
        if (types == null) {
            types = new HashSet<Class<?>>();
        }

        Set<Class<?>> enumTypes = cachedEnumTypes;
        if (enumTypes == null) {
            enumTypes = new HashSet<Class<?>>();
        }

        if (types.contains(cls)) {
            return null;
        }

        StringBuilder code = new StringBuilder();

//        if (!ignoreJava) {
//            // define package
//            code.append("package ").append(cls.getPackage().getName()).append(";\n");
//            code.append("option java_outer_classname = \"").append(cls.getSimpleName())
//                    .append(JPROTOBUF_CLASS_NAME_SUFFIX + "\";\n");
//        }

        // define outer name class

        types.add(cls);

        generateIDL(code, cls, types, enumTypes);

        return code.toString();
    }

    /**
     * get IDL content from class.
     *
     * @param cls target class to parse for IDL message.
     * @param cachedTypes if type already in set will not generate IDL. if a new type found will add to set
     * @param cachedEnumTypes if enum already in set will not generate IDL. if a new enum found will add to set
     * @return protobuf IDL content in string
     * @see Protobuf
     */
//    private static String getIDL(final Class<?> cls, final Set<Class<?>> cachedTypes,
//                                final Set<Class<?>> cachedEnumTypes) {
//
//        return getIDL(cls, cachedTypes, cachedEnumTypes, false);
//
//    }

    /**
     * get IDL content from class.
     *
     * @param cls target protobuf class to parse
     * @return protobuf IDL content in string
     */
//    private static String getIDL(final Class<?> cls) {
//        return getIDL(cls, null, null);
//    }

    /**
     * Generate idl.
     *
     * @param code the code
     * @param cls the cls
     * @param cachedTypes the cached types
     * @param cachedEnumTypes the cached enum types
     * @return sub message class list
     */
    private static void generateIDL(StringBuilder code, Class<?> cls, Set<Class<?>> cachedTypes,
                                    Set<Class<?>> cachedEnumTypes) {
        if (cls.getName().equals(ResultInfo.ResultHeader.class.getName())
                || cls.getName().equals(RequestInfo.RequestHeader.class.getName())) {
            return;
        }
        List<Field> fields = FieldUtils.findMatchedFields(cls, Protobuf.class);

        Set<Class<?>> subTypes = new HashSet<Class<?>>();
        Set<Class<Enum>> enumTypes = new HashSet<Class<Enum>>();
        code.append("message ").append(cls.getSimpleName()).append(" {  \n");

        List<FieldInfo> fieldInfos = ProtobufProxyUtils.processDefaultValue(fields);
        for (FieldInfo field : fieldInfos) {
            if (field.hasDescription()) {
                code.append("   ").append("// ").append(field.getDescription()).append("\n");
            }
            if (field.getFieldType() == FieldType.OBJECT || field.getFieldType() == FieldType.ENUM) {
                if (field.isList()) {
                    Type type = field.getField().getGenericType();
                    if (type instanceof ParameterizedType) {
                        ParameterizedType ptype = (ParameterizedType) type;

                        Type[] actualTypeArguments = ptype.getActualTypeArguments();
                        if (actualTypeArguments != null && actualTypeArguments.length > 0) {
                            Type targetType = actualTypeArguments[0];
                            if (targetType instanceof Class) {
                                Class c = (Class) targetType;

                                String fieldTypeName;
                                if (ProtobufProxyUtils.isScalarType(c)) {

                                    FieldType fieldType = ProtobufProxyUtils.TYPE_MAPPING.get(c);
                                    fieldTypeName = fieldType.getType();

                                } else {
                                    if (field.getFieldType() == FieldType.ENUM) {
                                        if (!cachedEnumTypes.contains(c)) {
                                            cachedEnumTypes.add(c);
                                            enumTypes.add(c);
                                        }
                                    } else {

                                        if (!cachedTypes.contains(c)) {
                                            cachedTypes.add(c);
                                            subTypes.add(c);
                                        }
                                    }

                                    fieldTypeName = c.getSimpleName();
                                }

                                code.append("   ").append("repeated ").append(fieldTypeName).append(" ")
                                        .append(field.getField().getName()).append("=").append(field.getOrder())
                                        .append(";\n");
                            }
                        }
                    }
                } else {
                    Class c = field.getField().getType();
                    code.append("   ").append(c.getSimpleName()).append(" ")
                            .append(field.getField().getName()).append("=").append(field.getOrder()).append(";\n");
//                    code.append(getFieldRequired(field.isRequired())).append(" ").append(c.getSimpleName()).append(" ")
//                            .append(field.getField().getName()).append("=").append(field.getOrder()).append(";\n");
                    if (field.getFieldType() == FieldType.ENUM) {
                        if (!cachedEnumTypes.contains(c)) {
                            cachedEnumTypes.add(c);
                            enumTypes.add(c);
                        }
                    } else {

                        if (!cachedTypes.contains(c)) {
                            cachedTypes.add(c);
                            subTypes.add(c);
                        }
                    }
                }
            } else {
                String type = field.getFieldType().getType().toLowerCase();

                if (field.getFieldType() == FieldType.ENUM) {
                    // if enum type
                    Class c = field.getField().getType();
                    if (Enum.class.isAssignableFrom(c)) {
                        type = c.getSimpleName();
                        if (!cachedEnumTypes.contains(c)) {
                            cachedEnumTypes.add(c);
                            enumTypes.add(c);
                        }
                    }
                }

                //String required = getFieldRequired(field.isRequired());
                code.append("   ");
                if (field.isList()) {
                    //required = "repeated";
                    code.append("repeated").append(" ");
                }
                code.append(type).append(" ").append(field.getField().getName())
                        .append("=").append(field.getOrder()).append(";\n");
//                code.append(required).append(" ").append(type).append(" ").append(field.getField().getName())
//                        .append("=").append(field.getOrder()).append(";\n");
            }

        }

        code.append("}\n");

        for (Class<Enum> subType : enumTypes) {
            generateEnumIDL(code, subType);
        }

        if (subTypes.isEmpty()) {
            return;
        }

        for (Class<?> subType : subTypes) {
            generateIDL(code, subType, cachedTypes, cachedEnumTypes);
        }

    }

    /**
     * Generate enum idl.
     *
     * @param code the code
     * @param cls the cls
     */
    private static void generateEnumIDL(StringBuilder code, Class<Enum> cls) {
        code.append("enum ").append(cls.getSimpleName()).append(" {  \n");

        Field[] fields = cls.getFields();
        for (Field field : fields) {

            String name = field.getName();
            code.append("   ").append(name).append("=");
            try {
                Enum value = Enum.valueOf(cls, name);
                if (value instanceof EnumReadable) {
                    code.append(((EnumReadable) value).value());
                } else {
                    code.append(value.ordinal());
                }
                code.append(";\n");
            } catch (Exception e) {
                continue;
            }
        }

        code.append("}\n");
    }

    /**
     * Gets the field required.
     *
     * @param required the required
     * @return the field required
     */
    private static String getFieldRequired(boolean required) {
        if (required) {
            return "required";
        }

        return "optional";
    }
}
