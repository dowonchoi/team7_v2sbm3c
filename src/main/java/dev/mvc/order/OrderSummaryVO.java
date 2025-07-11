package dev.mvc.order;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSummaryVO {
  // 주문 정보
  private int orderno;
  private int memberno;
  private int total;
  private String payment;
  private Date rdate;
  private String order_state;

  // 대표 상품 정보
  private String title;     // 상품명
  private String thumb1;    // 썸네일 이미지
  private int cnt;          // 수량
  private int saleprice;    // 할인가
  private String status;
}
