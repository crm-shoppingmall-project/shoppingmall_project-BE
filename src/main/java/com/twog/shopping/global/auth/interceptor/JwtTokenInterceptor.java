package com.twog.shopping.global.auth.interceptor;

import com.twog.shopping.global.common.AuthConstants;
import com.twog.shopping.global.common.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.rmi.RemoteException;


/*
 * 핸들러 인터셉터 구현체: 요청이 컨트롤러에 도달하기 전에 토큰의 유효성을 검증한다.
 * */

public class JwtTokenInterceptor implements HandlerInterceptor {

    // preHandle : 컨트롤러(핸들러)가 실행되기 전에 요청을 가로채서 실행되는 메소드
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 요청 헤더에서 Authorization 값을 가져온다.
        String header = request.getHeader(AuthConstants.AUTH_HEADER);

        // 실제 토큰값만 추출
        String token = TokenUtils.splitHeader(header);

        if (token != null) {
            // TokenUtils 클래스의 isValidToken 메소드를 이용하여 토큰이 유효한지 확인
            if (TokenUtils.isValidToken(token)) {
                return true;
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 만료되었거나 유효하지 않습니다.");
                return false;
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰 정보가 없습니다.");
            return false; // 진행 중단
        }
    }
}