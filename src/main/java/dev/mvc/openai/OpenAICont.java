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

/**
 * OpenAI 기반 기능 컨트롤러
 * - FastAPI와 연동하여 AI 서비스(레시피 추천, 이미지 기반 기능) 제공
 * - 역할:
 *    1) 식재료 기반 레시피 추천 UI 및 API 연동
 *    2) 사용자 이미지 기반 AI 기능
 *    3) FastAPI 서버와 통신 (HTTP 요청)
 */
@Controller
@RequestMapping("/openai")
public class OpenAICont {

  /** 회원 이미지 처리 서비스 */
  @Autowired
  @Qualifier("dev.mvc.openai.MemberImgProc")
  private MemberImgProcInter memberImgProc;
  
  private final String openai_ip="http://121.78.128.177:8000";
  //private final String openai_ip="http:/localhost:8000";

  /** 레시피 처리 서비스 */
  @Autowired
  @Qualifier("dev.mvc.openai.RecipeProc")
  private RecipeProcInter recipeProc;

  /** 회원 관련 기능 서비스 */
  @Autowired
  @Qualifier("dev.mvc.member.MemberProc")
  private MemberProcInter memberProc;

  /** FastAPI 호출용 HTTP 클라이언트 */
  private final RestTemplate restTemplate;

  /** FastAPI URL: 레시피 추천 API */
  private static final String FASTAPI_FOOD_URL = "http://121.78.128.177:8000/food";
  /** FastAPI URL: 회원 이미지 분석 API */
  private static final String FASTAPI_MEMBER_IMG_URL = "http://121.78.128.177:8000/member_img";

  
  /**
   * RestTemplate 의존성 주입 (생성자 방식)
   * @param restTemplate Spring에서 제공하는 HTTP 요청 객체
   */
  @Autowired
  public OpenAICont(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
    System.out.println("-> OpenAICont initialized. RestTemplate hashCode: " + this.restTemplate.hashCode());
  }

  /**
   * [GET] 레시피 추천 페이지
   * - 기능: 식재료 선택 UI 출력 + 사용자의 이전 레시피 내역 조회
   * - URL 예시: http://localhost:9093/openai/recipe
   *
   * @param session  로그인 정보 확인용 세션
   * @param model    뷰에 데이터 전달용 객체
   * @return         openai/recipe.html (Thymeleaf 템플릿)
   */
  @GetMapping("/recipe")
  public String recipePage(HttpSession session, Model model) {
    // (1) 로그인 여부 확인
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {       // 로그인 안 된 경우 → 로그인 유도 페이지로 리디렉션
      return "redirect:/member/login_cookie_need?url=/openai/recipe";
    }

    // (2) 식재료 목록 전달 (FastAPI와 동일한 순서)
    String[] foods = { "배추", "무", "대파", "양파", "오이", "사과", "배", "귤", "바나나", "딸기", "고등어", "갈치", "새우", "홍합", "오징어", "소고기",
        "돼지고기", "닭가슴살", "닭날개", "닭다리", "우유", "달걀", "치즈", "버터", "두부" };
    model.addAttribute("foods", foods);

    // (3) 사용자 레시피 내역 (DB에서 최근 기록 조회)
    model.addAttribute("recipeList", recipeProc.list_by_member(memberno));

    // (4) 템플릿 이동
    return "openai/recipe"; // /templates/openai/recipe.html
  }

  /**
   * [POST] AJAX 요청 → FastAPI 호출 → 레시피 추천 결과 반환
   * - 프론트엔드에서 선택한 식재료 데이터를 FastAPI에 전달하여 추천 결과를 받아옴
   * - DB에 추천 결과 로그 저장 (Recipe 테이블)
   * 
   * 요청 파라미터:
   *   - food (String): 선택된 재료를 이진수 문자열로 전달 (예: "0,1,0,0,1,...")
   * 응답:
   *   - JSON: { "success": true, "response": "추천 결과" } 형태
   */
  @PostMapping("/recipe_ajax")
  @ResponseBody
  public Map<String, Object> recommendRecipe(@RequestParam("food") String foodBinary, HttpSession session) {
    Map<String, Object> result = new HashMap<>();

    // (1) 로그인 여부 확인
    Integer memberno = (Integer) session.getAttribute("memberno"); // 로그인 사용자 PK
    if (memberno == null) {
      result.put("error", "로그인이 필요합니다.");
      return result;
    }

    try {
      // (2) FastAPI 요청 헤더 생성 (Content-Type: application/json)
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON); // JSON 데이터 전송 명시

      // (3) 요청 바디 데이터 구성
      Map<String, Object> body = new HashMap<>();
      body.put("SpringBoot_FastAPI_KEY", new LLMKey().getSpringBoot_FastAPI_KEY());  // 인증 키
      body.put("food", foodBinary); // 예: "0,1,0,0,1..." // 선택 재료 정보 (이진 문자열)

      // 요청 엔티티 생성 (헤더 + 바디)
      HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

      // (4) FastAPI 서버 호출 (POST)
      String response = restTemplate.postForObject(FASTAPI_FOOD_URL, requestEntity, String.class);
      System.out.println("[OpenAICont] FastAPI Response: " + response);

      // (5) DB에 추천 결과 저장
      RecipeVO vo = new RecipeVO();
      vo.setMemberno(memberno);       // 요청한 사용자
      vo.setFoodBinary(foodBinary);   // 선택된 재료
      vo.setContent(response);        // FastAPI에서 받은 추천 결과
      recipeProc.create(vo);          // DB insert

      // (6) 응답 데이터 JSON 그대로 반환 (프론트에서 처리)
      result.put("success", true);
      result.put("response", response);

    } catch (Exception e) {
      // 예외 처리: FastAPI 서버 오류, 네트워크 장애 등
      e.printStackTrace();
      result.put("success", false);
      result.put("error", "FastAPI 호출 중 오류 발생: " + e.getMessage());
    }

