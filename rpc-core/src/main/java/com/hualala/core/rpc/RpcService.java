package com.hualala.core.rpc;

import java.lang.annotation.*;

/**
 * 用于标记是Rpc服务
 * Created by xiangbin on 2016/10/31.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcService {
}
