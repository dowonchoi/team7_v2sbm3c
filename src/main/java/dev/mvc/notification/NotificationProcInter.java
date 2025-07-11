package dev.mvc.notification;

import java.util.List;

public interface NotificationProcInter {

  public int countUnreadNotifications(int memberno);

  public List<NotificationVO> selectNotificationsByMemberId(int memberno);

  public int markNotificationAsRead(int notification_id);

  public int create(NotificationVO vo);  // 알림 등록 메서드
}
