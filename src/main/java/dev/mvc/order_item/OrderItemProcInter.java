package dev.mvc.order_item;

public interface OrderItemProcInter {
  /**
   * 주문 상세 항목 1개 등록
   * @param orderItemVO 주문 상세 정보
   * @return 등록된 행 수 (1: 성공, 0: 실패)
   */
  public int create(OrderItemVO orderItemVO);
}