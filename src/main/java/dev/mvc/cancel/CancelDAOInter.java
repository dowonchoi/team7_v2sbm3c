package dev.mvc.cancel;

import java.util.List;

public interface CancelDAOInter {
  
  public int create(CancelVO cancelVO);
  
  public int countByMember(int memberno);          // 회원별 신청 수
  
  public int countUnread();                        // 읽지 않은 신청 수 (관리자용)
  
  public List<CancelVO> listByMember(int memberno);

  public CancelVO read(int cancel_id);
  
  public int markAsRead(int cancel_id);
  
  public int updateStatus(CancelVO cancelVO);
  
  public List<CancelVO> recentByMember(int memberno);
  
  public List<CancelVO> list_all();
}
