package com.twog.shopping.global.auth.filter;



import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.service.DetailsUser;
import com.twog.shopping.global.common.AuthConstants;
import com.twog.shopping.global.common.utils.TokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        // 들어오는 요청 중 인증은 했지만 권한이 필요없는 리소스 목록을 만들어서
        List<String> roleLessList = Arrays.asList("/signup");

        // 권한이 필요 없는 애면 다음 내용 동작하게 한다.
        if (roleLessList.contains((request.getRequestURI()))) {
            chain.doFilter(request, response);
            return;
        }

        // 요청이 들어오면 Authorization 헤더에서 토큰 추출
        String header = request.getHeader(AuthConstants.AUTH_HEADER);

        try {
            if (header != null && !header.equalsIgnoreCase("")) {
                String token = TokenUtils.splitHeader(header);

                // 토큰의 유효성을 검사
                if (TokenUtils.isValidToken(token)) {
                    // 복호화
                    Claims claims = TokenUtils.getClaimsFromToken(token);


                    DetailsUser authentication = new DetailsUser();
                    Member member = new Member();
                    user.setUserName(claims.get("userName").toString());
                    user.setRole(UserRole.valueOf(claims.get("Role").toString()));
                    authentication.setUser(user);
                    // 사용자의 ID, 권한 등의 정보를 UserDetails(DetailsUser)로 감싸
                    // UsernamePasswordAuthenticationToken을 생성
                    AbstractAuthenticationToken authenticationToken
                            = UsernamePasswordAuthenticationToken
                            .authenticated(authentication, token, authentication.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetails(request));

                    // SecurityContextHolder에 인증 정보를 저장하여 이후 인증이 필요한 요청에서 사용 가능하게 함
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    chain.doFilter(request, response);
                } else {
                    throw new RuntimeException("token이 유효하지 않습니다.");
                }
            } else {
                throw new RuntimeException("token이 존재하지 않습니다.");
            }
        } catch (Exception e) {
            response.setContentType("application/json");
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