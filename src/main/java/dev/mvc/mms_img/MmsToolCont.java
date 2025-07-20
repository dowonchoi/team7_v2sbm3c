package dev.mvc.mms_img;

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

@Controller
@RequestMapping("/mms_tool")
public class MmsToolCont {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MMSImageService mmsImageService;

    @Autowired
    private MmsSendService mmsSendService;

    private static final String FASTAPI_MMS_IMG_URL = "http://localhost:8000/mms_img";

    /**
     * ✅ 테스트 UI 화면
     */
    @GetMapping("/test")
    public String testPage(Model model) {
        return "mms_img/mms_tool"; // ✅ templates/mms_img/mms_tool.html
    }

    /**
     * ✅ STEP 1: FastAPI → AI 배경 이미지 생성
     */
    @PostMapping("/create")
    @ResponseBody
    public Map<String, Object> createImage(@RequestParam("prompt") String prompt) {
        Map<String, Object> result = new HashMap<>();
        try {
            // ✅ FastAPI 호출
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("SpringBoot_FastAPI_KEY", "YOUR_FASTAPI_KEY");
            body.put("prompt", prompt);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String response = restTemplate.postForObject(FASTAPI_MMS_IMG_URL, requestEntity, String.class);
            JSONObject json = new JSONObject(response);

            String fullPath = json.getString("file_name");
            String fileName = fullPath.substring(fullPath.lastIndexOf("/") + 1);

            result.put("success", true);
            result.put("fileName", fileName);
            result.put("filePath", "/mms_img/" + fileName);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * ✅ STEP 2: 텍스트 합성 (Java)
     */
    @PostMapping("/text")
    @ResponseBody
    public Map<String, Object> addText(@RequestParam("fileName") String fileName,
                                       @RequestParam("messageText") String messageText) {
        Map<String, Object> result = new HashMap<>();
        try {
            String inputPath = "C:/kd/deploy/mms/storage/" + fileName;

            // ✅ 텍스트 합성 & 압축
            String finalFileName = mmsImageService.addTextToImage(inputPath, messageText);

            result.put("success", true);
            result.put("finalFileName", finalFileName);
            result.put("filePath", "/mms_img/" + finalFileName);

        } catch (IOException e) {
            result.put("success", false);
            result.put("error", "이미지 처리 오류: " + e.getMessage());
        }
        return result;
    }

    /**
     * ✅ STEP 3: MMS 발송 (Gabia API)
     */
    @PostMapping("/send")
    @ResponseBody
    public Map<String, Object> sendMMS(@RequestParam("phoneNumber") String phoneNumber,
                                       @RequestParam("finalFileName") String finalFileName) {
        Map<String, Object> result = new HashMap<>();
        try {
            String imagePath = "C:/kd/deploy/mms/storage/" + finalFileName;

            File file = new File(imagePath);
            if (!file.exists()) {
                result.put("success", false);
                result.put("error", "이미지 파일이 존재하지 않습니다.");
                return result;
            }

            // ✅ MMS 발송 서비스 호출
            boolean sendResult = mmsSendService.sendMMS(phoneNumber, imagePath);

            result.put("success", sendResult);
            result.put("message", sendResult ? "MMS 발송 성공" : "MMS 발송 실패");

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "MMS 발송 오류: " + e.getMessage());
        }
        return result;
    }
}
