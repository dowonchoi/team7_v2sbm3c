package dev.mvc.order;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dev.mvc.member.MemberProcInter;
import dev.mvc.order_item.OrderItemDAOInter;
import dev.mvc.order_item.OrderItemVO;
import jakarta.transaction.Transactional;

@Service("dev.mvc.order.OrderProc")
public class OrderProc implements OrderProcInter {

  @Autowired
  private OrderDAOInter orderDAO;
  
  @Autowired
  @Qualifier("dev.mvc.member.MemberProc")
  private MemberProcInter memberProc;

  @Autowired
  @Qualifier("dev.mvc.order_item.OrderItemDAO")
  private OrderItemDAOInter orderItemDAO; // 🔹 주문 상세 DAO 주입

  /**
   * ✅ 주문 생성 + 상세 저장 + 포인트 적립
   */
  @Override
  @Transactional
  public int create(OrderVO orderVO, List<OrderItemVO> orderItems) {
    System.out.println("[OrderProc] 주문 생성 시작");

    // 1. 주문 생성
    int cnt = orderDAO.create(orderVO);
    int orderno = orderVO.getOrderno(); // MyBatis에서 keyProperty="orderno" 필요
    System.out.println("[OrderProc] 주문번호: " + orderno);

    // 2. 상세 항목 저장 + 포인트 합계 계산
    int totalPoint = 0;
    for (OrderItemVO item : orderItems) {
      item.setOrderno(orderno);
      orderItemDAO.create(item); // 주문 상세 추가
      totalPoint += item.getPoint() * item.getCnt();
    }

    // 3. 포인트 적립
    if (totalPoint > 0) {
      memberProc.addPoint(orderVO.getMemberno(), totalPoint);
      orderVO.setPoint(totalPoint); // ✅ 주문 객체에 적립 포인트 저장
      System.out.println("[OrderProc] 포인트 적립: " + totalPoint);
    }

    return cnt;
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
  
  //OrderProc.java
  @Override
  public int count_by_member_products(int memberno, int productsno) {
   return orderDAO.count_by_member_products(memberno, productsno);
}

  
  
  
  
}