package dev.mvc.order_item;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemVO {
  private int order_itemno;    // 주문 상세 번호 (PK)
  private int orderno;         // 주문 번호 (FK)
  private int productsno;      // 상품 번호 (FK)

  private String pname;        // 상품명 (복사 저장)
  private String thumb1;       // 썸네일 이미지
  private int price;           // 정가
  private int dc;              // 할인율
  private int saleprice;       // 할인가
  private int cnt;             // 수량
  private int totalprice;      // 할인가 × 수량
  private int point;           // 개당 포인트
  private int totalpoint;      // 총 적립 포인트
}