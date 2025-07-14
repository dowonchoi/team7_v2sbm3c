package dev.mvc.review;

import lombok.Data;

@Data
public class ReviewVO {
  private int reviewno;
  private int productsno;
  private int memberno;
  private String content;
  private Integer emotion;     // 1 or 0
  private String summary;
  private String rdate;
  private String mname;  // 조인용: 회원 이름
  private String title;  // 조인용: 상품명
  
  
}
