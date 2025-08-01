package dev.mvc.mms_img;

import dev.mvc.member.MemberProcInter;
import dev.mvc.tool.LLMKey;
import jakarta.servlet.http.HttpSession;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MmsImgCont
 * -----------------------------------------------------------
 * 역할:
 * - 관리자 전용 MMS 이미지 제작 기능 제공
 * - AI 기반 이미지 생성(FastAPI), 이미지 관리, MMS 발송 이력 관리
 *
 * 매핑 경로: /mms/*
 *
 * 구성 기능:
 * 1) createForm() : OpenAI 이미지 생성 페이지 (GET)
 * 2) 이후 메서드: 이미지 생성, 합성, 발송 등
 * -----------------------------------------------------------
 */
@Controller
@RequestMapping("/mms")
public class MmsImgCont {
  /** Gabia MMS 발송 처리 서비스 */
  @Autowired
  private MmsSendService mmsSendService;

  /** MMS 발송 로그 처리용 서비스 (DB 기록) */
  @Autowired
  @Qualifier("dev.mvc.mms_img.MmsSendLogProc")
  private MmsSendLogProcInter mmsSendLogProc;

  /** 이미지 처리 서비스 (텍스트 합성 등) */
  @Autowired
  private MMSImageService mmsImageService;

  /** MMS 이미지 DB 처리 서비스 */
  @Autowired
  @Qualifier("dev.mvc.mms_img.MmsImgProc")
  private MmsImgProcInter mmsImgProc;

  /** 회원 정보 처리 서비스 (권한 확인) */
  @Autowired
  @Qualifier("dev.mvc.member.MemberProc")
  private MemberProcInter memberProc;

  /** FastAPI 호출용 (AI 이미지 생성) */
  @Autowired
  private RestTemplate restTemplate; //   FastAPI 호출용

  /** FastAPI MMS 이미지 생성 API 엔드포인트 */
  private static final String FASTAPI_MMS_IMG_URL = "http://localhost:8000/mms_img";

  /**
   * STEP 1: OpenAI 이미지 생성 화면 요청
   * - 관리자만 접근 가능 (grade == "admin")
   * - 자신의 MMS 이미지 목록을 불러와 화면에 표시
   *
   * URL: GET /mms/create
   *
   * 화면: templates/mms_img/create.html
   *
   * 처리 흐름:
   * 1) 세션에서 grade(회원 등급) 확인
   *    - admin이 아니면 로그인 페이지로 리다이렉트
   * 2) 세션에서 memberno 추출 후, DB에서 해당 사용자의 이미지 목록 조회
   * 3) 모델에 이미지 목록 추가 후 create.html 렌더링
   */
  @GetMapping("/create")
  public String createForm(HttpSession session, Model model) {
    String grade = (String) session.getAttribute("grade");

    // 1. 권한 확인: grade == "admin"만 허용
    if (grade == null || !"admin".equals(grade)) {
      // 권한 없음 → 로그인/권한 요청 페이지로 이동
      return "redirect:/member/login_cookie_need?url=/mms/tool";
    }

    // 2. 현재 로그인된 관리자 memberno 가져오기
    Integer memberno = (Integer) session.getAttribute("memberno");
    
    // 3. 관리자 본인이 등록한 MMS 이미지 목록 조회
    if (memberno != null) {
      List<MmsImgVO> imgList = mmsImgProc.list();
      model.addAttribute("imgList", imgList);
    }
    // 4. MMS 이미지 생성 화면으로 이동
    return "mms_img/create"; //   뷰 템플릿 (mms_img/create.html)
  }

