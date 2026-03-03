package com.example.smarteducationsystem_back.security;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    // 标识哪些方法需要做数据范围校验，拦截器会根据当前用户角色动态拦截
    boolean value() default true;
}
