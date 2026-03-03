package com.example.smarteducationsystem_back.security;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckRole {
    String[] value() default {}; // 如 "SYS_ADMIN", "SCHOOL_ADMIN"
}
