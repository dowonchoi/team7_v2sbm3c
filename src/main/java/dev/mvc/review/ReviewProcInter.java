package dev.mvc.review;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface ReviewProcInter {
  public int create(ReviewVO reviewVO); // 리뷰 등록
  public List<ReviewVO> list_by_productsno(int productsno); // 해당 상품의 모든 리뷰
  
  public List<ReviewMemberVO> list_join_by_productsno(int productsno);

  public ReviewVO read(int reviewno); // 리뷰  조회
  public int update(ReviewVO reviewVO);      // 리뷰 수정
  public int delete(int reviewno);           // 리뷰 삭제
  
  public ReviewMemberVO read_with_member(int reviewno); // 추가
  
  public int countPurchasedByMember(@Param("memberno") int memberno, @Param("productsno") int productsno);

}
