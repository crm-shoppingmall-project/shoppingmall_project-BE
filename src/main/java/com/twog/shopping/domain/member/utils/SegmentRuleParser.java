package com.twog.shopping.domain.member.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SegmentRuleParser {

    private final ObjectMapper objectMapper;

    public SegmentRule parse(String json) {
        try {
            return objectMapper.readValue(json, SegmentRule.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("세그먼트 룰 JSON 파싱 실패: " + json, e );
        }
    }
}