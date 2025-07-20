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

public class MMS {
    public static final String MMS_SEND_URL = "https://sms.gabia.com/api/send/mms";

    public static void main(String[] args) throws IOException {
        // ✅ 1. Gabia 계정 정보
        String smsId = "simh0619sms"; // SMS ID
        String accessToken = Tool.getSMSToken(); // Access Token (Tool.java에서 발급)
        System.out.println("✅ Access Token: " + accessToken);

        // ✅ 2. Authorization 헤더 (Base64 인코딩)
        String authValue = Base64.getEncoder()
                .encodeToString(String.format("%s:%s", smsId, accessToken).getBytes(StandardCharsets.UTF_8));

        // ✅ 3. OkHttp Client 생성
        OkHttpClient client = new OkHttpClient();

        // ✅ 4. 이미지 파일 경로 확인
        File imageFile = new File("C:\\kd\\deploy\\mms\\storage\\mms_output.jpg");
        if (!imageFile.exists()) {
            System.out.println("❌ 이미지 파일이 존재하지 않습니다: " + imageFile.getAbsolutePath());
            return;
        }
        System.out.println("✅ 이미지 파일 크기: " + imageFile.length() / 1024 + " KB");

        // ✅ 5. Multipart Request 생성
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("phone", "01071481635") // ✅ 수신번호
                .addFormDataPart("callback", "01071481635") // ✅ 발신번호
                .addFormDataPart("message", "SoOn 마켓! 여름 이벤트") // ✅ MMS 본문
                .addFormDataPart("refkey", "12345") // ✅ 발송 조회용 키
                .addFormDataPart("subject", "여름 특가 세일!") // ✅ MMS 제목
                .addFormDataPart("image_cnt", "1") // ✅ 이미지 개수
                .addFormDataPart("images0", imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
                .build();

        // ✅ 6. HTTP 요청
        Request request = new Request.Builder()
                .url(MMS_SEND_URL)
                .post(requestBody)
                .addHeader("Authorization", "Basic " + authValue)
                .build();

        // ✅ 7. API 요청 실행
        Response response = client.newCall(request).execute();
        String responseBody = Objects.requireNonNull(response.body()).string();

        // ✅ 8. 응답 출력
        System.out.println("Gabia Response: " + responseBody);

        // ✅ 9. JSON 파싱 후 결과 확인
        HashMap<String, Object> result = new Gson().fromJson(responseBody, HashMap.class);
        System.out.println("✔ 결과 코드: " + result.get("code"));
        System.out.println("✔ 메시지: " + result.get("message"));
    }
}
