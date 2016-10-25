package com.hualala.core.grpc;

/**
 * Created by xiangbin on 2016/10/18.
 */
public class TraceThreadLocal {

    private static ThreadLocal<String> threadLocal = new ThreadLocal();

    public static void setValue(String value) {
        threadLocal.set(value);
    }

    public static String getValue() {
        return threadLocal.get();
    }
}
