package com.hualala.core.grpc;

import com.baidu.bjf.remoting.protobuf.Codec;
import com.baidu.bjf.remoting.protobuf.ProtobufProxy;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import com.hualala.core.base.DataMap;
import com.hualala.core.base.RequestInfo;
import com.hualala.core.base.ResultInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
		Codec<?> codec = ProtobufProxy.create(resultBeanClass);
		try {
			return (ResultInfo)codec.decode(rpcMessage.toByteArray());
		} catch (IOException e) {
			throw new IllegalArgumentException("grpc message to result error", e);
		}
	}

	public static RequestInfo grpcMessageToRequest(GeneratedMessage rpcMessage, Class<?> requestClass) {
		Codec<?> codec = ProtobufProxy.create(requestClass);
		try {
			return (RequestInfo)codec.decode(rpcMessage.toByteArray());
		} catch (IOException e) {
			throw new IllegalArgumentException("grpc message to request error", e);
		}
	}

	public static GeneratedMessage requestToGrpcMessage(RequestInfo requestInfo, Class<?> grpcMessageClass) {
		Codec codec = ProtobufProxy.create(requestInfo.getClass());
		try {
			Method method = ReflectionUtils.findMethod(grpcMessageClass, "parseFrom", byte[].class);
			Object object = ReflectionUtils.invokeMethod(method, null, new Object[] {codec.encode(requestInfo)});
			return (GeneratedMessage)object;
		} catch (IOException e) {
			throw new IllegalArgumentException("request to client message error", e);
		}
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
		Codec codec = ProtobufProxy.create(resultInfo.getClass());
		try {
			Method method = ReflectionUtils.findMethod(rpcClass, "parseFrom", byte[].class);
			Object object = ReflectionUtils.invokeMethod(method, null, new Object[] {codec.encode(resultInfo)});
			return (GeneratedMessage)object;
		} catch (IOException e) {
			throw new IllegalArgumentException("result to client message error", e);
		}
	}

	private static Optional getFieldValue(GeneratedMessage rpcMessage, String fieldName) {
		Map<Descriptors.FieldDescriptor, Object> allFields = rpcMessage.getAllFields();
		return allFields.keySet().stream().filter(fieldDescriptor ->
				fieldName.equals(fieldDescriptor.getName())).findFirst().map(fieldDescriptor -> allFields.get(fieldDescriptor));
	}
}
