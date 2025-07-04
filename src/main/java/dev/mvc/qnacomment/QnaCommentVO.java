package dev.mvc.qnacomment;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class QnaCommentVO {
  
    private int comment_id;
    
    private int qna_id;
    
    private String content;
    
    private Date reg_date;
}