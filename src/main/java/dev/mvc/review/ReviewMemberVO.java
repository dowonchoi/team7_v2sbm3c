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
  
  // 이미지 관련 필드
  private String file1;
  private String file1saved;
  private long size1;

  private String file2;
  private String file2saved;
  private long size2;

  private String file3;
  private String file3saved;
  private long size3;
}
