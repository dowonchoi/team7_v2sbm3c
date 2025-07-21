package dev.mvc.productsgood;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dev.mvc.products.ProductsVO;
import dev.mvc.productsgood.ProductsproductsgoodMemberVO;

public interface ProductsgoodProcInter {
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
  public ProductsgoodVO readByProductsnoMemberno(HashMap<String, Object> map);
  
  /**
   * 모든 목록, 테이블 3개 join
   * @return
   */
  public ArrayList<ProductsproductsgoodMemberVO> list_all_join();
  
  /**
   * productsno + memberno 조합으로 추천 여부 조회
   * @param productsno
   * @param memberno
   * @return ProductsgoodVO
   */
  public ProductsgoodVO readByProductsnoMemberno(int productsno, int memberno);

  /**
   * productsno + memberno 조합으로 추천 삭제
   * @param productsno
   * @param memberno
   * @return 삭제된 레코드 수
   */
  public int deleteByProductsnoMemberno(int productsno, int memberno);

  public ArrayList<ProductsVO> list_user_liked_products(int memberno);

  public ArrayList<ProductsVO> list_supplier_products_liked(int memberno);
  
  public List<ProductsVO> getProductsgoodByMember(int memberno);

  public List<ProductsVO> search(String word);
  
}


