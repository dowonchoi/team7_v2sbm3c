package dev.mvc.shipments;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class ShipmentsVO {
  
  private int shipmentno;     // 배송 고유 번호 (PK)
  
  private int orderno;        // 주문 번호 (FK)
  
  private String trackingno;  // 운송장 번호
  
  private String del_status;  // 배송 상태 (예: 배송중, 배송완료 등)
  
  private Date sdate;         // 배송 등록일
}