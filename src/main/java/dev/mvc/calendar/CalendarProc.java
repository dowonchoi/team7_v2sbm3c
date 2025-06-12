package dev.mvc.calendar;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("dev.mvc.calendar.CalendarProc")
public class CalendarProc implements CalendarProcInter{
  @Autowired // ContentsDAOInter interface를 구현한 클래스의 객체를 만들어 자동으로 할당해라.
  private CalendarDAOInter calendarDAO;
  
  @Override
  public int create(CalendarVO calendarVO) {
    int cnt = this.calendarDAO.create(calendarVO);
    
    return cnt;
  }

  @Override
  public ArrayList<CalendarVO> list_all() {
    ArrayList<CalendarVO> list = this.calendarDAO.list_all();
    
    return list;
  }

  @Override
  public CalendarVO read(int calendarno) {
    CalendarVO calendarVO = this.calendarDAO.read(calendarno);
    return calendarVO;
  }

  @Override
  public int increaseCnt(int calendarno) {
    int cnt = this.calendarDAO.increaseCnt(calendarno);
    return cnt;
  }

  @Override
  public int update(CalendarVO calendarVO) {
    int cnt = this.calendarDAO.update(calendarVO);
    return cnt;
  }

  @Override
  public int delete(int calendarno) {
    int cnt = this.calendarDAO.delete(calendarno);
    return cnt;
  }

  @Override
  public ArrayList<CalendarVO> list_calendar(String date) {
    ArrayList<CalendarVO> list = this.calendarDAO.list_calendar(date);
    return list;
  }

  @Override
  public ArrayList<CalendarVO> list_calendar_day(String date) {
    ArrayList<CalendarVO> list = this.calendarDAO.list_calendar_day(date);
    return list;
  }
  
}



