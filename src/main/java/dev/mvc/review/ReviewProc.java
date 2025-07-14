package dev.mvc.review;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 리뷰 처리 구현체
 */
@Service("dev.mvc.review.ReviewProc")
public class ReviewProc implements ReviewProcInter {

  @Autowired
  private ReviewDAOInter reviewDAO;

  @Autowired
  private ReviewLLMService reviewLLMService;

  @Override
  public int create(ReviewVO reviewVO) {
    String content = reviewVO.getContent();

    // 감정 분석 & 요약
    int emotion = reviewLLMService.analyzeEmotion(content);
    String summary = reviewLLMService.summarizeContent(content);

    reviewVO.setEmotion(emotion);
    reviewVO.setSummary(summary);

    return reviewDAO.create(reviewVO);
  }

  @Override
  public List<ReviewVO> list_by_productsno(int productsno) {
    return reviewDAO.list_by_productsno(productsno);
  }
  
  @Override
  public List<ReviewMemberVO> list_join_by_productsno(int productsno) {
    return reviewDAO.list_join_by_productsno(productsno);
  }
  
  @Override
  public ReviewVO read(int reviewno) {
    return this.reviewDAO.read(reviewno);
  }
  
  @Override
  public int update(ReviewVO reviewVO) {
    return this.reviewDAO.update(reviewVO);
  }
  
  @Override
  public int delete(int reviewno) {
    return reviewDAO.delete(reviewno);
  }
  
  @Override
  public ReviewMemberVO read_with_member(int reviewno) {
    return this.reviewDAO.read_with_member(reviewno); // DAO 호출
  }

  public int countPurchasedByMember(int memberno, int productsno) {
    return this.reviewDAO.countPurchasedByMember(memberno, productsno);
  }

  

}
