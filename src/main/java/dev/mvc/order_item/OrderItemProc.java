package dev.mvc.order_item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("dev.mvc.order_item.OrderItemProc")
public class OrderItemProc implements OrderItemProcInter {

  @Autowired
  private OrderItemDAOInter orderItemDAO;

  @Override
  public int create(OrderItemVO itemVO) {
    return orderItemDAO.create(itemVO);
  }
}