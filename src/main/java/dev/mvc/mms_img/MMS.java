package dev.mvc.mms_img;

import com.google.gson.Gson;
import dev.mvc.tool.Tool;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Objects;

/**
 * MMS 발송 테스트 클래스
 * - Gabia SMS/MMS API 연동
 * - OkHttp 기반 Multipart 요청
 * - 주요 기능:
 *   1) Access Token 발급 (Tool.getSMSToken)
 *   2) Authorization 헤더 생성 (Base64 인코딩)
 *   3) 이미지 첨부 MMS 발송
 *   4) JSON 응답 파싱 및 결과 출력
 *
 * API 문서: https://sms.gabia.com/
 */
public class MMS {
    /** Gabia MMS API 요청 URL */
    public static final String MMS_SEND_URL = "https://sms.gabia.com/api/send/mms";

    /**
     * Gabia MMS 발송 절차
     * <처리 순서>
     * 1. Gabia 계정 정보(SMS ID, Access Token) 준비
     * 2. Authorization 헤더 생성 (Basic {Base64(smsId:AccessToken)})
     * 3. 이미지 파일 유효성 검사
     * 4. OkHttp Multipart 요청 생성
     * 5. MMS 발송 API 호출
     * 6. 응답(JSON) 파싱 후 결과 출력
     *
     * @throws IOException HTTP 요청/응답 과정에서 예외 발생 가능
     */
    public static void main(String[] args) throws IOException {
        //   1. Gabia 계정 정보
        String smsId = "simh0619sms"; // SMS ID
        String accessToken = Tool.getSMSToken(); // Access Token (Tool.java에서 발급)
        System.out.println("  Access Token: " + accessToken);

        //   2. Authorization 헤더 (Base64 인코딩)
        // 형식: Basic {Base64(smsId:accessToken)}
        String authValue = Base64.getEncoder()
                .encodeToString(String.format("%s:%s", smsId, accessToken).getBytes(StandardCharsets.UTF_8));

        //   3. OkHttp Client 생성
        OkHttpClient client = new OkHttpClient();

        //   4. 이미지 파일 경로 확인
        //File imageFile = new File("C:\\kd\\deploy\\mms\\storage\\mms_output.jpg");
        File imageFile = new File("/home/ubuntu/deploy/mms/storage/mms_output.jpg");

        // 파일 존재 여부 확인
        if (!imageFile.exists()) {
            System.out.println("❌ 이미지 파일이 존재하지 않습니다: " + imageFile.getAbsolutePath());
            return;
        }
        System.out.println("  이미지 파일 크기: " + imageFile.length() / 1024 + " KB");

        // (5) Multipart Request Body 생성
        // - phone: 수신자 번호
        // - callback: 발신자 번호
        // - message: MMS 본문
        // - subject: MMS 제목
        // - image_cnt: 첨부 이미지 개수
        // - images0: 첨부 이미지 파일 (JPEG)
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("phone", "01071481635")          // 수신번호
            .addFormDataPart("callback", "01071481635")       // 발신번호
            .addFormDataPart("message", "SoOn 마켓! 여름 이벤트") // MMS 본문
            .addFormDataPart("refkey", "12345")              // 발송 조회용 키 (옵션)
            .addFormDataPart("subject", "여름 특가 세일!")       // MMS 제목
            .addFormDataPart("image_cnt", "1")               // 첨부 이미지 개수
            .addFormDataPart("images0", imageFile.getName(),
                    RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
            .build();

        //   6. HTTP 요청
        Request request = new Request.Builder()
                .url(MMS_SEND_URL)
                .post(requestBody)
                .addHeader("Authorization", "Basic " + authValue)
                .build();

        //   7. API 요청 실행
        Response response = client.newCall(request).execute();
        String responseBody = Objects.requireNonNull(response.body()).string();

        //   8. 응답 출력
        System.out.println("Gabia Response: " + responseBody);

        //   9. JSON 파싱 후 결과 확인
        HashMap<String, Object> result = new Gson().fromJson(responseBody, HashMap.class);
        System.out.println("✔ 결과 코드: " + result.get("code"));
        System.out.println("✔ 메시지: " + result.get("message"));
    }
}
