package dev.mvc.cancel;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cancel")
public class CancelCont {

  @Autowired
  @Qualifier("dev.mvc.cancel.CancelProc")
  private CancelProcInter cancelProc; 
  // CancelProcInter 인터페이스 주입 (취소/교환/반품 관련 로직 처리 객체)

  /** 
   * 소비자 신청 폼
   * - 특정 주문번호(orderno)에 대해 취소/교환/반품을 신청하기 위한 폼 화면을 출력
   * - 로그인하지 않은 사용자는 로그인 페이지로 리다이렉트
   */
  @GetMapping("/create")
  public String createForm(@RequestParam("orderno") int orderno, HttpSession session, Model model) {
    Integer memberno = (Integer) session.getAttribute("memberno"); // 현재 로그인한 회원번호 조회
    if (memberno == null) {
      return "redirect:/member/login"; // 로그인하지 않은 경우 로그인 페이지로 이동
    }

    model.addAttribute("orderno", orderno); // 선택한 주문번호를 폼에 전달
    return "cancel/create"; // 취소/교환/반품 신청 화면
  }

  /** 
   * 신청 처리
   * - 사용자가 신청 폼을 작성한 뒤 제출하면 DB에 해당 신청 내역을 저장
   * - 로그인하지 않은 사용자는 로그인 페이지로 리다이렉트
   */
  @PostMapping("/create")
  public String create(CancelVO cancelVO, HttpSession session) {
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return "redirect:/member/login";
    }

    cancelVO.setMemberno(memberno); // 현재 로그인한 사용자 번호 설정
    cancelProc.create(cancelVO); // 신청 내역 DB 저장

    return "redirect:/cancel/list"; // 신청 완료 후 소비자 신청 목록 페이지로 이동
  }

  /** 
   * 나의 신청 목록
   * - 현재 로그인한 소비자의 취소/교환/반품 신청 목록을 출력
   * - 로그인하지 않은 사용자는 로그인 페이지로 리다이렉트
   */
  @GetMapping("/list")
  public String list(HttpSession session, Model model) {
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return "redirect:/member/login"; // 로그인 필요
    }

    List<CancelVO> list = cancelProc.listByMember(memberno); // 해당 회원의 신청 목록 조회
    model.addAttribute("list", list); // View에 데이터 전달
    return "cancel/list"; // 소비자용 신청 목록 페이지
  }

  /** 
   * 관리자 전체 신청 목록
   * - 모든 사용자의 취소/교환/반품 신청 내역을 관리자 전용 화면에서 확인 가능
   * - 관리자 등급(1~4)만 접근 허용
   */
  @GetMapping("/admin/list")
  public String adminList(HttpSession session, Model model) {
    Integer grade = (Integer) session.getAttribute("grade"); // 현재 로그인한 사용자 등급
    if (grade == null || grade < 1 || grade > 4) {
      return "redirect:/member/login"; // 관리자 등급이 아니면 접근 불가
    }

    List<CancelVO> list = cancelProc.list_all(); // 모든 신청 내역 조회
    model.addAttribute("list", list);
    return "cancel/admin_list"; // 관리자용 목록 페이지
  }

  /** 
   * 관리자 상태 변경 처리
   * - 관리자(admin)가 특정 취소/교환/반품 신청의 상태를 변경 (예: 승인, 거절 등)
   * - 비관리자는 접근 불가
   */
  @PostMapping("/admin/update_status")
  public String updateStatus(HttpSession session, 
                             @RequestParam("cancel_id") int cancel_id, 
                             @RequestParam("status") String status) {
      Integer grade = (Integer) session.getAttribute("grade");
      if (grade == null || grade < 1 || grade > 4) {
          return "redirect:/member/login"; // 관리자만 접근 가능
      }

      CancelVO vo = new CancelVO();
      vo.setCancel_id(cancel_id); // 상태 변경 대상 신청 ID
      vo.setStatus(status);       // 새로운 상태값 설정
      cancelProc.updateStatus(vo); // DB 업데이트 실행

      return "redirect:/cancel/admin/list"; // 상태 변경 후 관리자 목록 페이지로 이동
  }
  
  /** 
   * 공급자 전용 신청 목록
   * - 특정 공급자(판매자)에게 해당되는 취소/교환/반품 신청 목록 출력
   * - 공급자 등급(5~15)만 접근 가능
   */
  @GetMapping("/supplier/list")
  public String list_by_supplier(HttpSession session, Model model) {
    Integer grade = (Integer) session.getAttribute("grade");
    Integer memberno = (Integer) session.getAttribute("memberno");

    // 로그인 및 공급자 등급 확인
    if (grade == null || memberno == null || grade < 5 || grade > 15) {
      return "redirect:/member/login_cookie_need"; // 공급자만 접근 가능
    }

    List<CancelVO> list = cancelProc.list_by_supplier(memberno); // 해당 공급자의 신청 내역
    model.addAttribute("list", list);

    return "cancel/list_by_supplier"; // 공급자 전용 목록 페이지
  }

}
