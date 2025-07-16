package dev.mvc.inquiry;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class InquiryVO {
  
  private Integer inquiry_id;
  
  private String title;
  
  private String content;
  
  private String writer_id;
  
  private String writer_name;
  
  private String user_type;
  
  private Integer memberno;
  
  private Date reg_date;
  
  private Integer view_count;  // 조회수
  
  //답변 내용
  private String answer;
  
  //답변 작성자
  private String answer_admin;
  
  //답변 등록일
  private Date answer_date;
}