  /**
   * STEP 1: AI 기반 MMS 이미지 생성
   * - 관리자 전용 기능 (grade == "admin")
   * - 클라이언트에서 입력한 프롬프트를 FastAPI 서버에 전달
   * - FastAPI가 이미지 생성 후 저장 경로를 응답
   * - 해당 이미지 정보를 DB에 저장
   *
   * 요청 URL: POST /mms/create
   * 요청 데이터:
   *   prompt : AI 이미지 생성용 프롬프트
   *
   * 응답 데이터:
   *   {
   *     "success": true/false,
   *     "filename": "생성된 이미지 파일명",
   *     "mimgno": DB에 저장된 MMS 이미지 PK
   *   }
   */
  @PostMapping("/create")
  @ResponseBody
  public Map<String, Object> createImage(@RequestParam("prompt") String prompt, HttpSession session) {
    Map<String, Object> result = new HashMap<>();
    
    // 1. 세션에서 관리자 권한 확인
    String grade = (String) session.getAttribute("grade");
    Integer memberno = (Integer) session.getAttribute("memberno");

    if (grade == null || !"admin".equals(grade)) {
      // 관리자가 아니면 이미지 생성 불가
      result.put("success", false);
      result.put("error", "권한이 없습니다.");
      return result;
    }

    String fileName = ""; // FastAPI 응답에서 추출할 최종 파일명
    try {
      // 2. FastAPI 요청 준비
      // 헤더 설정 (JSON 전송)
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      // 요청 본문 데이터 구성
      Map<String, Object> body = new HashMap<>();
      body.put("SpringBoot_FastAPI_KEY", new LLMKey().getSpringBoot_FastAPI_KEY());  // 인증 키
      body.put("prompt", prompt); ; // 이미지 생성용 프롬프트

      // HttpEntity로 요청 데이터(헤더 + 본문) 래핑
      HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

      // 3. FastAPI 호출 (POST)
      // 응답은 JSON 문자열 형태로 수신
      String response = restTemplate.postForObject(FASTAPI_MMS_IMG_URL, requestEntity, String.class);
      System.out.println("[MmsImgCont] FastAPI Response: " + response);

      // 4. 응답(JSON) 파싱
      // 응답 구조 예시: {"file_name": "C:/kd/deploy/mms/storage/xxx.jpg"}
      JSONObject json = new JSONObject(response);
      String fullPath = json.getString("file_name"); // 이미지 전체 경로 C:/kd/deploy/mms/storage/xxx.jpg

      // 5. 파일명만 추출 (경로에서 마지막 구분자 이후)
      fileName = fullPath.substring(fullPath.lastIndexOf("/") + 1);
      if (fileName.contains("\\")) {
        // Windows 경로 처리
        fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
      }
      System.out.println("-> Extracted fileName: " + fileName);

      // 6. DB 저장
      // 생성된 이미지 정보를 MmsImgVO에 담아 DB에 insert
      MmsImgVO vo = new MmsImgVO();
      vo.setMemberno(memberno);
      vo.setPrompt(prompt);
      vo.setOriginal_filename(fileName); 
      vo.setFilepath("/mms_img/" + fileName); // 웹에서 접근 가능한 경로
      vo.setStatus("created"); // 상태값: created

      mmsImgProc.create(vo); // DB insert 실행

      // 7. MyBatis selectKey로 PK(mimgno) 가져오기
      int mimgno = vo.getMimgno();

      // 8. 응답 데이터 구성
      result.put("success", true);
      result.put("filename", fileName);
      result.put("mimgno", mimgno);

    } catch (Exception e) {
      // 오류 발생 시 예외 메시지 반환
      e.printStackTrace();
      result.put("success", false);
      result.put("error", "이미지 생성 중 오류 발생: " + e.getMessage());
    }

    return result;
  }

