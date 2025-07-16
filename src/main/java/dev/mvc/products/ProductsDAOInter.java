package dev.mvc.products;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import dev.mvc.cate.CateVO;

/**
 * Spring Boot가 자동 구현
 * @author soldesk
 *
 */
public interface ProductsDAOInter {
  /**
   * 등록, 추상 메소드
   * @param productsVO
   * @return
   */
  public int create(ProductsVO productsVO);

  /**
   * 모든 카테고리의 등록된 글목록
   * @return
   */
  public ArrayList<ProductsVO> list_all();
  
  /**
   * 카테고리별 등록된 글 목록 (관리자, 일반 회원)
   * @param cateno
   * @return
   */
  public ArrayList<ProductsVO> list_by_cateno(int cateno);
  
  /**
   * 카테고리별 등록된 글 목록 (공급자(member)
   * @param memberno
   * @return
   */
  public ArrayList<ProductsVO> list_by_memberno(int memberno);
  
  /**
   * 조회
   * @param productsno
   * @return
   */
  public ProductsVO read(int productsno);
  
  /**
   * map 등록, 수정, 삭제
   * @param map
   * @return 수정된 레코드 갯수
   */
  public int map(HashMap<String, Object> map);

  /**
   * youtube 등록, 수정, 삭제
   * @param youtube
   * @return 수정된 레코드 갯수
   */
  public int youtube(HashMap<String, Object> map);
  
  /**
   * 카테고리별 검색 목록
   * @param map
   * @return
   */
  public ArrayList<ProductsVO> list_by_cateno_search(HashMap<String, Object> hashMap);
  
  /**
   * 카테고리별 검색된 레코드 갯수
   * @param map
   * @return
   */
  public int list_by_cateno_search_count(HashMap<String, Object> hashMap);
  
  /**
   * 카테고리별 검색 목록 + 페이징
   * @param productsVO
   * @return
   */
  public ArrayList<ProductsVO> list_by_cateno_search_paging(HashMap<String, Object> map);
  
  /**
   * 패스워드 검사
   * @param hashMap
   * @return
   */
  public int password_check(HashMap<String, Object> hashMap);
  
  /**
   * 글 정보 수정
   * @param productsVO
   * @return 처리된 레코드 갯수
   */
  public int update_text(ProductsVO productsVO);

  /**
   * 파일 정보 수정
   * @param productsVO
   * @return 처리된 레코드 갯수
   */
  public int update_file(ProductsVO productsVO);
 
  /**
   * 삭제
   * @param productsno
   * @return 삭제된 레코드 갯수
   */
  public int delete(int productsno);
  
  /**
   * FK cateno 값이 같은 레코드 갯수 산출
   * @param cateno
   * @return
   */
  public int count_by_cateno(int cateno);
 
  /**
   * 특정 카테고리에 속한 모든 레코드 삭제
   * @param cateno
   * @return 삭제된 레코드 갯수
   */
  public int delete_by_cateno(int cateno);
  
  /**
   * 추천수 증가
   * @param productsno
   * @return
   */
  public int increaseRecom(int productsno);
  
  /**
   * 추천수 감소
   * @param productsno
   * @return
   */
  public int decreaseRecom(int productsno);
  
  /**
   * FK memberno 값이 같은 레코드 갯수 산출
   * @param memberno
   * @return
   */
  public int count_by_memberno(int memberno);
 
  /**
   * 특정 카테고리에 속한 모든 레코드 삭제
   * @param memberno
   * @return 삭제된 레코드 갯수
   */
  public int delete_by_memberno(int memberno);
  
  /**
   * 글 수 증가
   * @param 
   * @return
   */ 
  public int increaseReplycnt(int productsno);
 
  /**
   * 글 수 감소
   * @param 
   * @return
   */   
  public int decreaseReplycnt(int productsno);
  
  public ArrayList<ProductsVO> list_by_cateno_except_self(@Param("cateno") int cateno, @Param("productsno") int productsno);

  /*무한 스크롤*/
  public ArrayList<ProductsVO> related_scroll(Map<String, Object> map);

  //제품 번호(productsno)를 기반으로, 해당 상품의 개당 포인트(point)를 조회하는 메서드
  public int getPointByProductsno(int productsno);

  public List<ProductsVO> getRecentlyViewed(int memberno);
  
  public int countPurchasedByMember(@Param("memberno") int memberno, @Param("productsno") int productsno);

}
 
 