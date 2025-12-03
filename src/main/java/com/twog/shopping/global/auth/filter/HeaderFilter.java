package com.twog.shopping.global.auth.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/*
 * CORS 설정을 위한 filter 설정 클래스
 * */
public class HeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        res.setHeader("Access-Control-Allow-Origin", "*");                        // 다른 외부 요청의 응답을 허용할 것인가?
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");  // 외부 요청에 허용할 메소드
        res.setHeader("Access-Control-Max-Age", "3600");                          // 캐싱을 허용할 시간
        res.setHeader("Access-Control-Allow-Headers",
                "X-Requested-With, Content-Type, Authorization, X-XSRF-token");     // 요청 시에 허용할 header 타입
        res.setHeader("Access-Control-Allow-Credentials", "false");               // 자격 증명을 허용할 것인가?

        filterChain.doFilter(servletRequest, res);
    }
}