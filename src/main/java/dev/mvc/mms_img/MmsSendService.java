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

@Service
public class MmsSendService {

    private static final String MMS_SEND_URL = "https://sms.gabia.com/api/send/mms";

    public boolean sendMMS(String phoneNumber, String imagePath) throws IOException {
        String smsId = "simh0619sms";
        String accessToken = Tool.getSMSToken();
        String authValue = Base64.getEncoder()
                .encodeToString(String.format("%s:%s", smsId, accessToken).getBytes(StandardCharsets.UTF_8));

        OkHttpClient client = new OkHttpClient();
        File imageFile = new File(imagePath);

        if (!imageFile.exists()) {
            throw new IOException("이미지 파일이 존재하지 않습니다: " + imageFile.getAbsolutePath());
        }

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("phone", phoneNumber)
                .addFormDataPart("callback", "01071481635") // 발신번호
                .addFormDataPart("message", "SoOn 마켓! 여름 이벤트")
                .addFormDataPart("refkey", "12345")
                .addFormDataPart("subject", "여름 특가 세일!")
                .addFormDataPart("image_cnt", "1")
                .addFormDataPart("images0", imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
                .build();

        Request request = new Request.Builder()
                .url(MMS_SEND_URL)
                .post(requestBody)
                .addHeader("Authorization", "Basic " + authValue)
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = Objects.requireNonNull(response.body()).string();
        System.out.println("Gabia Response: " + responseBody);

        HashMap<String, Object> result = new Gson().fromJson(responseBody, HashMap.class);
        return "200".equals(result.get("code"));
    }
}
