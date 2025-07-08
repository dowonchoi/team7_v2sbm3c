package dev.mvc.notice;

import java.util.List;

public interface NoticeProcInter {

  /**
   * 공지사항 등록
   * @param noticeVO 등록할 공지사항 정보
   * @return 성공한 레코드 수
   */
  public int create(NoticeVO noticeVO);

  /**
   * 공지사항 목록 조회 (최신순)
   * @return 공지사항 목록
   */
  public List<NoticeVO> list();

  /**
   * 공지사항 상세 조회 (PK로 조회)
   * @param notice_id 공지사항 번호
   * @return 해당 공지사항 정보
   */
  public NoticeVO read(int notice_id);

  /**
   * 공지사항 조회수 증가
   * @param notice_id 공지사항 번호
   * @return 성공한 레코드 수
   */
  public int increaseViewCount(int notice_id);

  /**
   * 공지사항 수정
   * @param noticeVO 수정할 공지사항 정보
   * @return 성공한 레코드 수
   */
  public int update(NoticeVO noticeVO);

  /**
   * 공지사항 삭제
   * @param notice_id 공지사항 번호
   * @return 성공한 레코드 수
   */
  public int delete(int notice_id);
}
