package com.twog.shopping.global.auth.handler;

import com.ohgiraffers.jwtsecurity.auth.model.DetailsUser;
import com.ohgiraffers.jwtsecurity.common.AuthConstants;
import com.ohgiraffers.jwtsecurity.common.utils.ConvertUtil;
import com.ohgiraffers.jwtsecurity.common.utils.TokenUtils;
import com.ohgiraffers.jwtsecurity.user.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

/*
 * 로그인 성공 시 동작하는 핸들러 (JWT 토큰 발급 및 응답 처리)
 * */
@Configuration
public class CustomAuthSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {

        // 1. 요청 사용자 정보 가져오기
        User user = ((DetailsUser) authentication.getPrincipal()).getUser();

        // 2. User 엔티티를 JSON 객체로 변환
        JSONObject jsonValue = (JSONObject) ConvertUtil.convertObjectToJsonObject(user);

        // 3. 응답 데이터 구성
        HashMap<String, Object> responseMap = new HashMap<>();

        JSONObject jsonObject;
        if ("N".equals(user.getState())) {
            // 휴면 계정인 경우 토큰을 발급하지 않고 메시지만 전달
            responseMap.put("userInfo", jsonValue);
            responseMap.put("message", "휴면 상태의 계정입니다.");
        } else {
            // 정상 계정인 경우 JWT 토큰 생성
            String token = TokenUtils.generateJwtToken(user);
            responseMap.put("userInfo", jsonValue);
            responseMap.put("message", "로그인 성공입니다.");

            response.addHeader(AuthConstants.AUTH_HEADER, AuthConstants.TOKEN_TYPE + " " + token);
        }

        // 4. 클라이언트로 응답 전송
        // 한글 깨짐 방지를 위해 인코딩 설정 필수!
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        // JSON 객체로 변환하여 출력
        jsonObject = new JSONObject(responseMap);

        PrintWriter printWriter = response.getWriter();
        printWriter.print(jsonObject);
        printWriter.flush();
        printWriter.close();
    }
}