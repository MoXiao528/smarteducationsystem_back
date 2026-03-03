package com.example.smarteducationsystem_back.security;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行OPTIONS请求或非方法调用
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || !(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 获取Token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Claims claims = null;
        if (token != null) {
            claims = jwtUtils.getClaimsFromToken(token);
            if (claims != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", claims.get("userId"));
                userInfo.put("roleType", claims.get("roleType"));
                userInfo.put("username", claims.getSubject());
                CurrentUser.set(userInfo);
            }
        }

        // 检查@CheckRole
        CheckRole checkRole = handlerMethod.getMethodAnnotation(CheckRole.class);
        if (checkRole == null) {
            checkRole = handlerMethod.getBeanType().getAnnotation(CheckRole.class);
        }

        if (checkRole != null) {
            if (claims == null) {
                response.setStatus(401);
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"code\":401, \"message\":\"未登录或Token失效\"}");
                return false;
            }

            String[] requiredRoles = checkRole.value();
            if (requiredRoles.length > 0) {
                String currentRole = (String) claims.get("roleType");
                boolean hashRole = Arrays.asList(requiredRoles).contains(currentRole);
                if (!hashRole) {
                    response.setStatus(403);
                    response.setContentType("application/json;charset=utf-8");
                    response.getWriter().write("{\"code\":403, \"message\":\"无权限访问\"}");
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CurrentUser.clear();
    }
}
