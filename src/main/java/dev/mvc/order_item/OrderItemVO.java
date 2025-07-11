package dev.mvc.order_item;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 주문 상세 항목 VO (order_item 테이블과 매핑)
 */
@Data
@Getter
@Setter
public class OrderItemVO {
  
  private int order_itemno;   // 주문 상세 고유 번호 (PK)
  private int orderno;        // 주문 번호 (orders 테이블 FK)
  private int productsno;     // 상품 번호 (products 테이블 FK)

  private String pname;       // 상품명 (복사해서 저장)
  private String thumb1;      // 썸네일 이미지 파일명

  private int price;          // 상품 정가
  private int dc;             // 할인율 (예: 10 = 10%)
  private int saleprice;      // 할인가 (price - 할인)

  private int cnt;            // 주문 수량
  private int totalprice;     // saleprice * cnt

  private int point;          // 개당 적립 포인트
  private int totalpoint;     // point * cnt
  private int memberno; // 상품 등록 회원번호
}











