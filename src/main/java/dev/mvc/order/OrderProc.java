package dev.mvc.order;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("dev.mvc.order.OrderProc")
public class OrderProc implements OrderProcInter {

  @Autowired
  private OrderDAOInter orderDAO;

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


  
  
}