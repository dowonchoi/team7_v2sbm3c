package dev.mvc.qna;

import java.util.List;
import java.util.Map;

public interface QnaDAOInter {
    
  /**
   * 사용자 유형별 Q&A 목록 조회 (user / supplier)
   * @param userType 사용자 유형
   * @return Q&A 목록
   */
  public List<QnaVO> listByUserType(String userType);
  
  /**
   * Q&A 상세 조회 (모두 열람 가능)
   * @param qna_id Q&A 번호
   * @return Q&A 정보
   */
  public QnaVO read(int qna_id);
  
  public int create(QnaVO qnaVO);

  public List<QnaVO> listByUserTypeAndCate(Map<String, Object> map);

  public void update(QnaVO qnaVO);
  
  public void addComment(int qna_id, String comment);


}
