package com.twog.shopping.global.config;

package com.ohgiraffers.jwtsecurity.auth.config;

import com.ohgiraffers.jwtsecurity.auth.filter.CustomAuthenticationFilter;
import com.ohgiraffers.jwtsecurity.auth.filter.JwtAuthorizationFilter;
import com.ohgiraffers.jwtsecurity.auth.handler.CustomAuthFailureHandler;
import com.ohgiraffers.jwtsecurity.auth.handler.CustomAuthSuccessHandler;
import com.ohgiraffers.jwtsecurity.auth.handler.CustomAuthenticationProvider;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/*
 *  Spring Security 핵심 설정 파일
 * 인증, 인가, 필터 설정 등을 포함하고 있다.
 * */
@Configuration
// @EnableMethodSecurity(securedEnabled = true) 어노테이션을 통해서
// 컨트롤러에서 @PreAuthorize 또는 @PostAuthorize를 사용할 수 있다(메서드 단위 접근 제한 제어 가능)
@EnableMethodSecurity(securedEnabled = true)
public class WebSecurityConfig {

    /**
     * description. 정적 자원에 대한 인증된 사용자의 접근을 설정하는 메소드
     *
     * @return WebSecurityCustomizer
     */

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    /**
     * description. Security filter chain 설정 메소드
     *
     * @param http : HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception
     */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // CSRF 비활성화 : 세션이 없기때문에 CSRF 보호 기능을 꺼야함
        http.csrf(AbstractHttpConfigurer::disable)

                // URL 권한 설정
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/api/v1/user/**").hasAnyRole("USER", "ADMIN")
//                        .requestMatchers("/api/v1/public/**").permitAll()
//                        .anyRequest().authenticated()
//                )

                // 필터 추가
                // jwtAuthorizationFilter : 토큰 인증 후 사용자 인증 정보를 세팅해주는 역할
                .addFilterBefore(jwtAuthorizationFilter(), BasicAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 시큐리티가 제공하는 기본 로그인창 쓰지 않겠다.
                .formLogin(form -> form.disable())
                // 시큐리티가 제공하는 기본인증 쓰지 않겠다. -> 우리가 지정해준 인증절차로 처리되게끔
                .httpBasic(basic -> basic.disable())
                // customAuthenticationFilter : 로그인 요청 시 토큰을 생성해서 반환
                .addFilterBefore(customAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * description. 사용자 요청(request) 시 수행되는 메소드 (토큰 검증)
     *
     * @return JwtAuthorizationFilter
     */

    private JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(authenticationManager());
    }

    /**
     * description. Authentization의 인증 메소드를 제공하는 매니저(= Provider의 인터페이스)를 반환하는 메소드
     *
     * @return AuthenticationManager
     */

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(customAuthenticationProvider());
    }

    /**
     * description. 사용자의 id와 password를 DB와 비교하여 검증하는 핸들러 메소드
     *
     * @return CustomAuthenticationProvider
     */

    @Bean
    public CustomAuthenticationProvider customAuthenticationProvider() {
        return new CustomAuthenticationProvider();
    }

    /**
     * description. 비밀번호를 암호화하는 인코더를 반환하는 메소드
     *
     * @return BCryptPasswordEncoder
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * description. 사용자의 인증 요청을 가로채서 로그인 로직을 수행하는 필터를 반환하는 메소드
     *
     * @return CustomAuthenticationFilter
     */
    public CustomAuthenticationFilter customAuthenticationFilter() {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManager());

        // 로그인 요청을 처리할 URL 설정
        customAuthenticationFilter.setFilterProcessesUrl("/login");

        // 핸들러 등록
        customAuthenticationFilter.setAuthenticationSuccessHandler(customAuthLoginSuccessHandler());
        customAuthenticationFilter.setAuthenticationFailureHandler(customAuthLoginFailureHandler());
        customAuthenticationFilter.afterPropertiesSet();
        return customAuthenticationFilter;
    }

    /**
     * description. 사용자 정보가 맞을 경우 (= 로그인 성공 시) 수행하는 핸들러를 반환하는 메소드
     *
     * @return CustomAuthSuccessHandler
     */
    private CustomAuthSuccessHandler customAuthLoginSuccessHandler() {
        return new CustomAuthSuccessHandler();
    }

    /**
     * description. 사용자 정보가 맞지 않는 경우 (= 로그인 실패 시) 수행하는 핸들러를 반환하는 메소드
     *
     * @return CustomAuthFailureHandler
     */
    private CustomAuthFailureHandler customAuthLoginFailureHandler() {
        return new CustomAuthFailureHandler();
    }

}
