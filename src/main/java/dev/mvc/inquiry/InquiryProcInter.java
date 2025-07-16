package dev.mvc.inquiry;

import java.util.List;

public interface InquiryProcInter {
  
  public int create(InquiryVO inquiryVO);                 // 문의 등록
  
  public InquiryVO read(int inquiry_id);                  // 상세 보기
  
  public int update(InquiryVO inquiryVO);  // 수정
  
  public int delete(int inquiry_id);  // 삭제
  
  public int countByMember(int memberno);                 // 내가 작성한 문의 수
  
  public List<InquiryVO> listByMember(int memberno);      // 내가 작성한 문의 목록
  
  public int updateViewCount(int inquiry_id);             // 조회수 증가

  public int updateAnswer(InquiryVO inquiryVO);           // ✍ 관리자 답변 등록
  
  public int deleteAnswer(int inquiry_id); // 관리자 답변 삭제
  
  public List<InquiryVO> list_all();
}
