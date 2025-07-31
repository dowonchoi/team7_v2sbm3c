package dev.mvc.inquiry;

import java.util.List;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import dev.mvc.notification.NotificationProcInter;
import dev.mvc.notification.NotificationVO;

@Controller
@RequestMapping("/inquiry")
public class InquiryCont {

  // ========================= 의존성 주입 =========================

  /** 1:1 문의( Inquiry ) 처리를 위한 서비스 객체 */
  @Autowired
  @Qualifier("dev.mvc.inquiry.InquiryProc")
  private InquiryProcInter inquiryProc;
  
  /** 알림(Notification) 처리를 위한 서비스 객체 */
  @Autowired
  @Qualifier("dev.mvc.notification.NotificationProc")
  private NotificationProcInter notificationProc;

  // ========================= 문의 등록 =========================

  /**
   * 문의 작성 폼 페이지로 이동
   * @return templates/inquiry/create.html
   */
  @GetMapping("/create")
  public String createForm() {
    return "/inquiry/create";
  }

  /**
   * 문의글 작성 처리
   * - 로그인한 사용자 정보를 세션에서 가져와 InquiryVO에 세팅
   * - 사용자 유형(user/supplier)에 따라 user_type 설정
   * - 문의글 등록 후 관리자에게 알림(Notification) 생성
   * @param session 로그인 세션 (memberno, id, mname, grade 사용)
   * @param inquiryVO 사용자 입력 문의 데이터
   * @return 작성 완료 후 내 문의 목록으로 리다이렉트
   */
  @PostMapping("/create")
  public String create(HttpSession session, InquiryVO inquiryVO) {
      Integer memberno = (Integer) session.getAttribute("memberno");
      String id = (String) session.getAttribute("id");
      String name = (String) session.getAttribute("mname");
      Integer grade = (Integer) session.getAttribute("grade");

      // 문의 작성자 정보 세팅
      inquiryVO.setMemberno(memberno);
      inquiryVO.setWriter_id(id);
      inquiryVO.setWriter_name(name);

      // 등급에 따라 user_type 구분
      if (grade != null && grade >= 16 && grade <= 39) {
          inquiryVO.setUser_type("user");
      } else if (grade != null && grade >= 5 && grade <= 15) {
          inquiryVO.setUser_type("supplier");
      } else {
          inquiryVO.setUser_type("unknown");
      }

      // DB 등록
      inquiryProc.create(inquiryVO);

      // 알림용 제목 처리 (길면 20자 이후 ... 붙임)
      String title = inquiryVO.getTitle();
      if (title.length() > 20) {
          title = title.substring(0, 20) + "...";
      }

      // 관리자(고정 memberno=1)에게 알림 전송
      NotificationVO notificationVO = new NotificationVO();
      notificationVO.setMemberno(1); // 관리자 memberno
      notificationVO.setType("inquiry");
      notificationVO.setMessage("새로운 1:1 문의 [" + title + "]가 등록되었습니다.");
      notificationVO.setUrl("/inquiry/list_all");
      notificationVO.setIs_read("N");

      notificationProc.create(notificationVO);

      return "redirect:/inquiry/list_by_member";
  }

  // ========================= 문의 목록 =========================

  /**
   * 로그인한 사용자의 문의 목록 출력
   * @param session 현재 로그인 사용자 정보
   * @param model 뷰로 데이터 전달
   * @return templates/inquiry/list.html
   */
  @GetMapping("/list")
  public String list(HttpSession session, Model model) {
    Integer memberno = (Integer) session.getAttribute("memberno");
    List<InquiryVO> list = inquiryProc.listByMember(memberno);
    model.addAttribute("list", list);
    return "/inquiry/list";
  }
  
  /**
   * 관리자 전용 전체 문의 목록 출력
   * - 관리자 등급(1~4)만 접근 가능
   * @param session 로그인 세션
   * @param model 뷰 데이터
   * @return templates/inquiry/list_all.html
   */
  @GetMapping("/list_all")
  public String list_all(HttpSession session, Model model) {
      Integer gradeObj = (Integer) session.getAttribute("grade");
      int grade = (gradeObj != null) ? gradeObj : 99;

      // 비관리자 접근 제한
      if (grade < 1 || grade > 4) {
          return "redirect:/error/permission";
      }

      List<InquiryVO> list = inquiryProc.list_all();
      model.addAttribute("list", list);
      return "/inquiry/list_all";
  }
  
  // ========================= 문의 상세 보기 =========================

  /**
   * 문의글 상세보기
   * @param inquiry_id 조회할 문의글 PK
   * @param model 뷰에 InquiryVO 데이터 전달
   * @return templates/inquiry/read.html
   */
  @GetMapping("/read")
  public String read(@RequestParam("inquiry_id") int inquiry_id, Model model) {
      InquiryVO inquiryVO = inquiryProc.read(inquiry_id); // read 메서드에서 조회수 증가
      model.addAttribute("inquiryVO", inquiryVO);
      return "/inquiry/read";
  }
  
  // ========================= 문의 수정 =========================

  /**
   * 문의 수정 폼 페이지
   * - 작성자 본인만 접근 가능
   * @param inquiry_id 수정할 글의 PK
   * @param model 뷰 데이터
   * @param session 로그인 세션
   * @return templates/inquiry/update.html
   */
  @GetMapping("/update")
  public String updateForm(@RequestParam("inquiry_id") int inquiry_id, Model model, HttpSession session) {
      InquiryVO inquiryVO = inquiryProc.read(inquiry_id);

      // 작성자 본인 여부 확인
      Integer memberno = (Integer) session.getAttribute("memberno");
      if (!memberno.equals(inquiryVO.getMemberno())) {
          return "redirect:/error/permission";
      }

      model.addAttribute("inquiryVO", inquiryVO);
      return "/inquiry/update";
  }
  