  /**
   * STEP 2: 이미지에 텍스트 합성 처리
   * - 관리자 기능: 기존 AI 생성 이미지를 불러와 텍스트를 합성
   * - 클라이언트에서 입력한 문구 및 폰트 옵션을 사용
   * - 합성된 결과 이미지는 새 파일로 저장하고 DB 상태 갱신
   *
   * 요청 URL: POST /mms/text
   *
   * 요청 파라미터:
   *   mimgno       : MMS 이미지 PK (DB에서 원본 이미지 조회용)
   *   message_text : 합성할 텍스트 내용 (줄바꿈 허용)
   *   fontName     : 폰트명 (옵션, 기본값: Malgun Gothic)
   *   fontSize     : 폰트 크기 (옵션, 기본값: 60)
   *   textColor    : 텍스트 색상 (옵션, 기본값: #FFFFFF)
   *   shadowColor  : 그림자 색상 (옵션, 기본값: #000000)
   *
   * 응답(JSON):
   *   {
   *     "success": true/false,
   *     "mimgno": 이미지 PK,
   *     "finalFileName": 최종 합성된 이미지 파일명,
   *     "status": 처리 상태(text_added)
   *   }
   */
  @PostMapping("/text")
  @ResponseBody
  public Map<String, Object> addText(@RequestParam("mimgno") int mimgno,
      @RequestParam("message_text") String messageText,
      @RequestParam(value = "fontName", required = false) String fontName,
      @RequestParam(value = "fontSize", required = false) Integer fontSize,
      @RequestParam(value = "textColor", required = false) String textColor,
      @RequestParam(value = "shadowColor", required = false) String shadowColor) {

    Map<String, Object> result = new HashMap<>();
    System.out.println("[DEBUG] /mms/text 요청");
    System.out.println("mimgno: " + mimgno);
    System.out.println("messageText: " + messageText);
    System.out.println("fontName: " + fontName + ", fontSize: " + fontSize);
    System.out.println("textColor: " + textColor + ", shadowColor: " + shadowColor);

    try {
      // 1. 줄바꿈 처리
      // - 클라이언트에서 "\\n" 형태로 들어온 개행 문자를 실제 개행(\n)으로 변환
      messageText = messageText.replace("\\n", "\n");

      // 2. 폰트 및 색상 기본값 설정
      if (fontName == null || fontName.trim().isEmpty()) {
        fontName = "Malgun Gothic"; // 기본 폰트
      }
      if (fontSize == null || fontSize <= 0) {
        fontSize = 60; // 기본 크기
      }
      if (textColor == null || textColor.trim().isEmpty()) {
        textColor = "#FFFFFF"; // 기본 텍스트 색상 (흰색)
      }
      if (shadowColor == null || shadowColor.trim().isEmpty()) {
        shadowColor = "#000000"; // 기본 그림자 색상 (검정)
      }

      // 3. DB에서 원본 이미지 정보 조회
      MmsImgVO vo = mmsImgProc.read(mimgno);
      String inputPath = "C:/kd/deploy/mms/storage/" + vo.getOriginal_filename();
      System.out.println("[DEBUG] 입력 경로: " + inputPath);

      // 4. 이미지에 텍스트 합성 처리
      // - 옵션(폰트, 크기, 색상, 그림자) 적용
      String finalFileName = mmsImageService.addTextToImage(inputPath, messageText, fontName, fontSize, textColor,
          shadowColor);
      System.out.println("[DEBUG] 최종 파일명: " + finalFileName);

      // 5. DB 상태 업데이트
      // - 합성된 텍스트, 최종 파일명, 상태값(text_added) 저장
      vo.setMessage_text(messageText);
      vo.setFinal_filename(finalFileName);
      vo.setStatus("text_added");
      int updated = mmsImgProc.updateTextAndFinalImage(vo);
      System.out.println("[DEBUG] DB 업데이트 결과: " + updated);

      // 6. 성공 응답 데이터 구성
      result.put("success", true);
      result.put("mimgno", vo.getMimgno());
      result.put("finalFileName", finalFileName);
      result.put("status", vo.getStatus());

    } catch (IOException e) {
      // 이미지 합성 처리 중 I/O 예외 발생
      e.printStackTrace();
      result.put("success", false);
      result.put("error", "이미지 합성 중 오류: " + e.getMessage());
    } catch (Exception e) {
      // 기타 서버 오류
      e.printStackTrace();
      result.put("success", false);
      result.put("error", "서버 오류: " + e.getMessage());
    }

    return result;
  }

