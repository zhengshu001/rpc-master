package com.hualala.core.grpc;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * Created by xiangbin on 2016/10/18.
 */
@Data
public class GrpcData {

    public GrpcData(String methodName) {
        this.methodName = methodName;
    }
    private Class<?> grpcServiceImpl;
    private String methodName;
    private String clentName;
    private Class<?> grpcParameterType;
    private Class<?> grpcReturnType;
    private Class<?> grpcServiceOuter;

    private Class<?> rpcParameterType;
    private Class<?> rpcReturnType;
    private Object rpcServiceImpl;
    private Class<?> rpcInterface;
    private Method rpcExecMethod;
    private Object reqFilter;
    private Object resFilter;
 }
