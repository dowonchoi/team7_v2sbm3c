package dev.mvc.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("dev.mvc.order.OrderProc")
public class OrderProc implements OrderProcInter {

  @Autowired
  private OrderDAOInter orderDAO;

  @Override
  public int create(OrderVO orderVO) {
    return orderDAO.create(orderVO);
  }
}