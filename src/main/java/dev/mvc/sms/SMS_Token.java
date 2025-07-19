package dev.mvc.sms;
 import com.google.gson.Gson;
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

            public class SMS_Token {
              public static final String SMS_OAUTH_TOKEN_URL = "https://sms.gabia.com/oauth/token"; // ACCESS
              //TOKEN 발급 API URL 입니다.

              public static void main(String[] args) throws IOException {
                String smsId = "simh0619sms"; // SMS ID 를 입력해 주세요.
                String apiKey = "6e30ef95ecf78dfec600d2071896c264"; // SMS 관리툴에서 발급받은 API KEY 를 입력해 주세요.
                String authValue =
                Base64.getEncoder().encodeToString(String.format("%s:%s", smsId,
                apiKey).getBytes(StandardCharsets.UTF_8)); // Authorization Header 에 입력할 값입니다.

                // 사용자 인증 API 를 호출합니다.
                OkHttpClient client = new OkHttpClient();

                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("grant_type", "client_credentials")
                //.addHeader("expires_in", "7200")
                .build();

                Request request = new Request.Builder()
                .url(SMS_OAUTH_TOKEN_URL)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", "Basic " + authValue)
                .addHeader("cache-control", "no-cache")
                .build();

                // Response 를 key, value 로 확인하실 수 있습니다.
                Response response = client.newCall(request).execute();
                HashMap<String, String> result = new
                Gson().fromJson(Objects.requireNonNull(response.body()).string(), HashMap.class);
                for(String key : result.keySet()) {
                  System.out.printf("%s: %s%n", key, result.get(key));
                }
                System.out.println("-> access_token: " + result.get("access_token"));
              }
            }