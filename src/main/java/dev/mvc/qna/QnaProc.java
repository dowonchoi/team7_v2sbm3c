package dev.mvc.qna;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("dev.mvc.qna.QnaProc")
public class QnaProc implements QnaProcInter {

  @Autowired
  private QnaDAOInter qnaDAO;

  @Override
  public List<QnaVO> listByUserType(String userType) {
      return this.qnaDAO.listByUserType(userType);
  }

  @Override
  public QnaVO read(int qna_id) {
      return this.qnaDAO.read(qna_id);
  }
  
  @Override
  public void increaseViewCount(int qna_id) {
    qnaDAO.increaseViewCount(qna_id);
  }
  
  @Override
  public int create(QnaVO qnaVO) {
      return this.qnaDAO.create(qnaVO);
  }
  
  @Override
  public List<QnaVO> listByUserTypeAndCate(String userType, String cate) {
      Map<String, Object> map = new HashMap<>();
      map.put("userType", userType);
      map.put("cate", cate);
      return qnaDAO.listByUserTypeAndCate(map);
  }
  
  @Override
  public void update(QnaVO qnaVO) {
      qnaDAO.update(qnaVO);
  }
  
  //QnaProc.java
  @Override
  public int delete(int qna_id) {
     return qnaDAO.delete(qna_id);
  }
    
  @Override
  public void addComment(int qna_id, String comment) {
      qnaDAO.addComment(qna_id, comment);
  }

  /** ✅ 공식 답변(reply) 등록/수정 */
  @Override
  public int updateReply(QnaVO qnaVO) {
      return this.qnaDAO.updateReply(qnaVO);
  }

  /** ✅ 공식 답변(reply) 조회 */
  @Override
  public String getReply(int qna_id) {
      return qnaDAO.getReply(qna_id);
  }
  
  @Override
  public int countByMember(int memberno) {
      return qnaDAO.countByMember(memberno);
  }
}
