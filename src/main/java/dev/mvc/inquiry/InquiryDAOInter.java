package dev.mvc.inquiry;

import java.util.List;

public interface InquiryDAOInter {
  
  public int create(InquiryVO inquiryVO);
  
  public InquiryVO read(int inquiry_id);
  
  public int update(InquiryVO inquiryVO);  // 수정
  
  public int delete(int inquiry_id);  // 삭제
  
  public int deleteAnswer(int inquiry_id);
  
  public int countByMember(int memberno);
  
  public List<InquiryVO> listByMember(int memberno);
  
  public int updateViewCount(int inquiry_id);
  
//  public List<InquiryVO> list_by_member(int memberno);
  
  public int updateAnswer(InquiryVO inquiryVO);  // answer, answer_date, answer_admin 업데이트
  
  public List<InquiryVO> list_all();

}
