package dev.mvc.review;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface ReviewDAOInter {

  /**
   * 리뷰 등록
   * @param reviewVO 리뷰 정보
   * @return 등록된 행 수 (1: 성공, 0: 실패)
   */
  public int create(ReviewVO reviewVO);

  /**
   * 특정 상품에 대한 리뷰 목록 조회
   * @param productsno 대상 상품 번호
   * @return 리뷰 리스트 (member.mname 조인 포함 예정)
   */
  public List<ReviewVO> list_by_productsno(int productsno);
  
  public List<ReviewMemberVO> list_join_by_productsno(int productsno);

  //리뷰 1건 조회
  public ReviewVO read(int reviewno); // 수정용 1건 조회
  public int update(ReviewVO reviewVO);   // 리뷰 수정
  public int delete(int reviewno);        // 리뷰 삭제 (선택)

  public ReviewMemberVO read_with_member(int reviewno); // 추가
  
  public int countPurchasedByMember(@Param("memberno") int memberno, @Param("productsno") int productsno);


  /**
   * 리뷰 이미지 파일 정보만 수정
   * @param reviewVO
   * @return 수정된 행 수 (1: 성공)
   */
  public int update_file(ReviewVO reviewVO);
  
}
