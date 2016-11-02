package com.hualala.core.rpc;

import com.hualala.core.base.RequestInfo;
import com.hualala.core.base.ResultInfo;
import org.springframework.util.ReflectionUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 生成要求格式的Grpc protobuf
 * Created by xiangbin on 2016/10/26.
 */
public class RpcProtoGenerator {

    private static String protoFile = "src/main/proto";
    public static void generate(Class<?> rpcInterface, String protoName, String output) {
        generate(rpcInterface, protoName, false, new Class[]{}, "", output);
    }

    public static void generateCmmons(Class[] rpcInterfaces, String protoName) {
        if (rpcInterfaces.length == 0) {
            return;
        }
        StringBuilder code = new StringBuilder();
        code.append("syntax = \"proto3\"").append(";\n").append("\n");
        code.append("option java_package = \"").append(rpcInterfaces[0].getPackage().getName()).append(".grpc").append("\";\n");

        String dataName = rpcInterfaces[0].getPackage().getName();
        dataName = dataName.substring(dataName.lastIndexOf(".") + 1);
        dataName = Character.toUpperCase(dataName.charAt(0)) + dataName.substring(1);
        code.append("option java_outer_classname = \"").append(dataName).append("Common").append("\";\n");
        Set<Class<?>> cachedTypes = new HashSet<>();
        Set<Class<?>> cachedEnumTypes = new HashSet<>();
        cachedTypes.add(ResultInfo.ResultHeader.class);
        cachedTypes.add(RequestInfo.RequestHeader.class);
        for (Class rpcInterface : rpcInterfaces) {
            if (Enum.class.isAssignableFrom(rpcInterface)) {
                generateEnum(code, rpcInterface);
            } else {
                generate(code, rpcInterface, cachedTypes, cachedEnumTypes);
            }
        }
        String codeStr = code.toString();
        System.out.println(codeStr);
        outputProto(codeStr, protoName, protoFile);
    }

    public static void generate(Class<?> rpcInterface, String protoName) {
        generate(rpcInterface, protoName, false, new Class[]{}, "", protoFile);
    }

    public static void generate(Class rpcInterface, String protoName, boolean ignoreCommon, Class[] excludeClasses, String extracommons) {
        generate(rpcInterface, protoName, ignoreCommon, excludeClasses, extracommons, protoFile);
    }

