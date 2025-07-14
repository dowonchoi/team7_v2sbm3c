package dev.mvc.review;

import lombok.Data;

@Data
public class ReviewMemberVO {
  private int reviewno;
  private int productsno;
  private int memberno;
  private String content;
  private Integer emotion;
  private String summary;
  private java.util.Date rdate;


  private String mname; // 회원 이름
  private String id;    // 아이디 (이메일 형식)
}
