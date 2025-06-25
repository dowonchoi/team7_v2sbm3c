package dev.mvc.member;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class MemberVO {
  /*
  memberno NUMBER(10) NOT NULL, -- 회원 번호, 레코드를 구분하는 컬럼 
  id         VARCHAR(30)   NOT NULL UNIQUE, -- 이메일(아이디), 중복 안됨, 레코드를 구분 
  passwd     VARCHAR(60)   NOT NULL, -- 패스워드, 영숫자 조합, 암호화
  mname      VARCHAR(30)   NOT NULL, -- 성명, 한글 10자 저장 가능
  tel            VARCHAR(14)   NOT NULL, -- 전화번호
  zipcode     VARCHAR(5)        NULL, -- 우편번호, 12345
  address1    VARCHAR(80)       NULL, -- 주소 1
  address2    VARCHAR(50)       NULL, -- 주소 2
  mdate       DATE             NOT NULL, -- 가입일    
  grade        NUMBER(2)     NOT NULL, -- 등급(1~10: 관리자, 11~20: 회원, 40~49: 정지 회원, 99: 탈퇴 회원)
  */

    /** 회원 번호 */
    private int memberno;
    /** 아이디 */
    private String id = "";
    /** 이메일 */
    private String email = "";
    /** 패스워드 */
    private String passwd = "";
    /** 회원 성명 */
    private String mname = "";
    /** 전화 번호 */
    private String tel = "";
    /** 우편 번호 */
    private String zipcode = "";
    /** 주소 1 */
    private String address1 = "";
    /** 주소 2 */
    private String address2 = "";
    /** 가입일 */
    private String mdate = "";
    /** 등급 */
    private int grade = 0;

    /** 등록된 패스워드 */
    private String old_passwd = "";
    /** id 저장 여부 */
    private String id_save = "";
    /** passwd 저장 여부 */
    private String passwd_save = "";
    /** 이동할 주소 저장 */
    private String url_address = "";
    
    /** 공급자 승인 여부: 'Y' 또는 'N' */
    private String supplier_approved = "N";

    /** 사업자 인증 파일 경로 */
    private MultipartFile business_fileMF;
    
    // 업로드용 필드, Controller에서 파일 받을 때 사용
    private String business_file = "";

    private String business_file_origin = ""; // 원본 파일명 (다운로드 시 표시용)
    
    /** 🔥 파일 확장자 (템플릿에서 용도에 따라 표시하거나 조건 처리) */
    private String fileExt = "";
}