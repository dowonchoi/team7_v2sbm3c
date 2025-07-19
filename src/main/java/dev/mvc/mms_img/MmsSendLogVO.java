package dev.mvc.mms_img;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MmsSendLogVO {
    private int mslogno;         // 발송 로그 번호
    private int mimgno;          // 이미지 번호
    private int memberno;        // 관리자 번호
    private String phone_number; // 수신자 번호
    private String send_status;  // success/fail
    private String send_time;    // 발송 시간
}
