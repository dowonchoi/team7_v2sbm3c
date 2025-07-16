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

  /**
   * 리뷰 이미지 파일 정보만 수정
   * @param reviewVO
   * @return 수정된 행 수 (1: 성공)
   */
  public int update_file(ReviewVO reviewVO);
  
  public List<ReviewMemberVO> list_join_by_productsno_paging(
      @Param("productsno") int productsno,
      @Param("start") int start,
      @Param("end") int end);

  List<ReviewMemberVO> list_more(int productsno, int offset, int limit);

}
