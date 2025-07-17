package dev.mvc.review;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
 // (1) LLM 분석 → VO에 emotion & summary 세팅
    reviewLLMService.process(reviewVO);

    // (2) DB 저장
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
  
  /**
   *  리뷰 수정
   * 1. LLM 호출 (수정된 content 기반)
   * 2. DB 반영
   */
  @Override
  public int update(ReviewVO reviewVO) {
    // (1) LLM 분석 → VO 업데이트
    reviewLLMService.process(reviewVO);

    // (2) DB 업데이트
    return reviewDAO.update(reviewVO);
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

  /**
   * 리뷰 이미지 파일 정보만 수정
   * @param reviewVO
   * @return 1 (성공), 0 (실패)
   */
  @Override
  public int update_file(ReviewVO reviewVO) {
    return this.reviewDAO.update_file(reviewVO);
  }
  
  @Override
  public List<ReviewMemberVO> list_join_by_productsno_paging(int productsno, int start, int end) {
      return reviewDAO.list_join_by_productsno_paging(productsno, start, end);
  }
  
  @Override
  public List<ReviewMemberVO> list_more(int productsno, int offset, int limit) {
      Map<String, Object> map = new HashMap<>();
      map.put("productsno", productsno);
      map.put("offset", offset);
      map.put("limit", limit);
      return reviewDAO.list_more(map);
  }
  

  

}