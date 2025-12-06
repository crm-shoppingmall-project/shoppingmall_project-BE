package com.twog.shopping.global.auth.filter;


import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.entity.MemberStatus;
import com.twog.shopping.domain.member.entity.UserRole;
import com.twog.shopping.domain.member.service.DetailsUser;
import com.twog.shopping.domain.member.service.MemberService;
import com.twog.shopping.global.common.AuthConstants;
import com.twog.shopping.global.common.utils.TokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/* 클라이언트가 전송한 JWT 토큰 검증하는 역할 */

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final MemberService memberService;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, MemberService memberService) {
        super(authenticationManager);
        this.memberService = memberService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {


        String uri = request.getRequestURI();

        // 0. Swagger + 회원가입/로그인 은 토큰 검사 아예 안 함
        if (uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.equals("/swagger-ui.html")
                || uri.equals("/api/v1/members/signup")
                || uri.equals("/api/v1/members/login")) {

            chain.doFilter(request, response);
            return;
        }

        // 1. 헤더에서 토큰 꺼내기
        String header = request.getHeader(AuthConstants.AUTH_HEADER);

        // [핵심 수정] 토큰이 없거나, Bearer 타입이 아니면? -> 그냥 통과시킨다.
        // 이유: 로그인(/login)이나 회원가입(/signup) 요청은 토큰이 없으므로 여기서 막으면 안 됩니다.
        // 여기서 통과되어도, WebSecurityConfig에서 인증이 필요한 페이지라면 알아서 막아줍니다.
        if (header == null || !header.startsWith(AuthConstants.TOKEN_TYPE)) {
            chain.doFilter(request, response);
            return;
        }


        try {
            // 2. 토큰이 있다면 유효성 검사 시작
            String token = TokenUtils.splitHeader(header);


            if (TokenUtils.isValidToken(token)) {
                Claims claims = TokenUtils.getClaimsFromToken(token);


                // 1) JWT에 넣었던 클레임에서 값 꺼내기
                String email = claims.get("email", String.class);
//                String role = claims.get("role", String.class);   // 예: "USER", "ADMIN"

//                Member member = Member.builder()
//                        .memberEmail(email)
//                        .memberRole(UserRole.valueOf(role))  // enum 변환
//                        .build();

                Member user = memberService.getByEmailOrThrow(email);

                if(user.getMemberStatus() == MemberStatus.withdrawn){
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                    return;
                }

                DetailsUser principal = new DetailsUser(user);

                AbstractAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                principal,          // principal
                                null,               // credentials (보통 null)
                                principal.getAuthorities()  // 권한
                        );

                authenticationToken.setDetails(new WebAuthenticationDetails(request));

                // 5) SecurityContext에 저장
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                chain.doFilter(request, response);

            } else {
                throw new RuntimeException("token이 유효하지 않습니다.");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter printWriter = response.getWriter();
            JSONObject jsonObject = jsonResponseWrapper(e);
            printWriter.print(jsonObject);  // 내보내기
            printWriter.flush();
            printWriter.close();
        }
    }

    /**
     * description. 토큰 관련 Exception 발생 시 예외 내용을 담은 객체 반환하는 메소드
     *
     * @param e : Exception
     * @return JSONObject
     */
    private JSONObject jsonResponseWrapper(Exception e) {
        String resultMsg = "";

        if (e instanceof ExpiredJwtException) {
            resultMsg = "Token Expired";
        } else if (e instanceof SignatureException) {
            resultMsg = "Token SignatureException";
        } else if (e instanceof JwtException) {
            resultMsg = "Token Parsing JwtException";
        } else {
            resultMsg = "other Token error";
        }

        HashMap<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("status", 401);
        jsonMap.put("message", resultMsg);
        jsonMap.put("reason", e.getMessage());

        return new JSONObject(jsonMap);
    }
}