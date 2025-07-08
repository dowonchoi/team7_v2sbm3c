package dev.mvc.delivery;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryVO {
  private int deliveryno;    // 기본키
  private int memberno;      // 회원번호 (FK)
  private String rname;      // 수령자 이름
  private String rtel;       // 연락처
  private String rzipcode;   // 우편번호
  private String raddress1;  // 주소1
  private String raddress2;  // 주소2
  private String message;    // 배송 메모
  private Date rdate;        // 등록일
}
