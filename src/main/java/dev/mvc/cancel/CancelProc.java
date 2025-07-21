package dev.mvc.cancel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dev.mvc.cancel.CancelProc")
public class CancelProc implements CancelProcInter {

  @Autowired
  private CancelDAOInter cancelDAO;

  @Override
  public int create(CancelVO cancelVO) {
    return cancelDAO.create(cancelVO);
  }

  @Override
  public int countByMember(int memberno) {
    return cancelDAO.countByMember(memberno);
  }

  @Override
  public int countUnread() {
    return cancelDAO.countUnread();
  }

  @Override
  public List<CancelVO> listByMember(int memberno) {
    return cancelDAO.listByMember(memberno);
  }

  @Override
  public CancelVO read(int cancel_id) {
    return cancelDAO.read(cancel_id);
  }

  @Override
  public int markAsRead(int cancel_id) {
    return cancelDAO.markAsRead(cancel_id);
  }

  @Override
  public int updateStatus(CancelVO cancelVO) {
    return cancelDAO.updateStatus(cancelVO);
  }
  
  @Override
  public List<CancelVO> recentByMember(int memberno) {
    return cancelDAO.recentByMember(memberno);
  }
  
  @Override
  public List<CancelVO> list_all() {
    return this.cancelDAO.list_all();
  }

}
