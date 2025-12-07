package com.twog.shopping.global.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public ModelMapper modelMapper() {

        ModelMapper modelMapper = new ModelMapper();
        /* setter 메소드 미사용 시 ModelMapper가 private 필드에 접근할 수 있도록 설정 */
        modelMapper.getConfiguration()
                .setFieldAccessLevel(
                        org.modelmapper.config.Configuration.AccessLevel.PRIVATE
                )
                .setFieldMatchingEnabled(true);

        return modelMapper;
    }
}