    public static void generate(Class rpcInterface, String protoName, boolean ignoreCommon, Class[] excludeClasses, String extracommons, String protoFile) {
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
        code.append("import \"commons.proto\"").append(";\n");
        if (extracommons != null && !"".equals(extracommons)) {
            code.append("import \"").append(extracommons).append(".proto\"").append(";\n");
        }
        code.append("\n");
        Set<Class<?>> cachedTypes = new HashSet<>();
        cachedTypes.add(ResultInfo.ResultHeader.class);
        cachedTypes.add(RequestInfo.RequestHeader.class);
        Set<Class<?>> cachedEnumTypes = new HashSet<>();
        for (Class excludeClass : excludeClasses) {
            if (Enum.class.isAssignableFrom(excludeClass)) {
                cachedEnumTypes.add(excludeClass);
            } else {
                cachedTypes.add(excludeClass);
            }
        }

        StringBuilder service = new StringBuilder();
        service.append("service ").append(rpcInterface.getSimpleName()).append("{").append("\n");
        Arrays.asList(methods).stream().forEach(rpcMethod -> {
            Class<?> requestDataClass = rpcMethod.getParameterTypes()[0];
            String requestMessage = generate(requestDataClass, cachedTypes, cachedEnumTypes);
            if (requestMessage != null) {
                code.append(requestMessage);
            }
            //System.out.print("=================" + requestMessage);

            Class<?> responseDataClass = rpcMethod.getReturnType();
            String responseMessage = generate(responseDataClass, cachedTypes, cachedEnumTypes);
            if (responseMessage != null) {
                code.append(responseMessage);
            }
            //System.out.print("+++++++++++++++++" + responseMessage);
            service.append("    ").append("rpc ").append(rpcMethod.getName())
                    .append("(").append(requestDataClass.getSimpleName()).append(") returns ")
                    .append("(").append(responseDataClass.getSimpleName()).append(") {}").append("\n");
        });
        service.append("}").append("\n");
        code.append(service.toString());
        String codeStr = code.toString();
        System.out.println(codeStr);
        outputProto(codeStr, protoName, protoFile);

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
            outputProto(commStr, "commons", protoFile);
        }
    }

    private static void outputProto(String protoStr, String protoName, String protoFile) {
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

    private static String generate(final Class<?> cls, final Set<Class<?>> cachedTypes,
                                 final Set<Class<?>> cachedEnumTypes) {
        Set<Class<?>> types = cachedTypes;
        Set<Class<?>> enumTypes = cachedEnumTypes;
        if (types.contains(cls)) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        types.add(cls);
        generate(code, cls, types, enumTypes);
        return code.toString();
    }

    private static void generate(StringBuilder code, Class<?> cls, Set<Class<?>> cachedTypes,
                                    Set<Class<?>> cachedEnumTypes) {
        List<FieldInfo> fields = ProtoFieldUtils.getAllProtoField(cls);
        Set<Class<?>> subTypes = new HashSet<>();
        Set<Class<Enum>> enumTypes = new HashSet<>();
        code.append("message ").append(cls.getSimpleName()).append(" {  \n");
        fields.stream().forEach(fieldInfo -> {
            if (fieldInfo.hasDescription()) {
                code.append("   ").append("// ").append(fieldInfo.getDescription()).append("\n");
            }
            code.append("   ");
            if (fieldInfo.isList()) {
                code.append("repeated").append(" ");
            }
            if ((fieldInfo.getFieldType() == FieldType.OBJECT) || fieldInfo.getFieldType() == FieldType.ENUM) {
                Class<?> genericType = fieldInfo.getGenericType();
                code.append(genericType.getSimpleName()).append(" ");
                if (fieldInfo.getFieldType() == FieldType.ENUM) {
                    if (!cachedEnumTypes.contains(genericType)) {
                        cachedEnumTypes.add(genericType);
                        enumTypes.add((Class<Enum>)genericType);
                    }
                } else {
                    if (!cachedTypes.contains(genericType)) {
                        cachedTypes.add(genericType);
                        subTypes.add(genericType);
                    }
                }
            } else {
                String type = fieldInfo.getFieldType().getType().toLowerCase();
                code.append(type).append(" ");
            }
            code.append(fieldInfo.getName()).append("=").append(fieldInfo.getOrder()).append(";\n");
//            if ((fieldInfo.getFieldType() == FieldType.OBJECT) || fieldInfo.getFieldType() == FieldType.ENUM) {
//                if
//                } else {
//                    Class c = fieldInfo.getType();
//                    code.append("   ").append(c.getSimpleName()).append(" ").append(fieldInfo.getName()).append("=").append(fieldInfo.getOrder()).append(";\n");
//                    if (fieldInfo.getFieldType() == FieldType.ENUM) {
//                        if (!cachedEnumTypes.contains(c)) {
//                            cachedEnumTypes.add(c);
//                            enumTypes.add(c);
//                        }
//                    } else {
//                        if (!cachedTypes.contains(c)) {
//                            cachedTypes.add(c);
//                            subTypes.add(c);
//                        }
//                    }
//                }
//            } else {
//                String type = fieldInfo.getFieldType().getType().toLowerCase();
//                code.append("   ");
//                if (fieldInfo.isList()) {
//                    code.append("repeated").append(" ");
//                }
//                code.append(type).append(" ").append(fieldInfo.getName()).append("=").append(fieldInfo.getOrder()).append(";\n");
//            }
        });
        code.append("}\n");
        enumTypes.stream().forEach((subType) ->  generateEnum(code, subType));
        subTypes.stream().forEach((subType) ->  generate(code, subType, cachedTypes, cachedEnumTypes));
    }

    private static void generateEnum(StringBuilder code, Class<Enum> cls) {
        code.append("enum ").append(cls.getSimpleName()).append(" {  \n");

        Arrays.asList(cls.getFields()).stream().forEach(field -> {
            code.append("   ").append(field.getName()).append("=");
            Enum value = Enum.valueOf(cls, field.getName());
            code.append(value.ordinal()).append(";\n");
        });
        code.append("}\n");
    }
}
