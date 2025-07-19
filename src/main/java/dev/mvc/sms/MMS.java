package dev.mvc.sms;

import com.google.gson.Gson;

import dev.mvc.tool.Tool;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Objects;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// 외부 라이브러리 다운로드가 필요합니다. (gradle 기준)
// implementation 'com.squareup.okhttp3:okhttp:4.9.3'
//https://mvnrepository.com/artifact/com.squareup.okhttp/okhttp
// implementation 'com.google.code.gson:gson:2.9.0' https://github.com/google/gson

public class MMS {
  public static final String MMS_SEND_URL = "https://sms.gabia.com/api/send/mms"; // MMS 발송 API URL입니다.

  public static void main(String[] args) throws IOException {
    String smsId = "simh0619sms"; // SMS ID 를 입력해 주세요.
    String accessToken = Tool.getSMSToken(); // ACCESS TOKEN 을 입력해 주세요.
    String authValue = Base64.getEncoder()
        .encodeToString(String.format("%s:%s", smsId, accessToken).getBytes(StandardCharsets.UTF_8));

    // MMS 발송 API 를 호출합니다.
    OkHttpClient client = new OkHttpClient();

    RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
        .addFormDataPart("phone", "01071481635") // 수신번호를 입력해 주세요. (수신번호가 두 개 이상인 경우 ',' 를 이용하여 입력합니다. ex)
                                                          // 01011112222,01033334444)
        .addFormDataPart("callback", "01071481635") // 발신번호를 입력해 주세요.
        .addFormDataPart("message", "MMS 테스트") // MMS 내용을 입력해 주세요.
        .addFormDataPart("refkey", "1") // 발송 결과 조회를 위한 임의의 랜덤 키를 입력해 주세요.
        .addFormDataPart("subject", "SoOn 마켓 세일 시작") // MMS 제목을 입력해 주세요.
        .addFormDataPart("image_cnt", "1") // 이미지 첨부 개수를 입력해 주세요. (최대 3개까지 첨부 가능합니다.)
        //.addFormDataPart("images0", "img.jpg", RequestBody.create(new File("C:\\kd\\deploy\\mms\\storage\\img.jpg"), MultipartBody.FORM)) // 이미지
        .addFormDataPart("images0", "img.jpg", RequestBody.create(new File("C:\\kd\\deploy\\mms\\storage\\sample_img.jpg"), MultipartBody.FORM)) // 이미지
        //.addFormDataPart("images1", "FILE_NAME", RequestBody.create(new File("FILE_PATH"), MultipartBody.FORM)) // 이미지                                               
        //.addFormDataPart("images2", "FILE_NAME", RequestBody.create(new File("FILE_PATH"), MultipartBody.FORM)) // 이미지                                                                
        .build();

    Request request = new Request.Builder().url(MMS_SEND_URL).post(requestBody)
        .addHeader("Authorization", "Basic " + authValue).build();

    Response response = client.newCall(request).execute();

    // Response 를 key, value 로 확인하실 수 있습니다.
    HashMap<String, String> result = new Gson().fromJson(Objects.requireNonNull(response.body()).string(),
        HashMap.class);
    for (String key : result.keySet()) {
      System.out.printf("%s: %s%n", key, result.get(key));
    }
  }
}