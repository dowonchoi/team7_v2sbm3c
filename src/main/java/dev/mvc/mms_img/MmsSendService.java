package dev.mvc.mms_img;

import com.google.gson.Gson;
import dev.mvc.tool.Tool;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Objects;

/**
 * MmsSendService
 * -----------------------------------------------------------
 * 주요 역할:
 * - Gabia MMS API를 이용하여 MMS 발송 처리
 * - OkHttp 라이브러리를 사용해 multipart/form-data 방식 요청
 * - Basic 인증 방식으로 API 호출
 *
 * 전체 흐름:
 * 1) Gabia API 인증 값 생성 (아이디 + 토큰 → Base64 인코딩)
 * 2) 이미지 파일 확인
 * 3) MultipartBody로 요청 본문 구성
 * 4) OkHttpClient로 API 요청 전송
 * 5) 응답(JSON)을 파싱해 성공 여부 반환
 * -----------------------------------------------------------
 */
@Service
public class MmsSendService {

    /** Gabia MMS 발송 API URL */
    private static final String MMS_SEND_URL = "https://sms.gabia.com/api/send/mms";

    /**
     * MMS 발송 메서드
     *
     * @param phoneNumber MMS 수신자 휴대폰 번호
     * @param imagePath   전송할 이미지 파일의 경로
     * @return true: 발송 성공, false: 실패
     * @throws IOException 이미지 파일 미존재 또는 API 요청 실패 시 발생
     */
    public boolean sendMMS(String phoneNumber, String imagePath) throws IOException {
        // 1. 인증 정보 준비
        // Gabia API는 Basic Auth 방식을 사용하며, "아이디:토큰"을 Base64로 인코딩해야 한다.
        String smsId = "simh0619sms"; // Gabia 계정 아이디
        String accessToken = Tool.getSMSToken(); // Gabia API 토큰 (별도 관리)
        String authValue = Base64.getEncoder()
                .encodeToString(String.format("%s:%s", smsId, accessToken).getBytes(StandardCharsets.UTF_8));

        // 2. OkHttpClient 초기화 (HTTP 요청을 처리하기 위한 클라이언트)
        OkHttpClient client = new OkHttpClient();
        
        // 3. 이미지 파일 존재 여부 확인
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            throw new IOException("이미지 파일이 존재하지 않습니다: " + imageFile.getAbsolutePath());
        }

        // 4. Multipart 요청 본문 구성
        // Gabia MMS API는 multipart/form-data 형식을 요구한다.
        // 필수 파라미터: phone, callback, message, subject, image_cnt, images0
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("phone", phoneNumber)               // 수신자 번호
            .addFormDataPart("callback", "01071481635")         // 발신자 번호
            .addFormDataPart("message", "SoOn 마켓! 여름 이벤트") // MMS 본문 메시지
            .addFormDataPart("refkey", "12345")                 // 참조용 키 (옵션)
            .addFormDataPart("subject", "여름 특가 세일!")         // MMS 제목
            .addFormDataPart("image_cnt", "1")                  // 첨부 이미지 개수
            .addFormDataPart("images0", imageFile.getName(),    // 첨부 이미지
                    RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
            .build();

        // 5. HTTP 요청 객체 생성
        // Authorization 헤더에 Basic Auth 값 추가
        Request request = new Request.Builder()
                .url(MMS_SEND_URL)
                .post(requestBody)
                .addHeader("Authorization", "Basic " + authValue)
                .build();

        // 6. API 요청 실행
        // OkHttp 동기 방식 요청 → 응답 본문(JSON) 문자열로 변환
        Response response = client.newCall(request).execute();
        String responseBody = Objects.requireNonNull(response.body()).string();
        System.out.println("Gabia Response: " + responseBody);
        
        // 7. 응답 파싱
        // Gabia API 응답 예시: {"code":"200","message":"success","data":{...}}
        HashMap<String, Object> result = new Gson().fromJson(responseBody, HashMap.class);
        
        // 8. 성공 여부 확인
        // 응답 코드(code)가 "200"이면 성공
        return "200".equals(result.get("code"));
    }
}
