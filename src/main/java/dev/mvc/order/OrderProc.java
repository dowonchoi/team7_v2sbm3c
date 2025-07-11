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
  @Override
  public List<OrderSummaryVO> list_by_member_summary(int memberno) {
    return this.orderDAO.list_by_member_summary(memberno);
  }

  @Override
  public List<OrderWithItemsVO> list_with_items_by_member(int memberno) {
    List<OrderVO> orders = orderDAO.list_by_memberno(memberno);
    List<OrderWithItemsVO> result = new ArrayList<>();

    for (OrderVO o : orders) {
      List<OrderItemVO> items = orderItemDAO.list_by_orderno(o.getOrderno());

      if (!items.isEmpty()) {  // ✅ 상품이 있을 경우에만 추가
        OrderWithItemsVO vo = new OrderWithItemsVO();

        vo.setOrderno(o.getOrderno());
        vo.setMemberno(o.getMemberno());
        vo.setDeliveryno(o.getDeliveryno());
        vo.setRname(o.getRname());
        vo.setRtel(o.getRtel());
        vo.setRzipcode(o.getRzipcode());
        vo.setRaddress1(o.getRaddress1());
        vo.setRaddress2(o.getRaddress2());
        vo.setMessage(o.getMessage());
        vo.setPayment(o.getPayment());
        vo.setTotal(o.getTotal());
        vo.setPoint(o.getPoint());
        vo.setOrder_state(o.getOrder_state());
        vo.setStatus(o.getStatus());
        vo.setRdate(o.getRdate());

        vo.setItems(items); // 상품 목록 설정
        result.add(vo);
      }
    }

    return result;
  }



  
  
}