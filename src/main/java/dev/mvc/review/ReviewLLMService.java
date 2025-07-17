package dev.mvc.review;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dev.mvc.tool.LLMKey;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReviewLLMService {

    @Autowired
    private RestTemplate restTemplate;

    private final String FASTAPI_URL_EMOTION = "http://localhost:8000/emotion";
    private final String FASTAPI_URL_SUMMARY = "http://localhost:8000/summary";

    /**
     * 리뷰 본문(content)을 FastAPI에 전달 → 감정 + 요약 결과를 ReviewVO에 세팅
     */
    public void process(ReviewVO reviewVO) {
        String content = reviewVO.getContent();

        // ✅ 감정 분석 호출
        String emotionResponse = callFastAPI(FASTAPI_URL_EMOTION, content);
        int emotionValue = parseEmotion(emotionResponse);
        reviewVO.setEmotion(emotionValue); // 0=부정, 1=긍정

        // ✅ 요약 호출
        String summaryResponse = callFastAPI(FASTAPI_URL_SUMMARY, content);
        String summary = parseSummary(summaryResponse);
        if (summary.length() > 500) {
            summary = summary.substring(0, 500);
        }
        reviewVO.setSummary(summary);

        System.out.println("▶ [ReviewLLMService] emotion=" + emotionValue + ", summary=" + summary);
    }

    /**
     * FastAPI 호출 공통 메서드
     */
    private String callFastAPI(String url, String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("SpringBoot_FastAPI_KEY", new LLMKey().getSpringBoot_FastAPI_KEY());
        body.put("content", content);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String response = restTemplate.postForObject(url, requestEntity, String.class);
        System.out.println("FastAPI Response (" + url + "): " + response);
        return response;
    }

    /**
     * ✅ 감정 분석 응답 파싱
     * 예시: {"res":1}
     */
    private int parseEmotion(String jsonResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);
            return root.get("res").asInt(); // FastAPI가 반환하는 숫자 그대로 사용
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // 기본값: 부정
        }
    }

    /**
     * ✅ 요약 응답 파싱
     * 예시: {"res":"요약 내용"}
     */
    private String parseSummary(String jsonResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);
            return root.get("res").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
