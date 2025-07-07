package dev.mvc.faq;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter @Getter @ToString
public class FaqVO {
  
    private int faq_id;
    
    private String cate;
    
    private String question;
    
    private String answer;
    
    private String writer_id;
    
    private String reg_date;
}