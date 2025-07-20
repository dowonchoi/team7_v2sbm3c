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

@Controller
@RequestMapping("/mms")
public class MmsImgCont {
  
  
  
  @Autowired
  private MmsSendService mmsSendService;

  @Autowired
  @Qualifier("dev.mvc.mms_img.MmsSendLogProc")
  private MmsSendLogProcInter mmsSendLogProc;
  
    @Autowired
    private MMSImageService mmsImageService;


    @Autowired
    @Qualifier("dev.mvc.mms_img.MmsImgProc")
    private MmsImgProcInter mmsImgProc;

    @Autowired
    @Qualifier("dev.mvc.member.MemberProc")
    private MemberProcInter memberProc;

    @Autowired
    private RestTemplate restTemplate; // ✅ FastAPI 호출용

    // ✅ FastAPI URL
    private static final String FASTAPI_MMS_IMG_URL = "http://localhost:8000/mms_img";

    /**
     * STEP 1: OpenAI 이미지 생성 화면 (GET)
     * 관리자만 접근 가능
     */
    @GetMapping("/create")
    public String createForm(HttpSession session, Model model) {
        String grade = (String) session.getAttribute("grade");

        // ✅ 권한 체크: admin만 접근 가능
        if (grade == null || !"admin".equals(grade)) {
            return "redirect:/member/login_cookie_need?url=/mms/create";
        }

        // ✅ 관리자 본인 이미지 목록 표시 (참고용)
        Integer memberno = (Integer) session.getAttribute("memberno");
        if (memberno != null) {
            List<MmsImgVO> imgList = mmsImgProc.list();
            model.addAttribute("imgList", imgList);
        }

        return "mms_img/create"; // ✅ 뷰 템플릿 (mms_img/create.html)
    }

    @PostMapping("/create")
    @ResponseBody
    public Map<String, Object> createImage(@RequestParam("prompt") String prompt, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        String grade = (String) session.getAttribute("grade");
        Integer memberno = (Integer) session.getAttribute("memberno");

        if (grade == null || !"admin".equals(grade)) {
            result.put("success", false);
            result.put("error", "권한이 없습니다.");
            return result;
        }

        String fileName = ""; // ✅ 변수 선언
        try {
            // ✅ 1. FastAPI 호출 준비
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("SpringBoot_FastAPI_KEY", new LLMKey().getSpringBoot_FastAPI_KEY());
            body.put("prompt", prompt);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // ✅ 2. FastAPI 호출
            String response = restTemplate.postForObject(FASTAPI_MMS_IMG_URL, requestEntity, String.class);
            System.out.println("[MmsImgCont] FastAPI Response: " + response);

            // ✅ 3. JSON 파싱
            JSONObject json = new JSONObject(response);
            String fullPath = json.getString("file_name"); // C:/kd/deploy/mms/storage/xxx.jpg

            // ✅ 4. 파일명 추출
            fileName = fullPath.substring(fullPath.lastIndexOf("/") + 1);
            if (fileName.contains("\\")) {
                fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
            }
            System.out.println("-> Extracted fileName: " + fileName);

            // ✅ 5. DB 저장
            MmsImgVO vo = new MmsImgVO();
            vo.setMemberno(memberno);
            vo.setPrompt(prompt);
            vo.setOriginal_filename(fileName);
            vo.setFilepath("/mms_img/" + fileName);
            vo.setStatus("created");

            mmsImgProc.create(vo);

            // ✅ 6. PK 가져오기 (MyBatis <selectKey> 설정 필요)
            int mimgno = vo.getMimgno();

            // ✅ 7. 응답 데이터
            result.put("success", true);
            result.put("filename", fileName);
            result.put("mimgno", mimgno);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "이미지 생성 중 오류 발생: " + e.getMessage());
        }

        return result;
    }


    
    @PostMapping("/text")
    @ResponseBody
    public Map<String, Object> addText(
            @RequestParam("mimgno") int mimgno,
            @RequestParam("message_text") String messageText) {

        Map<String, Object> result = new HashMap<>();
        System.out.println("[DEBUG] /mms/text 요청 - mimgno: " + mimgno + ", messageText: " + messageText);

        try {
            // ✅ 줄바꿈 변환 (\n → 실제 줄바꿈)
            messageText = messageText.replace("\\n", "\n");

            // ✅ 1. DB에서 원본 이미지 읽기
            MmsImgVO vo = mmsImgProc.read(mimgno);
            System.out.println("[DEBUG] 원본 이미지: " + vo.getOriginal_filename());

            // ✅ 2. 원본 이미지 절대경로
            String inputPath = "C:/kd/deploy/mms/storage/" + vo.getOriginal_filename();
            System.out.println("[DEBUG] 입력 경로: " + inputPath);

            // ✅ 3. 합성 이미지 생성 (옵션 포함)
            String finalFileName = mmsImageService.addTextToImage(
                    inputPath,
                    messageText,           // ✅ 줄바꿈 적용 후
                    "Malgun Gothic",       // ✅ 폰트명
                    60,                    // ✅ 폰트 크기
                    "#FFFFFF",             // ✅ 텍스트 색상
                    "#000000"              // ✅ 그림자 색상
            );
            System.out.println("[DEBUG] 최종 파일명: " + finalFileName);

            // ✅ 4. DB 업데이트
            vo.setMessage_text(messageText);
            vo.setFinal_filename(finalFileName);
            vo.setStatus("text_added");
            int updated = mmsImgProc.updateTextAndFinalImage(vo);
            System.out.println("[DEBUG] DB 업데이트 결과: " + updated);

            // ✅ 5. JSON 응답
            result.put("success", true);
            result.put("mimgno", vo.getMimgno());
            result.put("finalFileName", finalFileName);
            result.put("status", vo.getStatus());

        } catch (IOException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "이미지 합성 중 오류: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "서버 오류: " + e.getMessage());
        }

        return result;
    }




    @PostMapping("/send")
    @ResponseBody
    public Map<String, Object> sendMMS(
            @RequestParam("mimgno") int mimgno,
            @RequestParam("phone_number") String phoneNumber,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();
        Integer memberno = (Integer) session.getAttribute("memberno");

        try {
            // ✅ 1. DB에서 이미지 확인
            MmsImgVO vo = mmsImgProc.read(mimgno);

            // ✅ 2. final_filename이 없으면 original_filename 사용
            String finalFile = vo.getFinal_filename();
            if (finalFile == null || finalFile.trim().isEmpty()) {
                finalFile = vo.getOriginal_filename();
            }

            // ✅ 3. 실제 파일 경로
            String imagePath = "C:/kd/deploy/mms/storage/" + finalFile;

            // ✅ 4. MMS 발송
            boolean sendResult = mmsSendService.sendMMS(phoneNumber, imagePath);

            // ✅ 5. 로그 기록
            MmsSendLogVO logVO = new MmsSendLogVO();
            logVO.setMimgno(mimgno);
            logVO.setMemberno(memberno);
            logVO.setPhone_number(phoneNumber);
            logVO.setSend_status(sendResult ? "success" : "fail");

            mmsSendLogProc.create(logVO); // <selectKey>로 mslogno 자동 세팅

            // ✅ 6. 응답
            result.put("success", sendResult);
            result.put("status", logVO.getSend_status());
            result.put("logno", logVO.getMslogno()); // 이제 정상적으로 값 나옴

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }



}
