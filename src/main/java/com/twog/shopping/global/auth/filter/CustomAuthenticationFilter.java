package com.twog.shopping.global.auth.filter;

import com.twog.shopping.domain.member.dto.request.LoginRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }

    /**
     * description. 지정된 url(기본적으로 /login) 요청 시 해당 요청을 가로채서 검증 로직을 수행하는 메소드
     *
     * @param request  : HttpServletRequest
     * @param response : HttpServletResponse
     * @return Authentication
     * @throws AuthenticationException
     */

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        UsernamePasswordAuthenticationToken authRequest;

        try {
            authRequest = getAuthRequest(request);

            setDetails(request, authRequest);// 부가 정보(IP 등) 설정


            return this.getAuthenticationManager().authenticate(authRequest);

        } catch (IOException e) {
            throw new AuthenticationServiceException("로그인 요청 처리 중 입출력 오류 발생", e);
        }
    }

    /**
     * description. 사용자의 로그인 요청 시 요청 정보를 임시 토큰에 저장하는 메소드
     *
     * @param request : HttpServletRequest
     * @return UsernamePasswordAuthenticationToken
     * @throws IOException
     */

    private UsernamePasswordAuthenticationToken getAuthRequest(HttpServletRequest request) throws IOException {

        // JsonParser에 대한 설정으로, 리소스에 대해 우리가 판단할 수 있는 리소스가 아니면 자동으로 close
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);

        // request의 input stream에서 JSON 데이터를 읽어 LoginDTO로 반환
        // 즉, 로그인 요청을 받을때 정보를 dto로 받게끔
        // LoginDTO필드명과 프론트엔드에서 보내는 JSON 키값이 일치해야 한다. (LoginDTO에 Getter도 반드시 있어햐 함)
        LoginRequestDTO member = objectMapper.readValue(request.getInputStream(), LoginRequestDTO.class);

        // id와 pwd 기반으로 아직 인증되지 않은 임시 토큰 생성
        return new UsernamePasswordAuthenticationToken(member.getMemberEmail(), member.getMemberPwd());
    }
}