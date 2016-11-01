package com.hualala.core.grpc;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import com.hualala.core.base.DataMap;
import com.hualala.core.base.RequestInfo;
import com.hualala.core.base.ResultInfo;
import com.hualala.core.rpc.FieldInfo;
import com.hualala.core.rpc.FieldType;
import com.hualala.core.rpc.ProtoFieldUtils;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class GrpcUtils {

	public static String GRPC_SERVICE_IMPL_SUFFIX = "ImplBase";
	public static String GRPC_SERVICE_INTERFACE_SUFFIX = "Grpc";
	public static String GRPC_SERVICE_PACAKGE_SUFFIX = ".grpc";
	public static String GRPC_CLIENT_BEAN_NAME_SUFFIX = "Client";
	public static String GRPC_SERVICE_ASYNC_SUFFIX = "Async";
	public static String GRPC_TRACEID = "traceID";

	private static DefaultConversionService conversionService =new DefaultConversionService();

	/**
	 * 根据GrpcServiceImpl解析出rpcInterface
	 * @param grpcServiceImpl
	 * @return
	 */
	public static String parseGrpcInterface(String grpcServiceImpl) {
		String tempRpcInterface = grpcServiceImpl.substring(0, grpcServiceImpl.indexOf(GRPC_SERVICE_INTERFACE_SUFFIX));
		return tempRpcInterface.replaceAll(GRPC_SERVICE_PACAKGE_SUFFIX, "");
	}

	private static String parseGrpcServiceImpl(Class rpcInterface) {
		String grpcServiceImplClass = parseGrpcServiceOuter(rpcInterface) + "$" + rpcInterface.getSimpleName() + GRPC_SERVICE_IMPL_SUFFIX;
		return grpcServiceImplClass;
	}

	private static String parseGrpcServiceOuter(Class rpcInterface) {
		return rpcInterface.getPackage().getName() + GRPC_SERVICE_PACAKGE_SUFFIX + "." + rpcInterface.getSimpleName() + GRPC_SERVICE_INTERFACE_SUFFIX;
	}

	/**
	 * 设置RpcInterface暴露的client名称的beanName
	 * @param grpcServiceImpl
	 * @return
	 */
	public static String clientInterfaceBeanName(String grpcServiceImpl) {
		String rpcInterfaceName = parseGrpcInterface(grpcServiceImpl);
		return rpcInterfaceName + GRPC_CLIENT_BEAN_NAME_SUFFIX;
	}

	/**
	 * 设置RpcInterface暴露的client名称的beanName
	 * @param rpcInterface
	 * @return
	 */
	public static String clientInterfaceBeanName(Class<?> rpcInterface) {
		return rpcInterface.getName() + GRPC_CLIENT_BEAN_NAME_SUFFIX;
	}

	public static String parseGrpcServiceOuter(String grpcServiceImpl) {
		int index = grpcServiceImpl.indexOf("$");
		return grpcServiceImpl.substring(0, index);
	}
	public static Optional<GrpcData> parseGrpcServiceInfo(Class<?> rpcInterface, String methodName) {
		Optional<GrpcData> optional = Arrays.asList(ReflectionUtils.getAllDeclaredMethods(rpcInterface)).stream().filter((rpcMethod) ->
				rpcMethod.getName().equals(methodName) && rpcMethod.getParameterCount() == 1)
				.map((rpcMethod) -> {
					GrpcData grpcData = new GrpcData(methodName);
					grpcData.setRpcReturnType(rpcMethod.getReturnType());
					grpcData.setRpcParameterType(rpcMethod.getParameterTypes()[0]);
					grpcData.setRpcInterface(rpcInterface);
					grpcData.setRpcExecMethod(rpcMethod);
					return grpcData;
				}).findFirst();
		if (optional.isPresent()) {
			GrpcData grpcData = optional.get();
			Class<?> grpcServiceImpl = ClassUtils.resolveClassName(parseGrpcServiceImpl(rpcInterface), ClassUtils.getDefaultClassLoader());
			Class<?> grpcServiceOuter = ClassUtils.resolveClassName(parseGrpcServiceOuter(rpcInterface), ClassUtils.getDefaultClassLoader());
			grpcData.setGrpcServiceImpl(grpcServiceImpl);
			grpcData.setGrpcServiceOuter(grpcServiceOuter);
			grpcData.setClentName(rpcInterface.getPackage().getName());
			Optional<GrpcData> parseGrpcDataOptional = Arrays.asList(ReflectionUtils.getAllDeclaredMethods(grpcServiceImpl)).stream().filter((grpcMethod) ->
					grpcMethod.getName().equals(methodName) && grpcMethod.getParameterCount() == 2
			).map((grpcMethod) -> {
				GrpcData parseGrpcData = new GrpcData(methodName);
				Type[] ts = grpcMethod.getGenericParameterTypes();
				if (ts.length == 2 && ts[1] instanceof ParameterizedType) {
					ParameterizedType ptype = ((ParameterizedType)ts[1]);
					Type[] types = ptype.getActualTypeArguments();
					if (types.length == 1) {
						parseGrpcData.setGrpcReturnType((Class)types[0]);
					}
				}
				parseGrpcData.setGrpcParameterType((Class)ts[0]);
				return parseGrpcData;
			}).findFirst();
			if (parseGrpcDataOptional.isPresent()) {
				grpcData.setGrpcParameterType(parseGrpcDataOptional.get().getGrpcParameterType());
				grpcData.setGrpcReturnType(parseGrpcDataOptional.get().getGrpcReturnType());
			}
		}
		return optional;
	}

	public static RequestInfo dataMapToRequestBean(String traceID, DataMap paramData, Class<?> reqBeanClass) {
		RequestInfo requestInfo = (RequestInfo)dataMapToBean(paramData, reqBeanClass);
		requestInfo.setTraceID(traceID);
		return requestInfo;
	}

	public static ResultInfo resultBeanToDataMap(String traceID, Object resBean, Class<?> resBeanClass) {
		DataMap resDataMap = beanToDataMap(resBean, resBeanClass);
		if (resBean instanceof ResultInfo) {
			ResultInfo resultInfo = (ResultInfo)resBean;
			resDataMap.setTraceID(traceID).setCode(resultInfo.getCode()).setMessage(resultInfo.getMessage());
		}
		return resDataMap;
	}

	public static Object resultInfoToBean(ResultInfo resultInfo, Class<?> resBeanClass) {
		Object resBean;
		if (resultInfo instanceof DataMap) {
			resBean = dataMapToBean((DataMap)resultInfo, resBeanClass);
		} else {
			resBean = BeanUtils.instantiate(resBeanClass);
		}
		if (resBean instanceof ResultInfo) {
			((ResultInfo)resBean).setTraceID(resultInfo.getTraceID()).setCode(resultInfo.getCode()).setMessage(resultInfo.getMessage());
		}
		return resBean;
	}


	public static DataMap beanToDataMap(Object resBean, Class rpcResClass) {
		DataMap resDataMap = new DataMap();
		Arrays.asList(rpcResClass.getDeclaredFields()).stream().forEach(field -> {
			ReflectionUtils.makeAccessible(field);
			Object value = ReflectionUtils.getField(field, resBean);
			if (value != null) {
				if (List.class.isAssignableFrom(field.getType())) {
					Type type = field.getGenericType();
					if (type instanceof ParameterizedType) {
						Class actualType = (Class)((ParameterizedType)type).getActualTypeArguments()[0];
						if (BeanUtils.isSimpleValueType(actualType)) {
							resDataMap.put(field.getName(), value);
						} else {
							List<DataMap> resultList = ((List<Object>)value).stream().map(object ->
									beanToDataMap(object, actualType)
							).collect(Collectors.toList());
							resDataMap.put(field.getName(), resultList);
						}
					}
				} else if (Enum.class.isAssignableFrom(field.getType())) {
					resDataMap.put(field.getName(), value.toString());
				} else if (BeanUtils.isSimpleValueType(field.getType())){
					resDataMap.put(field.getName(), value);
				} else {
					resDataMap.put(field.getName(), beanToDataMap(value, field.getType()));
				}
			}
		});
		return resDataMap;
	}

	private static Object dataMapToBean(DataMap paramData, Class<?> beanClass) {
		Object rpcData = BeanUtils.instantiate(beanClass);
		Arrays.asList(beanClass.getDeclaredFields()).stream().forEach(field -> {
			if (paramData.contains(field.getName())) {
				Object value = paramData.get(field.getName());
				if (List.class.isAssignableFrom(field.getType()) && value instanceof List) {
					Type type = field.getGenericType();
					if (type instanceof ParameterizedType) {
						Class actualType = (Class)((ParameterizedType)type).getActualTypeArguments()[0];
						if (BeanUtils.isSimpleValueType(actualType)) {
							ReflectionUtils.makeAccessible(field);
							ReflectionUtils.setField(field, rpcData, value);
						} else {
							List<Object> resultList = ((List<DataMap>)value).stream().map(dataMap ->
									dataMapToBean(dataMap, actualType)
							).collect(Collectors.toList());
							ReflectionUtils.makeAccessible(field);
							ReflectionUtils.setField(field, rpcData, resultList);
						}
					}
				} else if (Enum.class.isAssignableFrom(field.getType())) {
					value = Enum.valueOf((Class<Enum>)field.getType(), value.toString());
					ReflectionUtils.makeAccessible(field);
					ReflectionUtils.setField(field, rpcData, value);
				} else if (BeanUtils.isSimpleValueType(field.getType())){
					if (conversionService.canConvert(value.getClass(), field.getType())) {
						ReflectionUtils.makeAccessible(field);
						ReflectionUtils.setField(field, rpcData, conversionService.convert(value, field.getType()));
					}
				} else if (value instanceof DataMap) {
					value = dataMapToBean((DataMap)value, field.getType());
					ReflectionUtils.makeAccessible(field);
					ReflectionUtils.setField(field, rpcData, value);
				}
			}
		});
		return rpcData;
	}

	public static ResultInfo grpcMessageToResultBean(GeneratedMessage rpcMessage, Class<?> resultBeanClass) {
		return (ResultInfo)grpcMessageToDataBean(rpcMessage, resultBeanClass);
//		Codec<?> codec = ProtobufProxy.create(resultBeanClass);
//		try {
//			return (ResultInfo)codec.decode(rpcMessage.toByteArray());
//		} catch (IOException e) {
//			throw new IllegalArgumentException("grpc message to result error", e);
//		}
	}

	public static RequestInfo grpcMessageToRequest(GeneratedMessage rpcMessage, Class<?> requestClass) {
		return (RequestInfo)grpcMessageToDataBean(rpcMessage, requestClass);
//		Codec<?> codec = ProtobufProxy.create(requestClass);
//		try {
//			return (RequestInfo)codec.decode(rpcMessage.toByteArray());
//		} catch (IOException e) {
//			throw new IllegalArgumentException("grpc message to request error", e);
//		}
	}

	public static GeneratedMessage requestToGrpcMessage(RequestInfo requestInfo, Class<?> grpcMessageClass) {
		return (GeneratedMessage)dataBeanToGrpcMessage(requestInfo, grpcMessageClass);
//		Codec codec = ProtobufProxy.create(requestInfo.getClass());
//		try {
//			Method method = ReflectionUtils.findMethod(grpcMessageClass, "parseFrom", byte[].class);
//			Object object = ReflectionUtils.invokeMethod(method, null, new Object[] {codec.encode(requestInfo)});
//			return (GeneratedMessage)object;
//		} catch (IOException e) {
//			throw new IllegalArgumentException("request to client message error", e);
//		}
	}

	private static <T> T grpcMessageToDataBean(CodedInputStream input, Class<T> dataBeanClass) {
		T object = BeanUtils.instantiate(dataBeanClass);
		List<FieldInfo> fieldInfoList = ProtoFieldUtils.getAllProtoField(dataBeanClass);
		boolean done = false;
		try {
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
							Object value = grpcMessageToDataBean(input, fieldInfo.getGenericType());
							listObject.add(value);
							input.checkLastTagWas(0);
							input.popLimit(oldLimit);
						} else {
							if (fieldInfo.isPrimitiveType()) {
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
						Object value = grpcMessageToDataBean(input, fieldInfo.getGenericType());
						ReflectionUtils.makeAccessible(fieldInfo.getField());
						ReflectionUtils.setField(fieldInfo.getField(), object, value);
						input.checkLastTagWas(0);
						input.popLimit(oldLimit);
					} else if (fieldInfo.getFieldType() == FieldType.ENUM) {
						Enum[] enums = ((Class<Enum>)fieldInfo.getField().getType()).getEnumConstants();
						Object value = enums[input.readEnum()];
						ReflectionUtils.makeAccessible(fieldInfo.getField());
						ReflectionUtils.setField(fieldInfo.getField(), object, value);
					} else if (fieldInfo.isPrimitiveType() || fieldInfo.getFieldType() == FieldType.STRING) {
						Object value = ReflectionUtils.invokeMethod(fieldInfo.getFieldType().getDecodeMethod(), input);
						ReflectionUtils.makeAccessible(fieldInfo.getField());
						ReflectionUtils.setField(fieldInfo.getField(), object, value);
					}
					continue;
				}
				input.skipField(tag);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return object;
	}

	public static <T> T dataBeanToGrpcMessage(Object dataBean, Class<T> grpcMessageClass) {
		byte[] bytes = encodeDataBean(dataBean, dataBean.getClass());
		Method method = ReflectionUtils.findMethod(grpcMessageClass, "parseFrom", byte[].class);
		Object object = ReflectionUtils.invokeMethod(method, null, new Object[] {bytes});
		return (T)object;
	}

	private static byte[] encodeDataBean(Object object, Class<?> clazz) {
		List<FieldInfo> fieldInfoList = ProtoFieldUtils.getAllProtoField(clazz);
		List<EncodeData> encodeDataList = new ArrayList<>();
		int size = computeSerializedSize(object, fieldInfoList, encodeDataList);
		byte[] bytes = new byte[size];
		CodedOutputStream output = CodedOutputStream.newInstance(bytes);
		encodeCodeOutput(encodeDataList, output);
		return bytes;
	}

	private static void encodeCodeOutput(List<EncodeData> encodeDataList, CodedOutputStream output) {
		encodeDataList.stream().forEach(encodeData -> {
			FieldInfo fieldInfo = encodeData.fieldInfo;
			if (fieldInfo.isList()) {
				writeListFieldData(fieldInfo, encodeData, (List)encodeData.getValue(), output);
			} else {
				writeFieldData(fieldInfo, encodeData, output);
			}
		});
	}

	private static void writeFieldData(FieldInfo fieldInfo, EncodeData encodeData, CodedOutputStream output) {
		try {
			if (fieldInfo.isPrimitiveType()) {
				if (fieldInfo.getFieldType() == FieldType.INT) {
					output.writeInt32(fieldInfo.getOrder(), (int) encodeData.getValue());
				} else if (fieldInfo.getFieldType() == FieldType.LONG) {
					output.writeInt64(fieldInfo.getOrder(), (long) encodeData.getValue());
				} else if (fieldInfo.getFieldType() == FieldType.FLOAT) {
					output.writeFloat(fieldInfo.getOrder(), (float) encodeData.getValue());
				} else if (fieldInfo.getFieldType() == FieldType.DOUBLE) {
					output.writeDouble(fieldInfo.getOrder(), (double) encodeData.getValue());
				}
			} else if (fieldInfo.getFieldType() == FieldType.ENUM) {
				output.writeEnum(fieldInfo.getOrder(), ((Enum)encodeData.getValue()).ordinal());
			} else if (fieldInfo.getFieldType() == FieldType.OBJECT) {
				output.writeTag(fieldInfo.getOrder(), 2);
				output.writeUInt32NoTag(encodeData.dataSize);
				encodeCodeOutput((List<EncodeData>) encodeData.getValue(), output);
			} else if (fieldInfo.getFieldType() == FieldType.STRING){
				output.writeString(fieldInfo.getOrder(), (String)encodeData.getValue());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private static void writeListFieldData(FieldInfo fieldInfo, EncodeData encodeData, List valueList, CodedOutputStream output) {
		try {
			int dataSize = valueList.size();
			if (fieldInfo.isPrimitiveType()) {
				output.writeRawVarint32(fieldInfo.repeatedMakeTag());
				output.writeRawVarint32(encodeData.dataSize);
				for (int i = 0; i < dataSize; i++) {
					if (fieldInfo.getFieldType() == FieldType.INT) {
						output.writeInt32NoTag((int)valueList.get(i));
					} else if (fieldInfo.getFieldType() == FieldType.LONG) {
						output.writeInt64NoTag((long)valueList.get(i));
					} else if (fieldInfo.getFieldType() == FieldType.FLOAT) {
						output.writeFloatNoTag((float)valueList.get(i));
					} else if (fieldInfo.getFieldType() == FieldType.DOUBLE) {
						output.writeDoubleNoTag((double)valueList.get(i));
					}
				}
			} else if (fieldInfo.getFieldType() == FieldType.STRING) {
				for (int i = 0; i < dataSize; i++) {
					output.writeString(fieldInfo.getOrder(), (String)valueList.get(i));
				}
			} else if (fieldInfo.getFieldType() == FieldType.OBJECT) {
				List<List<EncodeData>> dataList = (List<List<EncodeData>>)valueList;
				List<Integer> dataSizeList = (List<Integer>)encodeData.getExtra();
				for (int i = 0; i < dataSize; i++) {
					output.writeRawVarint32(fieldInfo.makeTag());
					output.writeRawVarint32(dataSizeList.get(i));
					encodeCodeOutput(dataList.get(i), output);
				}
			}
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	private static int computeSerializedSize(Object object, List<FieldInfo> fieldInfoList, List<EncodeData> encodeDataList) {
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

	private static int computeListFieldSize(FieldInfo fieldInfo, List<Object> valueList, EncodeData encodeData) {
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

	private static int computeFieldSize(FieldInfo fieldInfo, Object value, EncodeData encodeData) {
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

	private static int computePrimitiveSize(FieldInfo fieldInfo, Object o) {
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

	public static <T> T grpcMessageToDataBean(GeneratedMessage rpcMessage, Class<T> dataBeanClass) {
		byte[] bytes = rpcMessage.toByteArray();
		CodedInputStream input = CodedInputStream.newInstance(bytes, 0, bytes.length);
		T object = grpcMessageToDataBean(input, dataBeanClass);
		return object;
	}

	public static Optional getRequestHeaderFieldValue(GeneratedMessage rpcMessage, String fieldName) {
		Map<Descriptors.FieldDescriptor, Object> allFields = rpcMessage.getAllFields();
		Optional optional = allFields.keySet().stream().filter(fieldDescriptor ->
				"header".equals(fieldDescriptor.getName())
		).findFirst();
		if (optional.isPresent()) {
			GeneratedMessage requestHeaderMessage = ((GeneratedMessage)allFields.get(optional.get()));
			return getFieldValue(requestHeaderMessage, fieldName);
		}
		return optional;
	}

	public static GeneratedMessage resultToGrpcMessage(ResultInfo resultInfo, Class<?> rpcClass) {
		return (GeneratedMessage)dataBeanToGrpcMessage(resultInfo, rpcClass);
//		Codec codec = ProtobufProxy.create(resultInfo.getClass());
//		try {
//			Method method = ReflectionUtils.findMethod(rpcClass, "parseFrom", byte[].class);
//			Object object = ReflectionUtils.invokeMethod(method, null, new Object[] {codec.encode(resultInfo)});
//			return (GeneratedMessage)object;
//		} catch (IOException e) {
//			throw new IllegalArgumentException("result to client message error", e);
//		}
	}

	private static Optional getFieldValue(GeneratedMessage rpcMessage, String fieldName) {
		Map<Descriptors.FieldDescriptor, Object> allFields = rpcMessage.getAllFields();
		return allFields.keySet().stream().filter(fieldDescriptor ->
				fieldName.equals(fieldDescriptor.getName())).findFirst().map(fieldDescriptor -> allFields.get(fieldDescriptor));
	}

	@Data
	private static class EncodeData {
		private final FieldInfo fieldInfo;
		private Object value;
		private Object extra;
		private int dataSize;
		public EncodeData(FieldInfo fieldInfo) {
			this.fieldInfo = fieldInfo;
		}
	}
}
