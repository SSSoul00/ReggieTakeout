package com.itheima.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.common.BaseContext;
import com.itheima.controller.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 过滤器，拦截未登录请求
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，可以识别通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //1、获取本次请求URI
        String requestURI = request.getRequestURI();
        log.info("拦截到请求{}",requestURI);
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/login",
                "/user/sendMsg"
        };
        //2、判断本次请求是否需要处理
        boolean check = check(urls, requestURI);
        //3、如果不需要处理则直接放行
        if (check) {
            log.info("请求{}不需要处理直接放行",requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        //4、判断登陆状态，如果已登录则直接放行
        if (request.getSession().getAttribute("employee") != null) {
            Long employeeId = (Long) request.getSession().getAttribute("employee");
            log.info("用户已登录直接放行，用户ID:{}",employeeId);
            log.info(String.valueOf(Thread.currentThread().getId()));
            BaseContext.set(employeeId);
            filterChain.doFilter(request, response);
            return;
        }
        if (request.getSession().getAttribute("user") != null) {
            Long userId = (Long) request.getSession().getAttribute("user");
            log.info("用户已登录直接放行，用户ID:{}",userId);
            log.info(String.valueOf(Thread.currentThread().getId()));
            BaseContext.set(userId);
            filterChain.doFilter(request, response);
            return;
        }
        //5、如果未登录则返回未登录结果
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 判断请求路径是否需要处理
     * @param urls
     * @param requestUri
     * @return
     */
    public boolean check(String[] urls, String requestUri) {
        boolean match;
        for (String url : urls) {
            match = PATH_MATCHER.match(url, requestUri);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
