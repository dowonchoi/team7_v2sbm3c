package dev.mvc.mms_img;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import dev.mvc.tool.Tool;
import okhttp3.*;

@Service
public class MmsSendService {

    public boolean sendMMS(String phoneNumber, String imagePath) {
      try {
          String smsId = "simh0619sms";
          String accessToken = Tool.getSMSToken();
  
          System.out.println("[DEBUG] Access Token: " + accessToken);
  
          if (accessToken == null || accessToken.isEmpty()) {
              System.out.println("❌ Access Token 발급 실패");
              return false;
          }
  
          String authValue = Base64.getEncoder()
                  .encodeToString((smsId + ":" + accessToken).getBytes(StandardCharsets.UTF_8));
  
          System.out.println("[DEBUG] Authorization 헤더: Basic " + authValue);
  
          OkHttpClient client = new OkHttpClient();
  
          MediaType mediaType = MediaType.parse("image/jpeg");
          File imgFile = new File(imagePath);
  
          System.out.println("[DEBUG] 이미지 파일 크기: " + imgFile.length());
  
          RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                  .addFormDataPart("phone", phoneNumber)
                  .addFormDataPart("callback", "01071481635") // 인증된 발신번호
                  .addFormDataPart("message", "AI MMS 발송 테스트입니다.")
                  .addFormDataPart("subject", "AI MMS 테스트")
                  .addFormDataPart("image_cnt", "1")
                  .addFormDataPart("images0", imgFile.getName(), RequestBody.create(imgFile, mediaType))
                  .build();
  
          Request request = new Request.Builder()
                  .url("https://sms.gabia.com/api/send/mms")
                  .post(requestBody)
                  .addHeader("Authorization", "Basic " + authValue)
                  .addHeader("cache-control", "no-cache")
                  .build();
  
          Response response = client.newCall(request).execute();
          String responseStr = response.body() != null ? response.body().string() : "";
  
          System.out.println("[DEBUG] HTTP Code: " + response.code());
          System.out.println("[Gabia Response] " + responseStr);
  
          if (response.code() != 200) {
              System.out.println("❌ Gabia 서버 오류. HTTP Code: " + response.code());
              return false;
          }
  
          if (responseStr.isEmpty()) {
              System.out.println("❌ Gabia 응답 없음. 발송 실패.");
              return false;
          }
  
          HashMap<String, String> result = new Gson().fromJson(responseStr, HashMap.class);
          return result != null && "200".equals(result.get("code"));
  
      } catch (Exception e) {
          e.printStackTrace();
          return false;
      }
  }

}
