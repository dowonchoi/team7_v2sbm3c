package dev.mvc.notice;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service  // 반드시 필요! Service Bean으로 등록
public class NoticeProc implements NoticeProcInter {

  @Autowired
  private NoticeDAOInter noticeDAO;  // MyBatis Mapper 주입

  @Override
  public int create(NoticeVO noticeVO) {
    return this.noticeDAO.create(noticeVO);
  }

  @Override
  public List<NoticeVO> list() {
    return this.noticeDAO.list();
  }

  @Override
  public NoticeVO read(int notice_id) {
    return this.noticeDAO.read(notice_id);
  }

  @Override
  public int increaseViewCount(int notice_id) {
    return this.noticeDAO.increaseViewCount(notice_id);
  }

  @Override
  public int update(NoticeVO noticeVO) {
    return this.noticeDAO.update(noticeVO);
  }

  @Override
  public int delete(int notice_id) {
    return this.noticeDAO.delete(notice_id);
  }

}
