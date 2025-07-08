package dev.mvc.order;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderVO {
  private int orderno;         // 주문 번호 (PK)
  private int memberno;        // 주문자 회원 번호 (FK)
  private int deliveryno;  // 배송지 번호 (FK)


  private String rname;        // 수령자 이름
  private String rtel;         // 수령자 연락처
  private String rzipcode;     // 우편번호
  private String raddress1;    // 기본 주소
  private String raddress2;    // 상세 주소
  private String message;      // 배송 요청사항

  private String payment;      // 결제 수단 (예: 가상결제)
  private int total;           // 총 결제 금액
  private int point;           // 포인트 사용 or 적립
  private String order_state;  // 주문 상태 (예: 결제완료)
  private String rdate;        // 주문 일자
}