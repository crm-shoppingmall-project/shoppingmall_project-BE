package com.twog.shopping.global.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String result = "success";
    private String code = "200";
    private T data;
    private String message;

    public static <T> ApiResponse<T> of(String message, T data) {
        return new ApiResponse<>( "success", "200", data, message);
    }

    public static <T> ApiResponse<T> of(String message) {
        return new ApiResponse<>("success", "200", null, message);
    }
}
