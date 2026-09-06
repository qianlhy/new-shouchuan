package com.diy.interceptor;

import com.diy.constant.JwtClaimsConstant;
import com.diy.context.BaseContext;
import com.diy.properties.JwtProperties;
import com.diy.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getUserTokenName());
        log.info("用户端拦截器 - 请求路径: {}", request.getRequestURI());
        log.info("用户端拦截器 - Token名称: {}", jwtProperties.getUserTokenName());
        log.info("用户端拦截器 - Token值: {}", token);
        
        //2、校验令牌
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            
            // 兼容管理员token和用户token（开发阶段方便测试）
            Long currentId = null;
            if (claims.get(JwtClaimsConstant.USER_ID) != null) {
                // 用户token
                currentId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
                log.info("用户端拦截器 - 检测到用户token，用户ID: {}", currentId);
            } else if (claims.get(JwtClaimsConstant.EMP_ID) != null) {
                // 管理员token（开发阶段兼容）
                currentId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
                log.info("用户端拦截器 - 检测到管理员token，管理员ID: {}（开发模式）", currentId);
            }
            
            if (currentId == null) {
                log.error("用户端拦截器 - JWT中没有找到userId或empId");
                response.setStatus(401);
                return false;
            }
            
            BaseContext.setCurrentId(currentId);
            //3、通过，放行
            return true;
        } catch (Exception ex) {
            //4、不通过，响应401状态码
            log.error("用户端拦截器 - JWT校验失败: {}", ex.getMessage());
            response.setStatus(401);
            return false;
        }
    }
}
