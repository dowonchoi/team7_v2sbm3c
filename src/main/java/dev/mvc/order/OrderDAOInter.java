package dev.mvc.order;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface OrderDAOInter {
  /**
   * 주문 생성
   * @param orderVO 주문 정보
   * @return 등록된 행 수 (1: 성공, 0: 실패)
   */
  public int create(OrderVO orderVO);
  
  //회원 번호로 주문 목록 조회
   public List<OrderVO> list_by_memberno(int memberno);
   
   // 주문번호로 주문 정보 조회
   public OrderVO read(int orderno);  // ✅ DAO에서 실제 SELECT 수행
   
   // 내 주문 목록 조인
   public List<OrderSummaryVO> list_by_member_summary(int memberno);

   /** 관리자: 전체 주문 목록 조회 */
   public List<OrderVO> list_all();

   /** 공급자: 내 상품이 포함된 주문 목록 조회 */
   public List<OrderVO> list_by_supplier(int memberno);

   public int countOrders(int memberno);
   
   /**
    * 특정 회원이 특정 상품을 주문한 적 있는지 확인
    * @param memberno
    * @param productsno
    * @return 1 이상이면 주문함
    */
   public int count_by_member_products(@Param("memberno") int memberno, @Param("productsno") int productsno);

   
}