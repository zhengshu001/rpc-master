package com.hualala.core.api;

import com.hualala.core.ErrorInfo;
import com.hualala.core.base.DataMap;
import com.hualala.core.base.RequestInfo;
import com.hualala.core.base.ResultInfo;
import com.hualala.core.client.BaseRpcClient;
import com.hualala.core.config.message.MessageInfo;
import com.hualala.core.grpc.GrpcData;
import com.hualala.core.grpc.GrpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by xiangbin on 2016/7/5.
 */
public class BaseController implements ApplicationContextAware {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected ApplicationContext appContext;

    @Autowired
    private BaseDataFilter dataFilter;
    @Autowired
    private BaseRpcClient baseRpcClient;
    @Autowired
    private MessageInfo messageInfo;

    private String KEY_TRACEID = "traceID";
    private String KEY_CLIENT_IP = "clientIP";

    public ResultInfo hello(HttpServletRequest httpReq, HttpServletResponse httpRes) {
        DataMap dataMap = new DataMap()
                .putAll(parseHttpRequest(httpReq))
                .put("key", "hello client api").put("dateTime",DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()));
        return dataMap;
    }

    private ResultInfo execute(String uri, DataMap reqData) {
        long startTime = System.currentTimeMillis();
        Optional grpcDataOptional = dataFilter.parseRpcData(uri);
        if (!grpcDataOptional.isPresent()) {
            return messageInfo.getResultMessage(ErrorInfo.API_RPC_SERVICE_NOT_EXIST);
        }
        String traceID = reqData.getTraceID();
        if (traceID == null && reqData.contains(KEY_TRACEID)) {
            traceID = (String)reqData.get(KEY_TRACEID);
        }
        GrpcData grpcData = (GrpcData)grpcDataOptional.get();
        ResultInfo resultInfo = messageInfo.getResultMessage(ErrorInfo.API_PROXY_RPC_DEFAULT_ERROR).setTraceID(traceID);
        try {
            DataMap reqDataMap = dataFilter.reqDataFilter(uri, grpcData.getReqFilter(), reqData);
            if (traceID == null) {
                traceID = reqDataMap.contains("traceID") ? (String)reqDataMap.get("traceID") : UUID.randomUUID().toString();
            }
            if (logger.isInfoEnabled()) {
                logger.info("traceID [" + traceID + "] uri [" + uri + "] rpcInterface [" + grpcData.getRpcInterface() + "] method [" + grpcData.getMethodName() + "]");
                logger.info("req data [" + reqDataMap  + "]");
            }
            //请求数据转换
            RequestInfo rpcReqBean = GrpcUtils.dataMapToRequestBean(traceID, reqDataMap, grpcData.getRpcParameterType());
            Object rpcInterface = baseRpcClient.getRpcClient(grpcData.getRpcInterface());
            ResultInfo rpcResBean = (ResultInfo) ReflectionUtils.invokeMethod(grpcData.getRpcExecMethod(), rpcInterface, rpcReqBean);
            resultInfo = GrpcUtils.resultBeanToDataMap(traceID, rpcResBean, grpcData.getRpcReturnType());
            if (logger.isInfoEnabled()) {
                logger.info("traceID [" + traceID + "] res data [" + resultInfo + "]");
            }
            if (rpcResBean.success()) {
                resultInfo = dataFilter.resDataFilter(uri, grpcData.getResFilter(), reqDataMap, (DataMap)resultInfo);
            }
            return resultInfo.setTraceID(traceID);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("traceID [" + traceID + "] uri [" + uri + "] remote error", e);
            return messageInfo.getResultMessage(ErrorInfo.API_CALL_RPC_ERROR);
        } finally {
            if (logger.isInfoEnabled()) {
                logger.info("traceID [" + traceID + "] id [" + uri + "] cost [" + (System.currentTimeMillis() - startTime) + "]ms");
                logger.info("res data [" + resultInfo + "]");
            }
        }
    }

    public ResultInfo execute(HttpServletRequest httpReq, HttpServletResponse httpRes) {
        long startTime = System.currentTimeMillis();
        String uri = httpReq.getRequestURI();
        DataMap reqData = parseHttpRequest(httpReq);
        ResultInfo resultInfo = execute(uri, reqData);
        if (logger.isInfoEnabled()) {
            logger.info("uri [" + uri + "] cost [" + (System.currentTimeMillis() - startTime) + "]ms");
        }
        return resultInfo;
    }

    protected DataMap parseHttpRequest(HttpServletRequest httpRequest) {
        DataMap dataMap = new DataMap();
        Enumeration<String> params = httpRequest.getParameterNames();
        while (params.hasMoreElements()) {
            String key = params.nextElement();
            String value = httpRequest.getParameter(key);
            if (KEY_TRACEID.equals(key)) {
                dataMap.setTraceID(value);
            } else {
                dataMap.put(key, value);
            }
        }
        dataMap.put(KEY_CLIENT_IP, getRemoteIp(httpRequest));
        return dataMap;
    }

    private String getRemoteIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("x-real-ip");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip.startsWith("unknown")) {
            ip = ip.substring(ip.indexOf("unknown") + "unknown".length());
        }
        ip = ip.trim();
        if (ip.startsWith(",")) {
            ip = ip.substring(1);
        }
        if (ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }

    protected HttpServletRequest getHttpRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    protected HttpServletResponse getHttpResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }
}
