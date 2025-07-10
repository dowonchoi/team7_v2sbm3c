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


}