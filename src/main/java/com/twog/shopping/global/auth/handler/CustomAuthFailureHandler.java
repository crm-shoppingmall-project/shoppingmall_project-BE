package com.twog.shopping.global.auth.handler;

import com.twog.shopping.domain.log.entity.HistoryActionType;
import com.twog.shopping.domain.log.entity.HistoryRefTable;
import com.twog.shopping.domain.log.service.HistoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;


/**
 * 사용자 로그인 실패 시 실패 요청을 커스텀 하기 위한 핸들러 클래스
 */
/*
 *  로그인 실패 시 클라이언트에게 실패 원인을 JSON으로 응답하는 핸들러
 * */

@Component
@RequiredArgsConstructor
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {

    private final HistoryService historyService;

    /**
     * 사용자의 잘못된 로그인 시도를 커스텀 하기 위한 핸들러 메소드
     *
     * @param request   사용자 요청 개체
     * @param response  서버 응답값
     * @param exception 발생한 오류를 담는 개체
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        JSONObject jsonObject;
        String failMsg;

        if (exception instanceof AuthenticationServiceException) {
            // 사용자의 로그인 또는 인증 처리 과정에서 문제가 발생한다.
            failMsg = "존재하지 않는 사용자입니다.";

        } else if (exception instanceof BadCredentialsException) {
            // BadCredentialsException 오류는 사용자의 아이디가 DB에 존재하지 않는 경우 비밀번호가 맞지 않는 경우 발생한다.
            failMsg = "아이디 또는 비밀번호가 틀립니다.";

        } else if (exception instanceof LockedException) {
            // 계정이 잠긴 경우 발생한다.
            failMsg = "잠긴 계정입니다. 관리자에게 문의하세요.";

        } else if (exception instanceof DisabledException) {
            // 비활성화 된 계정에서 발생한다.
            failMsg = "비활성화된 계정입니다. 관리자에게 문의하세요.";

        } else if (exception instanceof AccountExpiredException) {
            // 계정 만료시 발생한다.
            failMsg = "만료된 계정입니다. 관리자에게 문의하세요.";

        } else if (exception instanceof CredentialsExpiredException) {
            // 자격 증명이 만료된 경우 발생한다.
            failMsg = "비밀번호가 만료되었습니다.";

        } else if (exception instanceof AuthenticationCredentialsNotFoundException) {
            // 보안 컨텍스트에 인증 객체가 존재하지 않거나 인증 정보가 없는 상태에서 보안처리 된 리소스에 접근하면 발생한다.
            failMsg = "인증 요청이 거부되었습니다.";

        } else if (exception instanceof UsernameNotFoundException) {
            // db에 사용자의 정보가 없는 경우 발생한다.
            failMsg = "존재하지 않는 이메일 입니다.";

        } else {
            failMsg = "정의되지 않은 케이스의 오류입니다. 관리자에게 문의해 주세요.";
        }

        String email = request.getParameter("email");
        Map<String, Object> detail = Map.of(
                "email",email,
                "failMessage",failMsg,
                "exception",exception.getClass().getSimpleName()
        );





        historyService.saveHistory(
                null,
                HistoryActionType.LOGIN_FAIL,
                detail,
                request,
                null,
                null




        );

        // map타입으로 만들어서 스트림 내보내기를 해준 내용이다.
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("failType", failMsg);

        jsonObject = new JSONObject(resultMap);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println(jsonObject);
        out.flush();
        out.close();
    }
}