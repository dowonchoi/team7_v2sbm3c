package dev.mvc.notification;

import java.util.List;

public interface NotificationProcInter {

  public int countUnreadNotifications(int memberno);

  public List<NotificationVO> selectNotificationsByMemberId(int memberno);

  public int markNotificationAsRead(int notification_id);

  public int create(NotificationVO vo);  // 알림 등록 메서드
  
  public int insertNotification(NotificationVO notificationVO);
  
  public int delete(int notification_id);
  
  /** 알림 1건 조회 */
  public NotificationVO read(int notification_id);
}
