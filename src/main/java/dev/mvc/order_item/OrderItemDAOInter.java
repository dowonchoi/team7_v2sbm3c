package dev.mvc.order_item;

import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * 주문 상세 항목 DAO 인터페이스
 * order_item.xml의 SQL과 연결됨
 */
public interface OrderItemDAOInter {
  /**
   * 주문 상세 항목 1개 등록
   * @param orderItemVO 주문 상세 정보
   * @return 등록된 행 수 (1: 성공, 0: 실패)
   */
    public int create(OrderItemVO orderItemVO);
    
    public List<OrderItemVO> list_by_orderno(int orderno);  // 주문별 상품 목록
    
    public List<OrderItemVO> list_by_orderno_with_memberno(@Param("orderno") int orderno, @Param("memberno") int memberno);


}