package com.twog.shopping.global.common.utils;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import tools.jackson.databind.ObjectMapper;

/*
 * Java 객체를 JSON 객체로 변환하는 유틸 클래스
 *
 * Jackson (ObjectMapper): 복잡한 자바 객체(User 등)를 문자열(String)로 바꿔줌
 * JSON.simple (JSONParser): 변환된 문자열을 다시 조작하기 쉬운 JSON 객체로 만드는 데 사용
 * */
public class ConvertUtil {

    public static Object convertObjectToJsonObject(Object obj) {

        // Spring Boot Web에 기본 내장된 Jackson 라이브러리
        ObjectMapper mapper = new ObjectMapper();

        // 별도로 추가한 JSON.simple 라이브러리
        JSONParser parser = new JSONParser();

        try {
            // 1. Java 객체(Entity) -> JSON 문자열(String)로 변환
            String JsonString = mapper.writeValueAsString(obj);

            // 2. JSON 문자열 -> JSON.simple의 JSONObject로 파싱
            return parser.parse(JsonString);

        } catch ( ParseException e) {
            // 변환 중 에러가 발생하면 런타임 예외로 던져서 흐름을 중단시킴
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }

}