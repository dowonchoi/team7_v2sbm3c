package dev.mvc.order;

public interface OrderDAOInter {
  /**
   * 주문 생성
   * @param orderVO 주문 정보
   * @return 등록된 행 수 (1: 성공, 0: 실패)
   */
  public int create(OrderVO orderVO);
}