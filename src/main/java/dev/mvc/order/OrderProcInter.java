package dev.mvc.order;

public interface OrderProcInter {
  /**
   * 주문 생성
   * @param orderVO 주문 정보
   * @return 등록된 행 수
   */
  public int create(OrderVO orderVO);
}