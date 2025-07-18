package dev.mvc.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dev.mvc.tool.LLMKey;

import java.util.*;

@Service
public class RecipeLLMService {

    @Autowired
    private RestTemplate restTemplate;

    private final String FASTAPI_URL_FOOD = "http://localhost:8000/food";

    /**
     * 식재료 선택 값(food 배열)을 FastAPI로 전달 → 추천 레시피 결과(JSON) 반환
     */
    public List<Map<String, String>> getRecipes(String foodBinary) {
        try {
            // ✅ HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // ✅ 요청 Body 생성
            Map<String, Object> body = new HashMap<>();
            body.put("SpringBoot_FastAPI_KEY", new LLMKey().getSpringBoot_FastAPI_KEY());
            body.put("food", foodBinary); // "0,1,0,1,..." 형태

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // ✅ FastAPI 호출
            String response = restTemplate.postForObject(FASTAPI_URL_FOOD, requestEntity, String.class);
            System.out.println("[RecipeLLMService] FastAPI Response: " + response);

            // ✅ JSON 파싱 → recipes 리스트 반환
            return parseRecipes(response);

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * JSON 응답 파싱
     * 예시:
     * {
     *   "recipes": [
     *       {"name": "된장찌개", "desc": "된장과 채소로 끓이는 찌개"},
     *       {"name": "김치찌개", "desc": "김치와 돼지고기로 만든 찌개"}
     *   ]
     * }
     */
    private List<Map<String, String>> parseRecipes(String jsonResponse) {
        List<Map<String, String>> result = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);
            JsonNode recipes = root.get("recipes");

            if (recipes != null && recipes.isArray()) {
                for (JsonNode recipe : recipes) {
                    Map<String, String> map = new HashMap<>();
                    map.put("name", recipe.get("name").asText());
                    map.put("desc", recipe.get("desc").asText());
                    result.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