  /**
   * 문의 수정 처리
   * - 작성자 본인만 가능
   * @param inquiryVO 수정할 데이터
   * @param session 로그인 세션
   * @return 수정 후 해당 문의글 상세보기로 리다이렉트
   */
  @PostMapping("/update")
  public String update(InquiryVO inquiryVO, HttpSession session) {
      Integer memberno = (Integer) session.getAttribute("memberno");

      // 기존 데이터 조회
      InquiryVO dbVO = inquiryProc.read(inquiryVO.getInquiry_id());
      if (!memberno.equals(dbVO.getMemberno())) {
          return "redirect:/error/permission";
      }

      // 작성자 정보 유지
      inquiryVO.setWriter_id(dbVO.getWriter_id());
      inquiryVO.setWriter_name(dbVO.getWriter_name());
      inquiryVO.setUser_type(dbVO.getUser_type());
      inquiryVO.setMemberno(memberno);

      inquiryProc.update(inquiryVO);
      return "redirect:/inquiry/read?inquiry_id=" + inquiryVO.getInquiry_id();
  }
  
  // ========================= 문의 삭제 =========================

  /**
   * 문의글 삭제 처리
   * - 작성자 본인 또는 관리자만 가능
   * @param inquiry_id 삭제할 글 PK
   * @param session 로그인 세션
   * @return 관리자면 list_all, 작성자면 list_by_member로 리다이렉트
   */
  @GetMapping("/delete")
  public String delete(@RequestParam("inquiry_id") int inquiry_id, HttpSession session) {
      InquiryVO inquiryVO = inquiryProc.read(inquiry_id);
      Integer memberno = (Integer) session.getAttribute("memberno");
      Integer grade = (Integer) session.getAttribute("grade");

      if (!memberno.equals(inquiryVO.getMemberno()) && (grade == null || grade < 1 || grade > 4)) {
          return "redirect:/error/permission";
      }

      inquiryProc.delete(inquiry_id);

      if (grade != null && grade >= 1 && grade <= 4) {
          return "redirect:/inquiry/list_all";
      } else {
          return "redirect:/inquiry/list_by_member";
      }
  }
  
  // ========================= 내 문의 목록 =========================

  /**
   * 로그인한 사용자의 1:1 문의 내역 페이지
   * @param session 로그인 세션
   * @param model 뷰 데이터
   * @return templates/inquiry/list_by_member.html
   */
  @GetMapping("/list_by_member")
  public String listByMember(HttpSession session, Model model) {
      Integer memberno = (Integer) session.getAttribute("memberno");

      if (memberno == null) {
          return "redirect:/member/login";
      }

      List<InquiryVO> list = inquiryProc.listByMember(memberno);
      model.addAttribute("list", list);

      return "/inquiry/list_by_member";
  }
  
  // ========================= 관리자 답변 =========================

  /**
   * 관리자 답변 등록
   * - 관리자만 접근 가능
   * - 답변 등록 후 해당 글 작성자에게 알림(Notification) 전송
   * @param inquiry_id 답변할 문의글 PK
   * @param answer 답변 내용
   * @param session 로그인 세션
   * @return 답변 등록 후 해당 문의 상세 페이지로 리다이렉트
   */
  @PostMapping("/reply")
  public String reply(@RequestParam("inquiry_id") int inquiry_id,
                      @RequestParam("answer") String answer,
                      HttpSession session) {
      Integer grade = (Integer) session.getAttribute("grade"); // ✅ 수정됨
  
      // 관리자 권한 체크
      if (grade == null || grade < 1 || grade > 4) {
          return "redirect:/error/permission";
      }

      // 문의글 조회 후 답변 업데이트
      InquiryVO vo = inquiryProc.read(inquiry_id);
      vo.setAnswer(answer);
      inquiryProc.updateAnswer(vo);

      // 알림 메시지용 제목 줄이기
      String title = vo.getTitle();
      if (title.length() > 20) {
          title = title.substring(0, 20) + "...";
      }

      // 알림 생성 (문의 작성자에게)
      NotificationVO notificationVO = new NotificationVO();
      notificationVO.setMemberno(vo.getMemberno());
      notificationVO.setType("inquiry");
      notificationVO.setMessage("문의하신 글 [" + title + "]에 답변이 등록되었습니다.");
      notificationVO.setUrl("/inquiry/read?inquiry_id=" + inquiry_id);
      notificationVO.setIs_read("N");

      notificationProc.create(notificationVO);

      return "redirect:/inquiry/read?inquiry_id=" + inquiry_id;
  }

  /**
   * 관리자 답변 삭제 (내용 초기화)
   * @param inquiry_id 답변을 삭제할 문의글 PK
   * @param session 로그인 세션
   * @return 해당 문의글 상세 페이지로 리다이렉트
   */
  @PostMapping("/delete_reply")
  public String deleteReply(@RequestParam("inquiry_id") int inquiry_id, HttpSession session) {
      Integer grade = (Integer) session.getAttribute("grade"); // ✅ 수정됨

      if (grade == null || grade < 1 || grade > 4) {
          return "redirect:/error/permission";
      }

      inquiryProc.deleteAnswer(inquiry_id);
      return "redirect:/inquiry/read?inquiry_id=" + inquiry_id;
  }

}
