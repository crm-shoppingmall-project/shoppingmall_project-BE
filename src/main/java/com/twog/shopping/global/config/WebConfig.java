package com.twog.shopping.global.config;


import com.twog.shopping.global.auth.filter.HeaderFilter;
import com.twog.shopping.global.auth.interceptor.JwtTokenInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 * Spring Web MVC 설정을 위한 클래스
 * - 정적 리소스 관리, CORS, 인터셉터 등 웹 관련 설정을 정의한다.
 * */
@Configuration
@EnableWebMvc

public class WebConfig implements WebMvcConfigurer {

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
            "classpath:/static/", "classpath:/public/", "classpath:/", "classpath:/resources/",
            "classpath:/META-INF/resources/", "classpath:/META-INF/resources/webjars/"
    };

    // 정적 자원에 대한 요청 허용을 위한 메소드 (별도의 인증이나 필터를 거치지 않고 클라이언트가 자원에 직접 접근 허용)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);

    }

    @Bean
    public FilterRegistrationBean<HeaderFilter> getFilterRegistrationBean() {

        FilterRegistrationBean<HeaderFilter> registrationBean = new FilterRegistrationBean<HeaderFilter>(createHeaderFilter());

        // 이 필터의 우선순위를 가장 높게 설정
        registrationBean.setOrder(Integer.MIN_VALUE);

        // 모든 요청 URL 패턴("/*")에 대해 이 필터가 동작하도록 설정
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }


    @Bean
    public HeaderFilter createHeaderFilter() {
        return new HeaderFilter();
    }


    @Bean
    public JwtTokenInterceptor jwtTokenInterceptor() {
        return new JwtTokenInterceptor();
    }


}