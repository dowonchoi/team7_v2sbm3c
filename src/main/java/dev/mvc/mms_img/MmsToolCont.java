package dev.mvc.mms_img;

import dev.mvc.tool.LLMKey;
import dev.mvc.tool.Tool;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * MmsToolCont
 * -----------------------------------------------------------
 * 역할:
 * - MMS 제작/발송 툴을 제공하는 컨트롤러
 * - AI 이미지 생성(FastAPI 연동), 텍스트 합성(Java 서비스), MMS 발송(Gabia API) 처리
 *
 * 요청 경로: /mms_tool/*
 * -----------------------------------------------------------
 */
@Controller
@RequestMapping("/mms_tool")
public class MmsToolCont {

    /** FastAPI와 HTTP 통신을 위해 Spring의 RestTemplate 사용 */
    @Autowired
    private RestTemplate restTemplate;

    /** 이미지 합성(텍스트 추가) 처리 서비스 */
    @Autowired
    private MMSImageService mmsImageService;

    /** Gabia MMS 발송 처리 서비스 */
    @Autowired
    private MmsSendService mmsSendService;

    /** FastAPI MMS 이미지 생성 API URL */
    private static final String FASTAPI_MMS_IMG_URL = "http://localhost:8000/mms_img";

    /**
     * 테스트 UI 페이지 로드
     * - 사용자에게 MMS 툴 화면 제공
     * - templates/mms_img/mms_tool.html 렌더링
     *
     * @return MMS 툴 페이지 경로
     */
    @GetMapping("/test")
    public String testPage(Model model) {
        return "mms_img/mms_tool"; // ✅ templates/mms_img/mms_tool.html
    }

