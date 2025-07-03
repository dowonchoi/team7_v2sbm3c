package dev.mvc.calendar;

import java.util.List;

public interface CalendarDAOInter {
  
  /** ✔ 등록 */
  public int create(CalendarVO calendarVO);
  
  /** ✔ 전체 목록 (관리자 전용) */
  public List<CalendarVO> list_all();

  /** ✔ 관리자 전체 (달력 기준) */
  public List<CalendarVO> list_admin();

  /** ✔ 공급자용 (관리자+공급자 공개 일정 + 본인 일정) */
  public List<CalendarVO> list_supplier(int memberno);

  /** ✔ 소비자용 (관리자+공급자 공개 일정 + 본인 소비자 일정) */
  public List<CalendarVO> list_user(int memberno);

  /** ✔ 특정 달의 목록 (labeldate 기준) */
  public List<CalendarVO> list_calendar_month(String month);

  /** ✔ 달력 범위 조회 (startdate ~ enddate) */
  public List<CalendarVO> list_calendar_range(String month);

  /** ✔ 특정 날짜 정확히 조회 (labeldate 기준) */
  public List<CalendarVO> list_calendar_day(String labeldate);

  /** ✔ 특정 달에 포함된 일정 조회 (기간 기준) */
  public List<CalendarVO> list_calendar_day_range(String month);

  /** ✔ 카테고리별 월간 일정 조회 */
  public List<CalendarVO> list_calendar_range_by_cate(String month, int cateno);

  /** ✔ 상세 조회 */
  public CalendarVO read(int calendarno);

  /** ✔ 조회수 증가 */
  public int increaseCnt(int calendarno);
  
  /** ✔ 수정 */
  public int update(CalendarVO calendarVO);
  
  /** 🔥 수정 (관리자용 - 공개여부 포함) */
  public int update_admin(CalendarVO calendarVO);
  
  /** ✔ 삭제 */
  public int delete(int calendarno);
  
  /** 공개 여부 수정 (관리자 전용) */
  public int updateVisible(int calendarno, String visible);
  
  /** 수정/삭제 로그 기록 */
  public int insertLog(CalendarLogVO logVO);

  /** 로그 목록 조회 */
  public List<CalendarLogVO> listLog();


}
