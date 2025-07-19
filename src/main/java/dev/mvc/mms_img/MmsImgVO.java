package dev.mvc.mms_img;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MmsImgVO {
    private int mimgno;               // MMS 이미지 번호
    private int memberno;             // 관리자 회원번호
    private String prompt;            // OpenAI 프롬프트
    private String message_text;      // 합성 텍스트
    private String original_filename; // 원본 이미지 파일명
    private String final_filename;    // 합성 이미지 파일명
    private String filepath;          // 저장 경로
    private String status;            // 상태
    private String rdate;             // 등록일
}
