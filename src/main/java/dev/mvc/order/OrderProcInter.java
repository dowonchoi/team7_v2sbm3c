package dev.mvc.order;

import java.util.List;

public interface OrderProcInter {
  /**
   * 주문 생성
   * @param orderVO 주문 정보
   * @return 등록된 행 수
   */
  public int create(OrderVO orderVO);
  
  //회원 번호로 주문 목록 조회
  public List<OrderVO> list_by_memberno(int memberno);
  
  // 주문번호로 주문 정보 조회
  public OrderVO read(int orderno); 

  public List<OrderVO> getRecentOrders(int memberno);
  
  public int countOrders(int memberno);
  
  public int countCancelledOrders(int memberno);
  
  //내 주문 목록 조인
  public List<OrderSummaryVO> list_by_member_summary(int memberno);
  
  public List<OrderWithItemsVO> list_with_items_by_member(int memberno);  // 💡 주문 + 상품목록 조회

  /** 관리자: 전체 주문 목록 조회 */
  public List<OrderVO> list_all();

  /** 공급자: 내 상품이 포함된 주문 목록 조회 */
  public List<OrderVO> list_by_supplier(int memberno);
  
  public List<OrderWithItemsVO> list_with_items_by_supplier(int memberno);

  

}