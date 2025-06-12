package dev.mvc.calendar;

import java.util.ArrayList;

public interface CalendarProcInter {
  /**
   * 등록, 추상 메소드
   * @param calendarVO
   * @return
   */
  public int create(CalendarVO calendarVO);
  
  /**
   * 전체 목록
   * @return
   */
  public ArrayList<CalendarVO> list_all();  
  
  /**
   * 조회
   * @param calendarno
   * @return
   */
  public CalendarVO read(int calendarno);

  /**
   * 조회수 증가
   * @param contentsVO
   * @return 처리된 레코드 갯수
   */
  public int increaseCnt(int calendarno);
  
  /**
   * 글 수정
   * @param contentsVO
   * @return 처리된 레코드 갯수
   */
  public int update(CalendarVO calendarVO);
  
  /**
   * 삭제
   * @param calendarno
   * @return 삭제된 레코드 갯수
   */
  public int delete(int calendarno);
  
  /**
   * 특정 달의 조회
   * @return
   */
  public ArrayList<CalendarVO> list_calendar(String date);
  
  /**
   * 특정 날짜의 조회
   * @return
   */
  public ArrayList<CalendarVO> list_calendar_day(String date);
  
}





