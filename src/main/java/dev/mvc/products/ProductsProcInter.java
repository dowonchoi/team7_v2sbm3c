package dev.mvc.products;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 개발자가 구현합니다.
 * @author soldesk
 *
 */
public interface ProductsProcInter {
  /**
   * 등록
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
   * 카테고리별 등록된 글 목록
   * @param cateno
   * @return
   */
  public ArrayList<ProductsVO> list_by_cateno(int cateno);
 
  /**
   * 카테고리별 등록된 글 목록
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
   * SPAN태그를 이용한 박스 모델의 지원, 1 페이지부터 시작 
   * 현재 페이지: 11 / 22   [이전] 11 12 13 14 15 16 17 18 19 20 [다음] 
   *
   * @param cateno 카테고리 번호
   * @param now_page 현재 페이지
   * @param word 검색어
   * @param list_file 목록 파일명
   * @param search_count 검색 레코드수   
   * @param record_per_page 페이지당 레코드 수
   * @param page_per_block 블럭당 페이지 수
   * @return 페이징 생성 문자열
   */ 
  public String pagingBox(int cateno, int now_page, String word, String list_file, int search_count, 
                                      int record_per_page, int page_per_block);   

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
  
  public ArrayList<ProductsVO> list_by_cateno_except_self(int cateno, int productsno);

  public ArrayList<ProductsVO> related_scroll(Map<String, Object> map);

}
