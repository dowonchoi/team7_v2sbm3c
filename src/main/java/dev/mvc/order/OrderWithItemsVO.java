package dev.mvc.order;

import java.util.Date;
import java.util.List;

import dev.mvc.order_item.OrderItemVO;
import lombok.Getter;
import lombok.Setter;

/**
 * 주문 1건 + 해당 주문의 상품 목록
 */
@Getter
@Setter
public class OrderWithItemsVO {
  // 주문 정보
  private int orderno;
  private int memberno;
  private int deliveryno;

  private String rname;
  private String rtel;
  private String rzipcode;
  private String raddress1;
  private String raddress2;
  private String message;

  private String payment;
  private int total;
  private int point;
  private String order_state;
  private String status;
  private Date rdate;

  // ✅ 해당 주문의 상품 목록
  private List<OrderItemVO> items;
}
