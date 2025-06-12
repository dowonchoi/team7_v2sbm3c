package dev.mvc.productsgood;

import java.util.ArrayList;
import java.util.HashMap;

public interface ProductsgoodDAOInter {
  /**
   * 등록, 추상 메소드
   * @param productsgoodVO
   * @return
   */
  public int create(ProductsgoodVO productsgoodVO);
  
  /**
   * 모든 목록
   * @return
   */
  public ArrayList<ProductsgoodVO> list_all();
  
  /**
   * 삭제
   * @param productsgoodno
   * @return
   */
  public int delete(int productsgoodno);
  
  /**
   * 특정 컨텐츠의 특정 회원 추천 갯수 산출
   * @param map
   * @return
   */
  public int hartCnt(HashMap<String, Object> map);  

  /**
   * 조회
   * @param productsgoodno
   * @return
   */
  public ProductsgoodVO read(int productsgoodno);

  /**
   * productsno, memberno로 조회
   * @param map
   * @return
   */
  public ProductsgoodVO readByproductsnoMemberno(HashMap<String, Object> map);
  
  /**
   * 모든 목록, 테이블 3개 join
   * @return
   */
  public ArrayList<productsproductsgoodMemberVO> list_all_join();
  
}




