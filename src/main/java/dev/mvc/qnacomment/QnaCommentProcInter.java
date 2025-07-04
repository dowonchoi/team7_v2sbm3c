package dev.mvc.qnacomment;

import java.util.List;

public interface QnaCommentProcInter {
  
  public List<QnaCommentVO> listByQnaId(int qna_id);
  
  public int create(QnaCommentVO vo);
  
  public int update(QnaCommentVO vo);
  
  public int delete(int comment_id);
  
}