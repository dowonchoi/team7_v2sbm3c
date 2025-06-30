package dev.mvc.calendar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("dev.mvc.calendar.CalendarProc")
public class CalendarProc implements CalendarProcInter {

    @Autowired
    private CalendarDAOInter calendarDAO;
    
    @Autowired
    private SqlSession sqlSession;

    @Override
    public int create(CalendarVO calendarVO) {
        return calendarDAO.create(calendarVO);
    }

    @Override
    public List<CalendarVO> list_all() {
        return calendarDAO.list_all();
    }

    @Override
    public List<CalendarVO> list_admin() {
        return calendarDAO.list_admin();
    }

    @Override
    public List<CalendarVO> list_supplier(int memberno) {
        return calendarDAO.list_supplier(memberno);
    }

    @Override
    public List<CalendarVO> list_user(int memberno) {
        return calendarDAO.list_user(memberno);
    }

    @Override
    public List<CalendarVO> list_calendar_month(String month) {
        return calendarDAO.list_calendar_month(month);
    }

    @Override
    public List<CalendarVO> list_calendar_range(String month) {
        return calendarDAO.list_calendar_range(month);
    }

    @Override
    public List<CalendarVO> list_calendar_day(String labeldate) {
        return calendarDAO.list_calendar_day(labeldate);
    }

    @Override
    public List<CalendarVO> list_calendar_day_range(String month) {
        return calendarDAO.list_calendar_day_range(month);
    }

    @Override
    public List<CalendarVO> list_calendar_range_by_cate(String month, int cateno) {
        Map<String, Object> params = new HashMap<>();
        params.put("month", month);
        params.put("cateno", cateno);
        return sqlSession.selectList("dev.mvc.calendar.CalendarDAOInter.list_calendar_range_by_cate", params);
    }

    @Override
    public CalendarVO read(int calendarno) {
        return calendarDAO.read(calendarno);
    }

    @Override
    public int increaseCnt(int calendarno) {
        return calendarDAO.increaseCnt(calendarno);
    }

    @Override
    public int update(CalendarVO calendarVO) {
        return calendarDAO.update(calendarVO);
    }

    @Override
    public int delete(int calendarno) {
        return calendarDAO.delete(calendarno);
    }

    @Override
    public int updateVisible(int calendarno, String visible) {
        Map<String, Object> map = new HashMap<>();
        map.put("calendarno", calendarno);
        map.put("visible", visible);
        return sqlSession.update("dev.mvc.calendar.CalendarDAOInter.updateVisible", map);
    }

    @Override
    public int insertLog(CalendarLogVO logVO) {
        return sqlSession.insert("dev.mvc.calendar.CalendarDAOInter.insertLog", logVO);
    }

    @Override
    public List<CalendarLogVO> listLog() {
        return sqlSession.selectList("dev.mvc.calendar.CalendarDAOInter.listLog");
    }
}
