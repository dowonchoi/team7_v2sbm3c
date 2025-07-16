package dev.mvc.notification;

import java.util.List;

public interface NotificationDAOInter {
  
  public int countUnreadNotifications(int memberno);
  
  public List<NotificationVO> selectNotificationsByMemberId(int memberno);
  
  public int markNotificationAsRead(int notification_id);
  
  public int insertNotification(NotificationVO notificationVO);
 
  public int delete(int notification_id);

}