  /**
   * STEP 3: MMS 발송 처리
   * - Gabia MMS API를 이용해 지정된 전화번호로 메시지 전송
   * - 발송 이력을 DB에 기록 (mms_send_log)
   *
   * 요청 URL: POST /mms/send
   *
   * 요청 파라미터:
   *   mimgno       : MMS 이미지 PK (DB에서 이미지 조회)
   *   phone_number : MMS 수신자 전화번호
   *
   * 세션:
   *   memberno : 발송 요청자(관리자) PK
   *
   * 응답(JSON):
   *   {
   *     "success": true/false,
   *     "status": "success/fail",
   *     "logno": 발송 로그 PK
   *   }
   */
  @PostMapping("/send")
  @ResponseBody
  public Map<String, Object> sendMMS(@RequestParam("mimgno") int mimgno,
      @RequestParam("phone_number") String phoneNumber, HttpSession session) {

    Map<String, Object> result = new HashMap<>();
    Integer memberno = (Integer) session.getAttribute("memberno"); // 발송 요청자 정보

    try {
      // 1. DB에서 MMS 이미지 정보 조회
      MmsImgVO vo = mmsImgProc.read(mimgno);

      // 2. 발송할 이미지 파일명 결정
      // - 텍스트 합성된 final_filename이 있으면 우선 사용
      // - 없으면 original_filename 사용
      String finalFile = vo.getFinal_filename();
      if (finalFile == null || finalFile.trim().isEmpty()) {
        finalFile = vo.getOriginal_filename();
      }

      // 3. 실제 서버 저장 경로 생성
      String imagePath = "C:/kd/deploy/mms/storage/" + finalFile;

      // 4. Gabia MMS API 호출
      // - 이미지와 메시지를 Gabia API를 통해 발송
      boolean sendResult = mmsSendService.sendMMS(phoneNumber, imagePath);

      // 5. 발송 로그 기록
      // - mms_send_log 테이블에 insert
      MmsSendLogVO logVO = new MmsSendLogVO();
      logVO.setMimgno(mimgno);             // 발송한 이미지 번호
      logVO.setMemberno(memberno);         // 발송 요청자 번호
      logVO.setPhone_number(phoneNumber);  // 수신자 번호
      logVO.setSend_status(sendResult ? "success" : "fail"); // 발송 결과

      // DB insert (MyBatis <selectKey>로 PK 자동 설정)
      mmsSendLogProc.create(logVO);

      // 6. 성공 응답 구성
      result.put("success", sendResult);
      result.put("status", logVO.getSend_status()); // 발송 상태
      result.put("logno", logVO.getMslogno()); // 발송 로그 PK

    } catch (Exception e) {
      e.printStackTrace();
      result.put("success", false);
      result.put("error", e.getMessage());
    }

    return result;
  }

  //   MMS Tool 테스트 페이지
  @GetMapping("/tool")
  public String tool(HttpSession session,  Model model) {
    // 예외 발생 시 실패 응답
    String gradeStr = (String) session.getAttribute("gradeStr");
    if (gradeStr == null || !"admin".equals(gradeStr)) {
      return "redirect:/member/login_cookie_need?url=/mms/tool";
    }
    // 전체 이미지 목록
    List<MmsImgVO> imgList = mmsImgProc.list();
    model.addAttribute("imgList", imgList);

    // 이미지별 발송 로그를 Map으로 저장
    Map<Integer, List<MmsSendLogVO>> logMap = new HashMap<>();
    for (MmsImgVO vo : imgList) {
      List<MmsSendLogVO> logs = mmsSendLogProc.listByImgno(vo.getMimgno());
      logMap.put(vo.getMimgno(), logs);
    }
    model.addAttribute("logMap", logMap);

    return "mms_img/mms_tool"; //   templates/mms_img/mms_tool.html
  }

}












