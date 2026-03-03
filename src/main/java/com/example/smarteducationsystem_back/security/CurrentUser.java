package com.example.smarteducationsystem_back.security;

import java.util.HashMap;
import java.util.Map;

public class CurrentUser {
    private static final ThreadLocal<Map<String, Object>> userThreadLocal = new ThreadLocal<>();

    public static void set(Map<String, Object> userInfo) {
        userThreadLocal.set(userInfo);
    }

    public static Map<String, Object> get() {
        return userThreadLocal.get();
    }

    public static Integer getUserId() {
        Map<String, Object> map = get();
        return map != null ? (Integer) map.get("userId") : null;
    }

    public static String getRoleType() {
        Map<String, Object> map = get();
        return map != null ? (String) map.get("roleType") : null;
    }

    public static void clear() {
        userThreadLocal.remove();
    }
}
