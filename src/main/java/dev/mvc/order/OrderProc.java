package dev.mvc.order;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dev.mvc.order_item.OrderItemDAOInter;
import dev.mvc.order_item.OrderItemVO;

@Service("dev.mvc.order.OrderProc")
public class OrderProc implements OrderProcInter {

  @Autowired
  private OrderDAOInter orderDAO;
  
  @Autowired
  @Qualifier("dev.mvc.order_item.OrderItemDAO")
  private OrderItemDAOInter orderItemDAO; // 🔹 주문 상세 DAO 주입

  /**
   * 주문 등록
   * @param orderVO 주문 정보
   * @return 등록 결과 (1: 성공)
   */
  @Override
  public int create(OrderVO orderVO) {
    System.out.println("[OrderProc] create() 실행됨 - 수령자: " + orderVO.getRname());

    int result = orderDAO.create(orderVO);  // result 변수 선언 + 결과 저장
    System.out.println("[OrderProc] orderDAO.create() 결과: " + result);

    return result;  // 마지막에 리턴
  }
  
  @Override
  public List<OrderVO> list_by_memberno(int memberno) {
    return orderDAO.list_by_memberno(memberno);
  }
  
  // 주문 번호로 주문 정보 조회
  @Override
  public OrderVO read(int orderno) {
    return orderDAO.read(orderno);
  }
  
  //내 주문 목록 조인
  /** 회원번호로 주문 + 간략 정보 조인 목록 */
  @Override
  public List<OrderSummaryVO> list_by_member_summary(int memberno) {
    return this.orderDAO.list_by_member_summary(memberno);
  }

  /** 회원별 주문 목록 + 상세 항목을 OrderWithItemsVO로 반환 (템플릿 join용) */
  public List<OrderWithItemsVO> list_with_items_by_supplier(int memberno) {
    List<OrderVO> orders = orderDAO.list_by_supplier(memberno); // 공급자용 주문 목록
    List<OrderWithItemsVO> result = new ArrayList<>();

    for (OrderVO order : orders) {
      // 🔽 공급자(memberno)의 상품만 포함
      List<OrderItemVO> items = orderItemDAO.list_by_orderno_with_memberno(order.getOrderno(), memberno);

      if (!items.isEmpty()) {
        OrderWithItemsVO vo = new OrderWithItemsVO();
        // order의 기본 정보 설정
        vo.setOrderno(order.getOrderno());
        vo.setMemberno(order.getMemberno());
        vo.setDeliveryno(order.getDeliveryno());
        vo.setRname(order.getRname());
        vo.setRtel(order.getRtel());
        vo.setRzipcode(order.getRzipcode());
        vo.setRaddress1(order.getRaddress1());
        vo.setRaddress2(order.getRaddress2());
        vo.setMessage(order.getMessage());
        vo.setPayment(order.getPayment());
        vo.setTotal(order.getTotal());
        vo.setPoint(order.getPoint());
        vo.setOrder_state(order.getOrder_state());
        vo.setStatus(order.getStatus());
        vo.setRdate(order.getRdate());

        vo.setItems(items); // 🔹공급자 상품만
        result.add(vo);
      }
    }

    return result;
  }



  /** 관리자용 전체 주문 목록 (items 포함) */
  @Override
  public List<OrderVO> list_all() {
    List<OrderVO> orderList = orderDAO.list_all();

    for (OrderVO order : orderList) {
      List<OrderItemVO> items = orderItemDAO.list_by_orderno(order.getOrderno());
      order.setItems(items); // 🔹 주문에 상품 목록을 설정
    }

    return orderList;
  }

  /** 공급자(memberno)의 상품이 포함된 주문 목록 */
  @Override
  public List<OrderVO> list_by_supplier(int memberno) {
    return orderDAO.list_by_supplier(memberno);
  }
  

  /**
   * 공급자(memberno)의 상품이 포함된 주문 목록 + 자신의 상품만 상세 포함
   * @param memberno 공급자 회원번호
   * @return 주문 + 자신의 상품 목록만 포함된 OrderWithItemsVO 리스트
   */
  @Override
  public List<OrderWithItemsVO> list_with_items_by_member(int memberno) {
    // ✅ 주문자(memberno)의 주문 목록 가져오기
    List<OrderVO> orders = orderDAO.list_by_memberno(memberno);

    List<OrderWithItemsVO> result = new ArrayList<>();

    for (OrderVO order : orders) {
      // ✅ 해당 주문의 전체 항목 가져오기
      List<OrderItemVO> items = orderItemDAO.list_by_orderno(order.getOrderno());

      if (!items.isEmpty()) {
        OrderWithItemsVO vo = new OrderWithItemsVO();

        vo.setOrderno(order.getOrderno());
        vo.setMemberno(order.getMemberno());
        vo.setDeliveryno(order.getDeliveryno());
        vo.setRname(order.getRname());
        vo.setRtel(order.getRtel());
        vo.setRzipcode(order.getRzipcode());
        vo.setRaddress1(order.getRaddress1());
        vo.setRaddress2(order.getRaddress2());
        vo.setMessage(order.getMessage());
        vo.setPayment(order.getPayment());
        vo.setTotal(order.getTotal());
        vo.setPoint(order.getPoint());
        vo.setOrder_state(order.getOrder_state());
        vo.setStatus(order.getStatus());
        vo.setRdate(order.getRdate());

        vo.setItems(items); // 🔹 주문한 상품들 전체

        result.add(vo);
      }
    }

    return result;
  }


  @Override
  public List<OrderVO> getRecentOrders(int memberno) {
    return orderDAO.list_by_memberno(memberno);
  }

  @Override
  public int countOrders(int memberno) {
    return orderDAO.countOrders(memberno);
  }

  @Override
  public int countCancelledOrders(int memberno) {
      return 0; // 임시
  }
  
  
  
  
}