    /**
     * STEP 1: AI 배경 이미지 생성 요청
     * - 클라이언트에서 프롬프트를 받아 FastAPI 서버로 전달
     * - FastAPI가 이미지 생성 후 서버에 저장 → 저장된 이미지 경로 반환
     * - 반환된 정보를 JSON으로 응답
     *
     * 요청 URL: POST /mms_tool/create
     * 요청 데이터: prompt (String)
     * 응답 데이터:
     *   {
     *     "success": true/false,
     *     "fileName": "생성된 이미지 파일명",
     *     "filePath": "/mms_img/이미지명"
     *   }
     */
    @PostMapping("/create")
    @ResponseBody
    public Map<String, Object> createImage(@RequestParam("prompt") String prompt) {
        // 응답 데이터를 담을 Map
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. FastAPI로 요청 보낼 HTTP 헤더 설정 (JSON 타입)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 2. 요청 본문 데이터 구성
            Map<String, Object> body = new HashMap<>();
            body.put("SpringBoot_FastAPI_KEY", new LLMKey().getSpringBoot_FastAPI_KEY()); // 인증 키
            body.put("prompt", prompt);

            // 3. 요청 엔티티 생성 (헤더 + 본문)
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 4. FastAPI 서버에 POST 요청
            String response = restTemplate.postForObject(FASTAPI_MMS_IMG_URL, requestEntity, String.class);
            
            // 5. 응답(JSON 문자열)을 JSONObject로 파싱
            JSONObject json = new JSONObject(response);

            // 6. 응답에서 이미지 파일 경로 추출
            String fullPath = json.getString("file_name"); // 예: /mnt/data/mms_img/generated_image.jpg
            String fileName = fullPath.substring(fullPath.lastIndexOf("/") + 1);  // 파일명만 추출

            // 7. 응답 데이터 구성
            result.put("success", true);
            result.put("fileName", fileName);
            result.put("filePath", "/mms_img/" + fileName); // 클라이언트에서 이미지 로드할 경로

        } catch (Exception e) {
            // 오류 발생 시 예외 메시지 전달
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    
    /**
     * STEP 2: 이미지에 텍스트 합성 처리
     * - 클라이언트에서 전달받은 옵션(폰트, 크기, 색상, 그림자)을 적용
     * - 기존 이미지에 텍스트를 합성 후 새로운 이미지 파일로 저장
     *
     * 요청 URL: POST /mms_tool/text
     * 요청 데이터:
     *   fileName     : 원본 이미지 파일명
     *   messageText  : 합성할 텍스트 내용 (줄바꿈 가능)
     *   fontName     : 폰트명 (기본값: Malgun Gothic)
     *   fontSize     : 폰트 크기 (기본값: 60)
     *   textColor    : 텍스트 색상 (기본값: #FFFFFF)
     *   shadowColor  : 그림자 색상 (기본값: #000000)
     *
     * 응답 데이터:
     *   {
     *     "success": true/false,
     *     "finalFileName": "합성된 이미지 파일명",
     *     "filePath": "/mms_img/합성된 이미지 파일"
     *   }
     */
    @PostMapping("/text")
    @ResponseBody
    public Map<String, Object> addText(@RequestParam("fileName") String fileName,
                                       @RequestParam("messageText") String messageText,
                                       @RequestParam(value = "fontName", required = false) String fontName,
                                       @RequestParam(value = "fontSize", required = false) Integer fontSize,
                                       @RequestParam(value = "textColor", required = false) String textColor,
                                       @RequestParam(value = "shadowColor", required = false) String shadowColor) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 기본값 설정
            // - 클라이언트에서 옵션을 전달하지 않은 경우 기본값 적용
            if (fontName == null || fontName.trim().isEmpty()) {
                fontName = "Malgun Gothic";  // 기본 폰트
            }
            if (fontSize == null || fontSize <= 0) {
                fontSize = 60;  // 기본 폰트 크기
            }
            if (textColor == null || textColor.trim().isEmpty()) {
                textColor = "#FFFFFF";  // 기본 텍스트 색상: 흰색
            }
            if (shadowColor == null || shadowColor.trim().isEmpty()) {
                shadowColor = "#000000";  // 기본 그림자 색상: 검정색
            }

            // 2. 원본 이미지 경로 생성
            //String inputPath = "C:/kd/deploy/mms/storage/" + fileName;
            String inputPath = MMSImage.getUploadDir() + fileName;

            // 3. MMSImageService 호출
            // - 이미지에 텍스트 합성 후 새로운 파일명 반환
            String finalFileName = mmsImageService.addTextToImage(
                    inputPath,
                    messageText.replace("\\n", "\n"), // "\n" 문자열을 실제 개행으로 변환
                    fontName,
                    fontSize,
                    textColor,
                    shadowColor
            );
            
            // 4. 결과 응답 데이터 구성
            result.put("success", true);
            result.put("finalFileName", finalFileName);
            result.put("filePath", "/mms_img/" + finalFileName);  // 브라우저에서 접근 가능한 경로

        } catch (IOException e) {
            // 이미지 처리 중 오류 발생
            result.put("success", false);
            result.put("error", "이미지 처리 오류: " + e.getMessage());
        }
        return result;
    }


    /**
     * STEP 3: MMS 발송 처리
     * - Gabia MMS API를 통해 지정된 휴대폰 번호로 MMS 전송
     *
     * 요청 URL: POST /mms_tool/send
     * 요청 데이터:
     *   phoneNumber    : MMS 수신자 번호
     *   finalFileName  : 최종 합성 이미지 파일명
     *
     * 응답 데이터:
     *   {
     *     "success": true/false,
     *     "message": "MMS 발송 성공/실패",
     *     "error": "실패 시 오류 메시지"
     *   }
     */
    @PostMapping("/send")
    @ResponseBody
    public Map<String, Object> sendMMS(@RequestParam("phoneNumber") String phoneNumber,
                                       @RequestParam("finalFileName") String finalFileName) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 최종 이미지 파일 경로 생성
            //String imagePath = "C:/kd/deploy/mms/storage/" + finalFileName;
            String imagePath = MMSImage.getUploadDir() + finalFileName;

            // 2. 파일 존재 여부 확인
            File file = new File(imagePath);
            if (!file.exists()) {
                result.put("success", false);
                result.put("error", "이미지 파일이 존재하지 않습니다.");
                return result;
            }

            // 3. MMS 발송 서비스 호출
            // - Gabia API 연동
            boolean sendResult = mmsSendService.sendMMS(phoneNumber, imagePath);

            // 4. 결과 응답
            result.put("success", sendResult);
            result.put("message", sendResult ? "MMS 발송 성공" : "MMS 발송 실패");

        } catch (Exception e) {
          // MMS 발송 중 예외 발생
            result.put("success", false);
            result.put("error", "MMS 발송 오류: " + e.getMessage());
        }
        return result;
    }
}
