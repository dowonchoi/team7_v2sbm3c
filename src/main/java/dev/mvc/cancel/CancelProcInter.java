package dev.mvc.cancel;

import java.util.List;

public interface CancelProcInter {
  
  public int create(CancelVO cancelVO);
  
  public int countByMember(int memberno);
  
  public int countUnread();
  
  public List<CancelVO> listByMember(int memberno);
    
  public CancelVO read(int cancel_id);
  
  public int markAsRead(int cancel_id);
  
  public int updateStatus(CancelVO cancelVO);
  
  public List<CancelVO> recentByMember(int memberno); // 인터페이스
  
  public List<CancelVO> list_all();
}
