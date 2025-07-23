package dev.mvc.calendar;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import dev.mvc.cate.CateProcInter;
import dev.mvc.cate.CateVOMenu;
import dev.mvc.member.MemberProcInter;
import dev.mvc.member.MemberVO;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/calendar")
public class CalendarCont {

  @Autowired
  @Qualifier("dev.mvc.calendar.CalendarProc")
  private CalendarProcInter calendarProc;

  @Autowired
  @Qualifier("dev.mvc.member.MemberProc")
  private MemberProcInter memberProc;

  @Autowired
  @Qualifier("dev.mvc.cate.CateProc")
  private CateProcInter cateProc;

  private final String uploadDir = "C:/kd/deploy/resort/calendar/storage/";

  // ✅ 전체 목록
  @GetMapping("/list_all")
  public String list_all(Model model) {
      List<CalendarVO> list = calendarProc.list_all();
      model.addAttribute("list", list);
      model.addAttribute("menu", cateProc.menu());
      return "calendar/list_all";
  }

  // ✅ 캘린더 화면
  @GetMapping("/list_calendar")
  public String list_calendar(Model model,
                              @RequestParam(name = "year", defaultValue = "0") int year,
                              @RequestParam(name = "month", defaultValue = "0") int month) {
      if (year == 0) {
          LocalDate today = LocalDate.now();
          year = today.getYear();
          month = today.getMonthValue();
      }
      model.addAttribute("year", year);
      model.addAttribute("month", month - 1);
      return "calendar/list_calendar";
  }

  // ✅ 캘린더 월간 데이터
  @GetMapping(value = "/list_calendar_range", produces = "application/json; charset=UTF-8")
  @ResponseBody
  public String list_calendar_range(@RequestParam("month") String month) {
      List<CalendarVO> list = calendarProc.list_calendar_range(month);

      JSONArray array = new JSONArray();
      for (CalendarVO vo : list) {
          JSONObject obj = new JSONObject();
          obj.put("calendarno", vo.getCalendarno());
          obj.put("title", vo.getTitle());
          obj.put("label", vo.getLabel());
          obj.put("startdate", vo.getStartdate());
          obj.put("enddate", vo.getEnddate());
          obj.put("grade", memberProc.getGrade(vo.getMemberno()));
          array.put(obj);
      }
      return array.toString();
  }

 // 등록 폼
  @GetMapping("/create")
  public String create(Model model, HttpSession session) {
      Integer grade = convertGrade(session.getAttribute("grade"));

      if (grade != null && grade <= 15) {
          model.addAttribute("isAdmin", true);  // 관리자 + 공급자
      } else {
          model.addAttribute("isAdmin", false); // 소비자
      }

      model.addAttribute("calendarVO", new CalendarVO());  // ✅ 추가
      model.addAttribute("cateList", cateProc.menu());     // ✅ 이름도 cateList로 맞춰주는 걸 권장
      return "calendar/create";
  }

  //등록 처리
  @PostMapping("/create")
  public String createProc(HttpSession session, CalendarVO vo) throws Exception {
      Integer memberno = (Integer) session.getAttribute("memberno");
      Integer grade = convertGrade(session.getAttribute("grade"));

      if (memberno == null) return "redirect:/member/login";

      vo.setMemberno(memberno);
      vo.setVisible((grade != null && grade <= 4) ? "Y" : "Y");

      if (grade >= 16) {  // 소비자면 카테고리 자동 설정
          vo.setCateno(0);  // 또는 필요시 디폴트 값
      }

      uploadFile(vo);
      calendarProc.create(vo);

      return "redirect:/calendar/list_calendar";
  }

  // ✅ 상세 보기
  @GetMapping("/read/{calendarno}")
  public String read(Model model, @PathVariable("calendarno") int calendarno) {
      CalendarVO vo = calendarProc.read(calendarno);
      calendarProc.increaseCnt(calendarno);

      MemberVO memberVO = memberProc.read(vo.getMemberno());
      String id = (memberVO != null) ? memberVO.getId() : "알 수 없음";
      String gradeName = gradeName(memberVO != null ? memberVO.getGrade() : null);

      model.addAttribute("calendarVO", vo);
      model.addAttribute("id", id);
      model.addAttribute("gradeName", gradeName);
      model.addAttribute("menu", cateProc.menu());

      return "calendar/read";
  }

