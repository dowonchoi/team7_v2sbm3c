package dev.mvc.notification;

import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("dev.mvc.notification.NotificationProc")
public class NotificationProc implements NotificationProcInter {

  @Autowired
  private SqlSession sqlSession;

  private final String NAMESPACE = "dev.mvc.notification.NotificationDAOInter";

  @Override
  public int countUnreadNotifications(int memberno) {
    return sqlSession.selectOne(NAMESPACE + ".countUnreadNotifications", memberno);
  }

  @Override
  public List<NotificationVO> selectNotificationsByMemberId(int memberno) {
    return sqlSession.selectList(NAMESPACE + ".selectNotificationsByMemberId", memberno);
  }

  @Override
  public int markNotificationAsRead(int notification_id) {
    return sqlSession.update(NAMESPACE + ".markNotificationAsRead", notification_id);
  }

  @Override
  public int create(NotificationVO notificationVO) {
    return sqlSession.insert(NAMESPACE + ".insertNotification", notificationVO);
  }

  @Override
  public int insertNotification(NotificationVO notificationVO) {
    return sqlSession.insert(NAMESPACE + ".insertNotification", notificationVO);
  }
  
  @Override
  public int delete(int notification_id) {
    return sqlSession.delete(NAMESPACE + ".delete", notification_id);
  }
}
