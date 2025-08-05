package dev.mvc.calendar;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
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

  // ===================== 의존성 주입 =====================
  /** 캘린더(일정) 관련 비즈니스 로직 처리 객체 */
  @Autowired
  @Qualifier("dev.mvc.calendar.CalendarProc")
  private CalendarProcInter calendarProc;

  /** 회원 관련 비즈니스 로직 처리 객체 (등록자 정보 및 등급 확인) */
  @Autowired
  @Qualifier("dev.mvc.member.MemberProc")
  private MemberProcInter memberProc;

  /** 카테고리 메뉴 처리 객체 (상단 메뉴 출력용) */
  @Autowired
  @Qualifier("dev.mvc.cate.CateProc")
  private CateProcInter cateProc;

  // ===================== 파일 저장 경로 =====================
  /** 캘린더 첨부 파일이 저장될 디렉토리 경로 */
  private final String uploadDir = CalendarPath.getUploadDir();  // 통일된 경로 사용

  // ===================== 일정 전체 목록 (관리자 전용) =====================
  /**
   * 전체 일정 목록 페이지
   * - 관리자 전용 목록 페이지
   * - DB의 전체 일정 데이터를 조회해 화면에 전달
   */
  @GetMapping("/list_all")
  public String list_all(Model model) {
      List<CalendarVO> list = calendarProc.list_all(); // DB에서 전체 일정 조회
      model.addAttribute("list", list);                // 일정 리스트
      model.addAttribute("menu", cateProc.menu());     // 상단 카테고리 메뉴
      return "calendar/list_all";
  }

  // ===================== 캘린더 메인 페이지 =====================
  /**
   * 캘린더 페이지
   * - 연도/월 파라미터가 없으면 현재 날짜 기준으로 설정
   * - JS에서 월은 0부터 시작하므로 month-1 값을 넘김
   */
  @GetMapping("/list_calendar")
  public String list_calendar(Model model,
                              @RequestParam(name = "year", defaultValue = "0") int year,
                              @RequestParam(name = "month", defaultValue = "0") int month) {
      if (year == 0) { // 파라미터 없으면 오늘 날짜로 설정
          LocalDate today = LocalDate.now();
          year = today.getYear();
          month = today.getMonthValue();
      }
      model.addAttribute("year", year);
      model.addAttribute("month", month - 1); // JS 달력용 (0부터 시작)
      return "calendar/list_calendar";
  }

  // ===================== AJAX: 월간 일정 조회 =====================
  /**
   * 특정 월(month)에 해당하는 일정 목록을 JSON으로 반환
   * - FullCalendar 등 JS 플러그인에서 사용 가능
   * - grade: 등록자 등급도 함께 반환
   */
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
          obj.put("grade", memberProc.getGrade(vo.getMemberno())); // 작성자 등급
          array.put(obj);
      }
      return array.toString(); // JSON 문자열 반환
  }

  //===================== 일정 등록 폼 =====================
  /**
  * 일정 등록 폼
  * - 로그인 여부 및 등급 확인
  * - 관리자/공급자 여부를 isAdmin으로 전달
  * - 카테고리 목록 로딩 실패 방지
  */
  @GetMapping("/create")
  public String create(Model model, HttpSession session) {
      Object memberno = session.getAttribute("memberno");
      Object gradeObj = session.getAttribute("grade");

      System.out.println("🟢 등록 폼 접근: memberno=" + memberno + ", grade=" + gradeObj);

      Integer grade = convertGrade(gradeObj);
      boolean isAdmin = grade != null && grade <= 15;

      model.addAttribute("isAdmin", isAdmin);
      model.addAttribute("calendarVO", new CalendarVO());

      List<CateVOMenu> cateList = new ArrayList<>();
      if (isAdmin) {
          cateList = cateProc.menu();
          System.out.println("✅ cateList 개수: " + cateList.size());
      }
      model.addAttribute("cateList", cateList);

      return "calendar/create";
  }
  
  //===================== 일정 등록 처리 =====================
  /**
  * 일정 등록 처리
  * - 로그인한 사용자의 memberno를 VO에 설정
  * - 소비자는 cateno 강제 0으로 설정
  * - 첨부 파일 업로드 후 DB에 저장
  */
  @PostMapping("/create")
  public String createProc(HttpSession session, CalendarVO vo) {
      try {
          Integer memberno = (Integer) session.getAttribute("memberno");
          Integer grade = convertGrade(session.getAttribute("grade"));

          System.out.println("✅ 일정 등록 요청: memberno=" + memberno + ", grade=" + grade);

          if (memberno == null) {
              System.out.println("⚠️ 비로그인 상태. 로그인 페이지로 이동");
              return "redirect:/member/login";
          }

          vo.setMemberno(memberno);
          vo.setVisible("Y");

          if (grade != null && grade >= 16) {
              vo.setCateno(0);
              System.out.println("🔒 소비자 → cateno 0으로 강제 설정");
          }

          System.out.println("📁 파일 업로드 시작");
          uploadFile(vo);  // 파일 업로드
          System.out.println("📁 파일 업로드 완료");

          System.out.println("💾 DB 저장 시작");
          calendarProc.create(vo);  // DB 저장
          System.out.println("💾 DB 저장 완료");

          return "redirect:/calendar/list_calendar";

      } catch (Exception e) {
        e.printStackTrace();
        return "calendar/create";  // 실패 시 다시 등록 폼
    }
  }

  // ===================== 일정 상세 보기 =====================
  /**
   * 일정 상세 페이지
   * - 조회수 증가
   * - 작성자 ID, 등급 명칭 등을 함께 전달
   */
  @GetMapping("/read/{calendarno}")
  public String read(Model model, @PathVariable("calendarno") int calendarno) {
      CalendarVO vo = calendarProc.read(calendarno);
      calendarProc.increaseCnt(calendarno); // 조회수 +1

      MemberVO memberVO = memberProc.read(vo.getMemberno());
      String id = (memberVO != null) ? memberVO.getId() : "알 수 없음";
      String gradeName = gradeName(memberVO != null ? memberVO.getGrade() : null);

      model.addAttribute("calendarVO", vo);
      model.addAttribute("id", id);
      model.addAttribute("gradeName", gradeName);
      model.addAttribute("menu", cateProc.menu());
      return "calendar/read";
  }

  // ===================== 일정 수정 폼 =====================
  /**
   * 일정 수정 폼
   * - 관리자(<=15) 또는 작성자 본인만 접근 가능
   * - 카테고리 리스트, 기존 일정 데이터 전달
   */
  @GetMapping("/update/{calendarno}")
  public String update_form(@PathVariable("calendarno") int calendarno, Model model, HttpSession session) {
     Integer memberno = (Integer) session.getAttribute("memberno");
     Integer grade = convertGrade(session.getAttribute("grade"));
     if (memberno == null || grade == null) return "redirect:/member/login";

     CalendarVO vo = calendarProc.read(calendarno);
     if (vo == null) return "redirect:/calendar/list_calendar";

     if (grade <= 15 || memberno.equals(vo.getMemberno())) {
         model.addAttribute("isAdmin", grade <= 15);
         model.addAttribute("cateList", cateProc.menu());
         model.addAttribute("calendarVO", vo);
         return "calendar/update";
     }
     return "redirect:/calendar/list_calendar";
  }

  // ===================== 일정 수정 처리 =====================
  /**
   * 일정 수정 처리
   * - 파일 업로드 시 기존 파일 삭제 후 교체
   * - 소비자는 카테고리 변경 불가
   * - 관리자(1~4)는 별도 update_admin 로직 호출
   */
  @PostMapping("/update")
  public String update(CalendarVO vo, HttpSession session) {
     Integer memberno = (Integer) session.getAttribute("memberno");
     Integer grade = convertGrade(session.getAttribute("grade"));
     if (memberno == null || grade == null) return "redirect:/member/login";

     vo.setMemberno(memberno);
     CalendarVO origin = calendarProc.read(vo.getCalendarno());
     MultipartFile mf = vo.getFile1MF();

     if (mf != null && !mf.isEmpty()) {
         uploadFile(vo);                      // 새 파일 업로드
         deleteFile(origin.getFile1saved());  // 기존 파일 삭제
     } else {
         // 기존 파일 정보 유지
         vo.setFile1saved(origin.getFile1saved());
         vo.setFile1origin(origin.getFile1origin());
         vo.setFile1size(origin.getFile1size());
     }

     if (grade >= 16) {
         vo.setCateno(origin.getCateno()); // 소비자: 카테고리 변경 불가
     }

     if (grade <= 4) {
         calendarProc.update_admin(vo); // 관리자용 수정 로직
     } else {
         calendarProc.update(vo);
     }

     return "redirect:/calendar/list_calendar";
  }

  // ===================== 일정 삭제 폼 =====================
  /**
   * 일정 삭제 폼
   * - 관리자 또는 작성자 본인만 접근 가능
   */
  @GetMapping("/delete/{calendarno}")
  public String delete_form(@PathVariable("calendarno") int calendarno, Model model, HttpSession session) {
      CalendarVO vo = calendarProc.read(calendarno);
      Integer memberno = (Integer) session.getAttribute("memberno");
      Integer grade = convertGrade(session.getAttribute("grade"));

      // 권한 체크
      if (grade != null && grade > 4 && !memberno.equals(vo.getMemberno())) {
          return "redirect:/calendar/list_calendar";
      }

      model.addAttribute("calendarVO", vo);
      return "calendar/delete";
  }

  // ===================== 일정 삭제 처리 =====================
  /**
   * 일정 삭제 처리
   * - 관리자 또는 작성자 본인만 삭제 가능
   * - 파일 존재 시 서버에서도 삭제
   */
  @PostMapping("/delete")
  public String delete_proc(@RequestParam("calendarno") int calendarno, HttpSession session) {
      CalendarVO vo = calendarProc.read(calendarno);
      Integer memberno = (Integer) session.getAttribute("memberno");
      Integer grade = convertGrade(session.getAttribute("grade"));

      if (grade != null && grade > 4 && !memberno.equals(vo.getMemberno())) {
          return "redirect:/calendar/list_calendar";
      }

      deleteFile(vo.getFile1saved()); // 서버 내 파일 삭제
      calendarProc.delete(calendarno);
      return "redirect:/calendar/list_all";
  }

  private void uploadFile(CalendarVO vo) {
    try {
        MultipartFile mf = vo.getFile1MF();
        if (mf != null && !mf.isEmpty()) {
            String uploadDir = CalendarPath.getUploadDir();
            System.out.println("📁 업로드 시도 파일명: " + mf.getOriginalFilename());

            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String origin = mf.getOriginalFilename(); // 사용자 보기용
            String ext = origin.substring(origin.lastIndexOf(".")); // 확장자 추출
            String saved = UUID.randomUUID().toString() + ext;      // 안전 저장명

            File dest = new File(uploadDir + saved);
            mf.transferTo(dest);

            vo.setFile1origin(origin);
            vo.setFile1saved(saved);
            vo.setFile1size(mf.getSize());

            System.out.println("✅ 저장 성공: " + saved);

        } else {
            System.out.println("📂 업로드할 파일 없음");
        }
    } catch (Exception e) {
        System.out.println("❌ 파일 업로드 실패: " + e.getMessage());
        e.printStackTrace();
    }
  }

  
  // ===================== 파일 삭제 메서드 =====================
  /**
   * 서버에 저장된 파일을 삭제한다.
   *
   * @param filename 삭제할 파일명
   */
  private void deleteFile(String filename) {
    if (filename != null && !filename.isEmpty()) {
        String uploadDir = CalendarPath.getUploadDir();
        File file = new File(uploadDir + filename);
        if (file.exists()) file.delete();
    }
  }

  // ===================== 등급 숫자를 명칭으로 변환 =====================
  /**
   * 회원 등급을 텍스트 명칭으로 변환
   * @return 관리자 / 공급자 / 소비자 / 알 수 없음
   */
  private String gradeName(Integer grade) {
      if (grade == null) return "알 수 없음";
      if (grade <= 4) return "관리자";
      if (grade <= 15) return "공급자";
      return "소비자";
  }

  // ===================== 세션 등급 변환 =====================
  /**
   * 세션에 저장된 grade(등급) 값을 정수형으로 변환
   * - 숫자 또는 문자열("admin", "supplier", "user") 모두 대응
   */
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
