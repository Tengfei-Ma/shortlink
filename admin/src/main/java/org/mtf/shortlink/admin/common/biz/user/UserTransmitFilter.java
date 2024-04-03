package org.mtf.shortlink.admin.common.biz.user;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.admin.common.constant.RedisCacheConstant;
import org.mtf.shortlink.admin.common.constant.user.UserConstant;
import org.mtf.shortlink.admin.common.convention.exception.ClientException;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.List;

import static org.mtf.shortlink.admin.common.enums.UserErroeCodeEnum.USERNAME_OR_TOKEN__NOT_EXIST;

/**
 * 用户信息传输过滤器
 */
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {
    private final StringRedisTemplate stringRedisTemplate;
    /**
    * 查询用户名是否可用接口，登录接口，注册接口需要忽略
    * 但是注册接口的url和一些其他接口的url重复，只是请求方式不一样，需要单独判断
    */
    private static final List<String> IGNORE_URI = Lists.newArrayList(
            "/api/shortlink/v1/user/login",
            "/api/shortlink/v1/user/has-username");
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpServletRequest.getRequestURI();
        if(!IGNORE_URI.contains(requestURI)){
            String method = httpServletRequest.getMethod();
            if(!("/api/shortlink/v1/user".equals(requestURI)&&"POST".equals(method))){
                String username = httpServletRequest.getHeader(UserConstant.USERINFO_USERNAME);
                String token = httpServletRequest.getHeader(UserConstant.USERINFO_TOKEN);
                if(!StrUtil.isAllNotBlank(username,token)){
                    throw new ClientException(USERNAME_OR_TOKEN__NOT_EXIST);
                }
                Object userInfoJsonStr;
                try {
                    userInfoJsonStr= stringRedisTemplate.opsForHash().get(RedisCacheConstant.USER_LOGIN_KEY+username, token);
                    if(userInfoJsonStr==null){
                        throw new ClientException(USERNAME_OR_TOKEN__NOT_EXIST);
                    }
                }catch (Exception exception){
                    //全局异常拦截器拦截不到？
                    throw new ClientException(USERNAME_OR_TOKEN__NOT_EXIST);
                }
                UserInfoDTO userInfoDTO = JSON.parseObject(userInfoJsonStr.toString(), UserInfoDTO.class);
                UserContext.setUser(userInfoDTO);
            }
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }
}
