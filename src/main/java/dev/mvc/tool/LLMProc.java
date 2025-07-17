package dev.mvc.tool;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * ✅ LLMProc
 * - Spring Boot → FastAPI API 호출 전담
 */
@Component
public class LLMProc {

    @Autowired
    private RestTemplate restTemplate;

    // ✅ FastAPI 서버 URL
    private static final String FASTAPI_REVIEW_ANALYSIS_URL = "http://localhost:8000/review/analysis";

    // ✅ API Key
    private static final String FASTAPI_KEY = "YOUR_FASTAPI_KEY"; // .env와 동일
    
    /**
     * 리뷰 감정 분석 & 요약 요청
     * @param text 리뷰 본문
     * @return Map<String, Object> (emotion, summary)
     */
    public Map<String, Object> analyzeReview(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("SpringBoot_FastAPI_KEY", new LLMKey().getSpringBoot_FastAPI_KEY());
        body.put("text", text);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                FASTAPI_REVIEW_ANALYSIS_URL,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        return response.getBody();
    }
}
