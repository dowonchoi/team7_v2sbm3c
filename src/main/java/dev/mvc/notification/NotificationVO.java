package dev.mvc.notification;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class NotificationVO {
  
  private int notification_id;  // 알림 번호
  
  private int memberno;         // 회원 번호
  
  private String type;          // 알림 종류 ('order', 'qna' 등)
  
  private String message;       // 알림 내용
  
  private String url;           // 이동 URL
  
  private String is_read;       // 읽음 여부 ('Y', 'N')
  
  private Date created_at;      // 생성일시
}