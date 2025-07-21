package dev.mvc.cart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface CartProcInter {

  /**
   * 장바구니 항목 추가 (중복 체크 포함 → 있으면 cnt 증가 or 무시)
   * @param cartVO
   * @return 처리 결과 (1: 성공, 0: 실패)
   */
  public int create(CartVO cartVO);

  /**
   * 회원 장바구니 목록 조회 (상품 정보 포함)
   * @param memberno
   * @return 장바구니 목록
   */
  public ArrayList<CartVO> list_by_memberno(int memberno);

  /**
   * 회원 + 상품 조합으로 기존 항목 조회
   * @param map (memberno, productsno)
   * @return CartVO
   */
  public CartVO read_by_memberno_productsno(Map<String, Object> map);

  /**
   * 수량 변경
   * @param map (cartno, cnt)
   * @return 처리된 개수
   */
  public int update_cnt(Map<String, Object> map);

  /**
   * 개별 항목 삭제
   * @param cartno
   * @return 삭제된 개수
   */
  public int delete(int cartno);

  /**
   * 특정 회원 장바구니 전체 삭제
   * @param memberno
   * @return 삭제된 개수
   */
  public int delete_by_memberno(int memberno);

  /**
   * 장바구니 총 결제금액 계산
   * @param memberno
   * @return 총합
   */
  public int sum_total_price(int memberno);
  
  public int update_selected(Map<String, Object> map);
  
  /** 선택된 항목들의 총 적립 포인트 합산 */
  public int sum_total_point(int memberno);
  
  /**
   * 회원 장바구니 정가 기준 총합 계산
   */
  public int sum_total_price_origin(int memberno);
  
  /**
   * 회원 장바구니 총 할인액 계산
   */
  public int sum_total_discount(int memberno);

  //선택된 장바구니 항목 목록 조회 (selected='Y')
  public List<CartVO> list_selected_by_memberno(int memberno);
  
  //선택된 장바구니 항목의 총 금액 계산
  public int total_selected_by_memberno(int memberno);
  
  //선택된 장바구니 항목의 삭제
  public int delete_selected_by_memberno(int memberno);

  public int countItems(int memberno);
  
  /** 특정 회원의 장바구니 상품 개수 */
  public int count_by_member(int memberno);
  
  /** 회원 + 상품별 장바구니 개수 */
  public int countByMemberProduct(Integer memberno, int productsno);

  /** 장바구니 수량 업데이트 */
  public int updateCnt(CartVO cartVO);

  /** 회원별 장바구니 전체 개수 */
  public int countByMember(Integer memberno);


}
