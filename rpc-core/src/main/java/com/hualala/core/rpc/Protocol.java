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

    int order();

    FieldType fieldType();

    String description() default "";
}
