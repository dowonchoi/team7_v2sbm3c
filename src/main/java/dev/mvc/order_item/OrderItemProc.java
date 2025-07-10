package dev.mvc.order_item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 주문 상세 항목 처리 클래스
 * 주문 항목 등록 처리
 */
@Service("dev.mvc.order_item.OrderItemProc")
public class OrderItemProc implements OrderItemProcInter {

  @Autowired
  @Qualifier("dev.mvc.order_item.OrderItemDAO")  // 정확한 이름으로 명시
  private OrderItemDAOInter orderItemDAO;

  /**
   * 주문 상세 항목 1개 등록
   * @param orderItemVO 주문 항목 데이터
   * @return 등록된 행 수 (1: 성공)
   */
  @Override
  public int create(OrderItemVO orderItemVO) {
    return orderItemDAO.create(orderItemVO);  // 실제 SQL 실행
  }
}