  //수정 폼
  @GetMapping("/update/{calendarno}")
  public String update_form(@PathVariable("calendarno") int calendarno, Model model, HttpSession session) {
     Integer memberno = (Integer) session.getAttribute("memberno");
     Integer grade = convertGrade(session.getAttribute("grade"));
  
     if (memberno == null || grade == null) {
         return "redirect:/member/login";
     }
  
     CalendarVO vo = calendarProc.read(calendarno);
     if (vo == null) {
         return "redirect:/calendar/list_calendar";
     }
  
     if (grade <= 15) {
         model.addAttribute("isAdmin", true); // 관리자 + 공급자
     } else if (!memberno.equals(vo.getMemberno())) {
         return "redirect:/calendar/list_calendar";
     } else {
         model.addAttribute("isAdmin", false); // 소비자
     }
  
     model.addAttribute("cateList", cateProc.menu());
     model.addAttribute("calendarVO", vo);
  
     return "calendar/update";
  }
  
  //수정 처리
  @PostMapping("/update")
  public String update(CalendarVO vo, HttpSession session) {
     Integer memberno = (Integer) session.getAttribute("memberno");
     Integer grade = convertGrade(session.getAttribute("grade"));
  
     if (memberno == null || grade == null) return "redirect:/member/login";
  
     vo.setMemberno(memberno);
  
     CalendarVO origin = calendarProc.read(vo.getCalendarno());
  
     MultipartFile mf = vo.getFile1MF();
  
     if (mf != null && !mf.isEmpty()) {
         uploadFile(vo);
         deleteFile(origin.getFile1saved());
     } else {
         vo.setFile1saved(origin.getFile1saved());
         vo.setFile1origin(origin.getFile1origin());
         vo.setFile1size(origin.getFile1size());
     }
  
     // ✅ 소비자는 카테고리 수정 불가 → 기존 값 유지
     if (grade >= 16) {
         vo.setCateno(origin.getCateno());
     }
  
     if (grade <= 4) {
         calendarProc.update_admin(vo);
     } else {
         calendarProc.update(vo);
     }
  
     return "redirect:/calendar/list_calendar";
  }
    
  // ✅ 삭제 폼
  @GetMapping("/delete/{calendarno}")
  public String delete_form(@PathVariable("calendarno") int calendarno, Model model, HttpSession session) {
      CalendarVO vo = calendarProc.read(calendarno);
      Integer memberno = (Integer) session.getAttribute("memberno");
      Integer grade = convertGrade(session.getAttribute("grade"));

      if (grade != null && grade > 4 && !memberno.equals(vo.getMemberno())) {
          return "redirect:/calendar/list_calendar";
      }

      model.addAttribute("calendarVO", vo);
      return "calendar/delete";
  }

  // ✅ 삭제 처리
  @PostMapping("/delete")
  public String delete_proc(@RequestParam("calendarno") int calendarno, HttpSession session) {
      CalendarVO vo = calendarProc.read(calendarno);
      Integer memberno = (Integer) session.getAttribute("memberno");
      Integer grade = convertGrade(session.getAttribute("grade"));

      if (grade != null && grade > 4 && !memberno.equals(vo.getMemberno())) {
          return "redirect:/calendar/list_calendar";
      }

      deleteFile(vo.getFile1saved());
      calendarProc.delete(calendarno);
      return "redirect:/calendar/list_all";
  }

  // ✅ 파일 업로드 처리
  private void uploadFile(CalendarVO vo) {
      try {
          MultipartFile mf = vo.getFile1MF();
          if (mf != null && !mf.isEmpty()) {
              File dir = new File(uploadDir);
              if (!dir.exists()) dir.mkdirs();

              String origin = mf.getOriginalFilename();
              String saved = UUID.randomUUID().toString() + "_" + origin;
              mf.transferTo(new File(uploadDir + saved));

              vo.setFile1origin(origin);
              vo.setFile1saved(saved);
              vo.setFile1size(mf.getSize());
          }
      } catch (Exception e) {
          e.printStackTrace();
      }
  }

  // ✅ 파일 삭제 처리
  private void deleteFile(String filename) {
      if (filename != null && !filename.isEmpty()) {
          File file = new File(uploadDir + filename);
          if (file.exists()) file.delete();
      }
  }

  // ✅ Grade → 등급명 변환
  private String gradeName(Integer grade) {
      if (grade == null) return "알 수 없음";
      if (grade <= 4) return "관리자";
      if (grade <= 15) return "공급자";
      return "소비자";
  }

  // ✅ grade 세션 처리 함수 (문자형 → 숫자형 변환 포함)
  private Integer convertGrade(Object gradeObj) {
      if (gradeObj instanceof Integer) return (Integer) gradeObj;
      if (gradeObj instanceof String) {
          switch (((String) gradeObj)) {
              case "admin": return 1;
              case "supplier": return 5;
              case "user": return 16;
              default:
                  try {
                      return Integer.parseInt((String) gradeObj);
                  } catch (NumberFormatException e) {
                      return null;
                  }
          }
      }
      return null;
  }
}
