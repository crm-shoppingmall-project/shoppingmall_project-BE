package com.twog.shopping.global.common.utils;

import com.ohgiraffers.jwtsecurity.user.entity.User;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
 * 토큰을 관리하기 위한 Utils 모음 클래스
 * - JWT 생성, 분해(파싱), 유효성 검증 등을 담당한다.
 * */

@Component
public class TokenUtils {

    private static String jwtSecretKey;
    private static Long tokenValidateTime;

    @Value("${jwt.key}")
    public void setJwtSecretKey(String jwtSecretKey) {
        TokenUtils.jwtSecretKey = jwtSecretKey;
    }

    @Value("${jwt.time}")
    public void setTokenValidateTime(Long tokenValidateTime) {
        TokenUtils.tokenValidateTime = tokenValidateTime;
    }

    /**
     * description. header의 token을 분리하는 메소드
     *
     * @param header (Authrization의 header값)
     * @return String (Authrization의 token 부분)
     */

    public static String splitHeader(String header) {

        if (!header.equals("")) {
            // 빈 문자열이 아니더라도 "Bearer "로 시작하지 않거나 형식이 안 맞을 수 있음
            // 안전하게 공백으로 나눈 뒤 배열 길이를 체크
            String[] split = header.split(" ");

            // "Bearer"와 "토큰값" 2개로 잘 나뉘었는지 확인
            if (split.length > 1) {
                return split[1];
            }
        }
        // 헤더가 없거나 형식이 올바르지 않으면 null 반환
        return null;
    }


    /**
     * description. 토큰이 유효한지 확인하는 메소드
     * - 복호화가 정상적으로 진행되면 유효한 토큰으로 간주
     *
     * @param token 분리된 토큰 값
     * @return boolean : 유효 판단 여부
     */

    public static boolean isValidToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return true;

        } catch (ExpiredJwtException e) {
            e.printStackTrace();
            return false;

        } catch (JwtException e) {
            e.printStackTrace();
            return false;

        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * description. 토큰을 복호화 하는 메소드
     *
     * @param token
     * @return Claims
     */

    // Claims 는 payload에 담긴 데이터를 의미한다.
    public static Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(Base64.getDecoder().decode(jwtSecretKey))
                .parseClaimsJws(token)  // payload, header, signature로 분리
                .getBody(); // payload에 해당하는 데이터를 Claims라는 작은 데이터 단위로 반환 받는다.
    }


    /**
     * description. 토큰을 생성하는 메소드
     *
     * @param user
     * @return token (String)
     */
    public static String generateJwtToken(User user) {
        Date expireTime = new Date(System.currentTimeMillis() + tokenValidateTime);

        JwtBuilder builder = Jwts.builder()
                .setHeader(createHeader())      // 헤더 세팅
                .setClaims(createClaims(user))  // user 데이터를 넣는 payload 세팅
                .setSubject("ohgiraffers token : " + user.getUserNo())  // 토큰 제목 설정
                .signWith(SignatureAlgorithm.HS256, createSignature())  // 토큰 생성을 위해 암호화 설정
                .setExpiration(expireTime);


        return builder.compact();
    }

    /**
     * description. 토큰의 header를 설정하는 메소드
     *
     * @return Map<String, Object> (header의 설정 정보)
     */
    private static Map<String, Object> createHeader() {
        Map<String, Object> header = new HashMap<>();

        header.put("type", "jwt");  // 타입이 json web token
        header.put("alg", "HS256"); // 알고리즘으로 암호화 방식 설정
        header.put("date", System.currentTimeMillis()); // 만들어진 시간 정보

        return header;
    }

    /**
     * description. 사용자 정보를 기반으로 claim을 생성하는 메소드
     *
     * @param user (사용자 정보)
     * @return Map<String, Object> (claims 정보)
     */
    private static Map<String, Object> createClaims(User user) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("userName", user.getUserName()); // 이름과
        claims.put("Role", user.getRole());         // 권한 정보로 claim 생성

        return claims;
    }

    /**
     * description. JWT 서명을 발급하는 메소드
     *
     * @return Key : SecretKeySpec
     */
    private static Key createSignature() {
        byte[] secretBytes = Base64.getDecoder().decode(jwtSecretKey);
        return new SecretKeySpec(secretBytes, SignatureAlgorithm.HS256.getJcaName());
    }
}