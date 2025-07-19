package dev.mvc.openai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.mvc.member.MemberProcInter;
import dev.mvc.tool.LLMKey;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/openai")
public class OpenAICont {
  
  @Autowired
  @Qualifier("dev.mvc.openai.MemberImgProc")
  private MemberImgProcInter memberImgProc;

    @Autowired
    @Qualifier("dev.mvc.openai.RecipeProc")
    private RecipeProcInter recipeProc;
  
    @Autowired
    @Qualifier("dev.mvc.member.MemberProc")
    private MemberProcInter memberProc;

    private final RestTemplate restTemplate;

    // FastAPI URL
    private static final String FASTAPI_FOOD_URL = "http://localhost:8000/food";

    private static final String FASTAPI_MEMBER_IMG_URL = "http://localhost:8000/member_img";

    
    @Autowired
    public OpenAICont(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        System.out.println("-> OpenAICont initialized. RestTemplate hashCode: " + this.restTemplate.hashCode());
    }

    /**
     * ✅ 레시피 추천 페이지 (식재료 선택 UI 출력)
     */
    @GetMapping("/recipe")
    public String recipePage(HttpSession session, Model model) {
        Integer memberno = (Integer) session.getAttribute("memberno");
        if (memberno == null) {
            return "redirect:/member/login_cookie_need?url=/openai/recipe";
        }

        // ✅ 식재료 목록 전달 (FastAPI와 동일한 순서)
        String[] foods = {
                "배추", "무", "대파", "양파", "오이",
                "사과", "배", "귤", "바나나", "딸기",
                "고등어", "갈치", "새우", "홍합", "오징어",
                "소고기", "돼지고기", "닭가슴살", "닭날개", "닭다리",
                "우유", "달걀", "치즈", "버터", "두부"
        };
        model.addAttribute("foods", foods);
        
        // 추가: 사용자 레시피 내역
        model.addAttribute("recipeList", recipeProc.list_by_member(memberno));

        return "openai/recipe"; // /templates/openai/recipe.html
    }

    /**
     * ✅ AJAX 요청 → FastAPI 호출 → 레시피 추천 결과 반환
     */
    @PostMapping("/recipe_ajax")
    @ResponseBody
    public Map<String, Object> recommendRecipe(@RequestParam("food") String foodBinary, 
                                                                   HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // ✅ 로그인 체크
        Integer memberno = (Integer) session.getAttribute("memberno");
        if (memberno == null) {
            result.put("error", "로그인이 필요합니다.");
            return result;
        }

        try {
            // ✅ FastAPI 요청 준비
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("SpringBoot_FastAPI_KEY", new LLMKey().getSpringBoot_FastAPI_KEY());
            body.put("food", foodBinary); // 예: "0,1,0,0,1..."

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // ✅ FastAPI 호출
            String response = restTemplate.postForObject(FASTAPI_FOOD_URL, requestEntity, String.class);
            System.out.println("[OpenAICont] FastAPI Response: " + response);
            
            // ✅ DB 저장
            RecipeVO vo = new RecipeVO();
            vo.setMemberno(memberno);
            vo.setFoodBinary(foodBinary);
            vo.setContent(response);
            recipeProc.create(vo);

            // ✅ JSON 그대로 반환 (프론트에서 처리)
            result.put("success", true);
            result.put("response", response);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "FastAPI 호출 중 오류 발생: " + e.getMessage());
        }

        return result;
    }
    
    @GetMapping("/member_img")
    public String memberImgPage(HttpSession session, Model model) {
        String grade = (String) session.getAttribute("grade");
        if (grade == null || (!"admin".equals(grade) && !"supplier".equals(grade))) {
            return "redirect:/member/login_cookie_need?url=/openai/member_img";
        }

        Integer memberno = (Integer) session.getAttribute("memberno");
        if (memberno != null) {
            model.addAttribute("imgList", memberImgProc.list_by_member(memberno));
        }

        return "openai/member_img";
    }

    @PostMapping("/member_img_ajax")
    @ResponseBody
    public Map<String, Object> createMemberImage(@RequestParam("prompt") String prompt, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        String grade = (String) session.getAttribute("grade");
        Integer memberno = (Integer) session.getAttribute("memberno");

        if (grade == null || (!"admin".equals(grade) && !"supplier".equals(grade))) {
            result.put("success", false);
            result.put("error", "권한이 없습니다.");
            return result;
        }

        String fileName = ""; // ✅ try 바깥에서 선언
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("SpringBoot_FastAPI_KEY", new LLMKey().getSpringBoot_FastAPI_KEY());
            body.put("prompt", prompt);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // ✅ FastAPI 호출
            String response = restTemplate.postForObject(FASTAPI_MEMBER_IMG_URL, requestEntity, String.class);
            System.out.println("[OpenAICont] FastAPI Response: " + response);

            // ✅ JSON 파싱
            JSONObject json = new JSONObject(response);
            String fullPath = json.getString("file_name"); // "C:/kd/deploy/team/member_img/storage/xxx.jpg"

            // ✅ 파일명만 추출
            fileName = fullPath.substring(fullPath.lastIndexOf("/") + 1);
            if (fileName.contains("\\")) {
                fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
            }
            System.out.println("-> Extracted fileName: " + fileName);

            // ✅ DB 저장
            MemberImgVO vo = new MemberImgVO();
            vo.setMemberno(memberno);
            vo.setPrompt(prompt);
            vo.setFilename(fileName);
            memberImgProc.create(vo);

            result.put("success", true);
            result.put("filename", fileName);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "FastAPI 호출 중 오류 발생: " + e.getMessage());
        }

        return result;
    }


}
