package dev.mvc.cancel;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class CancelVO {
  
  private int cancel_id;
  
  private int orderno;
  
  private int memberno;
  
  private String pname;
  
  private String mname;
  
  private String type;       // 취소 / 교환 / 반품
  
  private String reason;
  
  private String status;
  
  private String is_read;
  
  private Date created_at;

  // Getter/Setter 생략 가능 (Lombok 사용 시 @Data)
}
