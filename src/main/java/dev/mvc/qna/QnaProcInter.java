package dev.mvc.qna;

import java.util.List;
import java.util.Map;

public interface QnaProcInter {
    
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
  
  public void increaseViewCount(int qna_id);
    
  /** Q&A 등록 */
  public int create(QnaVO qnaVO);
  
  /** 사용자 유형 + 카테고리별 목록 조회 */
  public List<QnaVO> listByUserTypeAndCate(String userType, String cate);

  /** Q&A 수정 */
  public void update(QnaVO qnaVO);
  
  public int delete(int qna_id);
  
  /** 댓글 내용 업데이트 (※ 댓글 테이블 따로 쓰는 중이라면 사실상 비추천) */
  public void addComment(int qna_id, String comment);
  
  /** ✅ 공식 답변(reply) 등록/수정 */
  public int updateReply(QnaVO qnaVO);
  
  /** ✅ 공식 답변(reply) 조회 */
  public String getReply(int qna_id);
  
  public int countByMember(int memberno);
}
