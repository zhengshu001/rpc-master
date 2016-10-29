package com.hualala.core.rpc;

import java.lang.annotation.*;

/**
 * Created by xiangbin on 2016/10/26.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Protocol {

    //顺序 Grpc协议必须输入
    int order();
    //对应 Grpc的类型
    FieldType fieldType();

    String description() default "";
}