    return result; // JSON 응답
  }

  /**
   * [GET] 회원 이미지 생성 페이지
   * - 접근 권한: 관리자(admin) 또는 공급자(supplier)만 가능
   * - 해당 회원이 생성한 이미지 리스트를 DB에서 조회하여 화면에 출력
   * 
   * 처리 흐름:
   *   1) 세션에서 gradeStr(문자열 등급) 확인
   *   2) 권한 없으면 로그인 안내 페이지로 리다이렉트
   *   3) 로그인된 회원의 이미지 목록 조회 → Model에 저장
   *   4) 뷰 페이지로 forward → openai/member_img.html 렌더링
   */
  @GetMapping("/member_img")
  public String memberImgPage(HttpSession session, Model model) {
    // (1) 사용자 권한 확인 (admin 또는 supplier만 허용)
    String gradeStr = (String) session.getAttribute("gradeStr");
    if (gradeStr == null || (!"admin".equals(gradeStr) && !"supplier".equals(gradeStr))) {
      // 권한 없음 → 로그인 필요 안내 페이지로 리다이렉트
      return "redirect:/member/login_cookie_need?url=/openai/member_img";
    }

    // (2) 로그인한 회원번호 확인
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno != null) {
      // (3) 회원별 생성 이미지 목록 DB 조회
      model.addAttribute("imgList", memberImgProc.list_by_member(memberno));
    }
    // (4) 뷰 렌더링
    return "openai/member_img";
  }

  
  /**
   * [POST] 회원 이미지 생성 (AJAX)
   * - 사용자가 입력한 프롬프트를 기반으로 FastAPI 서버를 호출해 AI 이미지 생성
   * - 생성된 이미지 파일명을 DB에 저장
   * - 관리자(admin) 또는 공급자(supplier)만 접근 가능
   *
   * 요청 파라미터:
   *   - prompt: 이미지 생성에 사용할 텍스트 프롬프트
   *
   * 처리 흐름:
   *   1) 세션에서 로그인 및 권한 확인 (admin, supplier만 허용)
   *   2) FastAPI 호출 준비 (헤더 + JSON Body)
   *   3) FastAPI로 요청 → AI 이미지 생성 후 응답(JSON)
   *   4) 응답 JSON에서 생성된 파일 경로 추출 → 파일명만 파싱
   *   5) DB(MemberImg 테이블)에 이미지 정보 저장 (회원번호, 프롬프트, 파일명)
   *   6) 프론트에 성공 여부와 파일명 반환
   *
   * 응답 예시:
   *   { "success": true, "filename": "ai_image_202507.jpg" }
   */
  @PostMapping("/member_img_ajax")
  @ResponseBody
  public Map<String, Object> createMemberImage(@RequestParam("prompt") String prompt, HttpSession session) {
    Map<String, Object> result = new HashMap<>();

    // (1) 세션에서 로그인 및 권한 확인
    Integer grade = (Integer) session.getAttribute("grade"); // 숫자 등급 (예: 1=admin, 5=supplier, 16=user)
    Integer memberno = (Integer) session.getAttribute("memberno");

    // 관리자(1~4) 또는 공급자(5~15)만 허용
    if (grade == null || memberno == null || grade > 15) {
      result.put("success", false);
      result.put("error", "권한이 없습니다.");
      return result;
    }

    String fileName = "";
    try {
      // (2) FastAPI 호출을 위한 요청 설정
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      Map<String, Object> body = new HashMap<>();
      body.put("SpringBoot_FastAPI_KEY", new LLMKey().getSpringBoot_FastAPI_KEY());
      body.put("prompt", prompt);

      HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
      String response = restTemplate.postForObject(FASTAPI_MEMBER_IMG_URL, requestEntity, String.class);
      System.out.println("[OpenAICont] FastAPI Response: " + response);

      // (3) 파일명 파싱
      JSONObject json = new JSONObject(response);
      String fullPath = json.getString("file_name");
      fileName = fullPath.substring(fullPath.lastIndexOf("/") + 1);
      if (fileName.contains("\\")) {
        fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
      }

      System.out.println("-> Extracted fileName: " + fileName);

      // (4) DB 저장
      MemberImgVO vo = new MemberImgVO();
      vo.setMemberno(memberno);
      vo.setPrompt(prompt);
      vo.setFilename(fileName);
      memberImgProc.create(vo);

      // (5) 응답
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
