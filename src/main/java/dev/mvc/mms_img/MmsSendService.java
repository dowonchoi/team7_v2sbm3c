package dev.mvc.mms_img;

import org.springframework.stereotype.Service;

@Service
public class MmsSendService {

    public boolean sendMMS(String phoneNumber, String imagePath) {
        System.out.println("[MmsSendService] 발송 번호: " + phoneNumber);
        System.out.println("[MmsSendService] 이미지 경로: " + imagePath);

        // TODO: 가비아 MMS API 연동 구현
        return true; // 테스트 단계에서는 항상 성공
    }
}
