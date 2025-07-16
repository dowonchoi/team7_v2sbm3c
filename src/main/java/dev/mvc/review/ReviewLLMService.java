package dev.mvc.review;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 리뷰 감정분석 및 요약 FastAPI 연동 서비스
 */
@Service
public class ReviewLLMService {

  private final String BASE_URL = "http://localhost:8000"; // FastAPI 서버 주소
  private final String API_KEY = "team7-llm-secret-key";   // .env에 저장된 키와 동일하게 설정

  /**
   * 감정 분석 호출
   * @param content 리뷰 내용
   * @return 1(긍정), 0(부정), -1(에러)
   */
  public int analyzeEmotion(String content) {
    try {
      RestTemplate restTemplate = new RestTemplate();

      // 요청 헤더 설정
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      // 요청 바디 구성
      Map<String, String> body = new HashMap<>();
      body.put("SpringBoot_FastAPI_KEY", API_KEY);
      body.put("content", content);

      // 요청 전송
      HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
      ResponseEntity<Map> response = restTemplate.exchange(BASE_URL + "/emotion", HttpMethod.POST, request, Map.class);

      // 응답 파싱
      if (response.getBody() != null && response.getBody().get("res") != null) {
        return (Integer) response.getBody().get("res");
      }
    } catch (Exception e) {
      System.err.println("[ERROR] 감정 분석 실패: " + e.getMessage());
    }
    return -1; // 실패 시 기본값
  }

  /**
   * 요약 생성 호출
   * @param content 리뷰 내용
   * @return 요약된 문장, 실패 시 "요약 실패"
   */
  public String summarizeContent(String content) {
    try {
      RestTemplate restTemplate = new RestTemplate();

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      Map<String, String> body = new HashMap<>();
      body.put("SpringBoot_FastAPI_KEY", API_KEY);
      body.put("content", content);

      HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
      ResponseEntity<Map> response = restTemplate.exchange(BASE_URL + "/summary", HttpMethod.POST, request, Map.class);

      if (response.getBody() != null && response.getBody().get("res") != null) {
        return (String) response.getBody().get("res");
      }
    } catch (Exception e) {
      System.err.println("[ERROR] 요약 실패: " + e.getMessage());
    }
    return "요약 실패";
  }
  
  /**
   * 감정 분석 및 요약 결과를 ReviewVO에 반영
   * @param reviewVO 리뷰 객체
   */
  public void process(ReviewVO reviewVO) {
    try {
      String content = reviewVO.getContent();

      // 감정 분석 호출
      int emotion = this.analyzeEmotion(content);
      reviewVO.setEmotion(emotion);  // 1 또는 0, 실패 시 -1

      // 요약 호출
      String summary = this.summarizeContent(content);
      reviewVO.setSummary(summary);

    } catch (Exception e) {
      System.err.println("[ERROR] 감정 분석 또는 요약 실패: " + e.getMessage());
    }
  }

}
