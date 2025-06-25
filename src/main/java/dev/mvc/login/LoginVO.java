package dev.mvc.login;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class LoginVO {
  /*
  loginno                        NUMBER(10) NOT NULL PRIMARY KEY,
  ip                               VARCHAR2(15) NOT NULL,
  id                               VARCHAR(30) NOT NULL,
  sw                              VARCHAR(1) DEFAULT 'N' NOT NULL,
  logindate                     DATE NOT NULL
  */
  
  /** 로그인 번호 */
  private int loginno;
  
  /** 회원 번호 */
  private int memberno; 
  
  /** 접속 IP */
  private String ip;
  
  /** 회원 아이디 */
  private String id;
  
  /** 로그인 성공 여부 (Y/N) */
  private String sw;
  
  /** 로그인 날짜 */
  private Date logindate;
}