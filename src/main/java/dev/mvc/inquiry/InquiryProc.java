package dev.mvc.inquiry;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("dev.mvc.inquiry.InquiryProc")
public class InquiryProc implements InquiryProcInter {

  @Autowired
  private InquiryDAOInter inquiryDAO;

  @Override
  public int create(InquiryVO inquiryVO) {
    return inquiryDAO.create(inquiryVO);
  }

  @Override
  public InquiryVO read(int inquiry_id) {
    return inquiryDAO.read(inquiry_id);
  }
  
  @Override
  public int update(InquiryVO inquiryVO) {
      return inquiryDAO.update(inquiryVO);  // DAO 호출
  }
  
  @Override
  public int delete(int inquiry_id) {
      return inquiryDAO.delete(inquiry_id);  // DAO 위임
  }

  @Override
  public int countByMember(int memberno) {
    return inquiryDAO.countByMember(memberno);
  }

  @Override
  public List<InquiryVO> listByMember(int memberno) {
    return inquiryDAO.listByMember(memberno);
  }

  @Override
  public int updateViewCount(int inquiry_id) {
    return inquiryDAO.updateViewCount(inquiry_id);
  }

  // ✅ 관리자 답변 등록
  @Override
  public int updateAnswer(InquiryVO inquiryVO) {
    return inquiryDAO.updateAnswer(inquiryVO);
  }
  
  @Override
  public int deleteAnswer(int inquiry_id) {
      return inquiryDAO.deleteAnswer(inquiry_id);
  }
  
  @Override
  public List<InquiryVO> list_all() {
      return inquiryDAO.list_all();
  }

}
