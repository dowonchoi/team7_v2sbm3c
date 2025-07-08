package dev.mvc.cart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface CartDAOInter {
  
  /**
   * 장바구니에 상품 추가 (중복 포함)
   * @param cartVO
   * @return 등록된 레코드 수
   */
  public int create(CartVO cartVO);

  /**
   * 특정 회원의 장바구니 전체 조회 (상품 정보 포함)
   * @param memberno
   * @return 장바구니 목록
   */
  public ArrayList<CartVO> list_by_memberno(int memberno);

  /**
   * 동일 회원 + 상품 조합의 장바구니 항목이 있는지 확인
   * @param map (memberno, productsno)
   * @return CartVO or null
   */
  public CartVO read_by_memberno_productsno(Map<String, Object> map);

  /**
   * 수량 변경
   * @param map (cartno, cnt)
   * @return 수정된 레코드 수
   */
  public int update_cnt(Map<String, Object> map);

  /**
   * 장바구니 항목 삭제 (개별)
   * @param cartno
   * @return 삭제된 레코드 수
   */
  public int delete(int cartno);

  /**
   * 특정 회원의 장바구니 전체 삭제
   * @param memberno
   * @return 삭제된 레코드 수
   */
  public int delete_by_memberno(int memberno);

  /**
   * 장바구니 총합 계산 (상품단가 * 수량)
   * @param memberno
   * @return 총 결제 금액
   */
  public int sum_total_price(int memberno);
  
  /**
   * selected 상태 변경 (Y <-> N)
   * @param map (cartno, selected)
   * @return 변경된 레코드 수
   */
  public int update_selected(Map<String, Object> map);
  
  /**
   * 회원의 장바구니 정가 기준 총합 계산
   * @param memberno
   * @return 총 정가
   */
  public int sum_total_price_origin(int memberno);

  /**
   * 회원 장바구니 총 할인액 계산
   * @param memberno
   * @return 총 할인 금액
   */
  public int sum_total_discount(int memberno);

  //선택된 장바구니 항목만 조회
  public List<CartVO> list_selected_by_memberno(int memberno);
  
  //선택된 장바구니 항목의 총 결제 금액 계산
  public int total_selected_by_memberno(int memberno);

  //선택된 장바구니 항목 삭제
  public int delete_selected_by_memberno(int memberno);

}
