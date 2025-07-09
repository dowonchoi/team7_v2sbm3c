package dev.mvc.order;

import java.util.List;

public interface OrderDAOInter {
  /**
   * 주문 생성
   * @param orderVO 주문 정보
   * @return 등록된 행 수 (1: 성공, 0: 실패)
   */
  public int create(OrderVO orderVO);
  
  //회원 번호로 주문 목록 조회
   public List<OrderVO> list_by_memberno(int memberno);
}