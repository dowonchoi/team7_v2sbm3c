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

  /** 소비자 신청 폼 */
  @GetMapping("/create")
  public String createForm(@RequestParam("orderno") int orderno, HttpSession session, Model model) {
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return "redirect:/member/login";
    }

    model.addAttribute("orderno", orderno);
    return "/cancel/create";
  }

  /** 신청 처리 */
  @PostMapping("/create")
  public String create(CancelVO cancelVO, HttpSession session) {
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return "redirect:/member/login";
    }

    cancelVO.setMemberno(memberno);
    cancelProc.create(cancelVO);

    return "redirect:/cancel/list"; // 소비자 목록으로
  }

  /** 나의 신청 목록 */
  @GetMapping("/list")
  public String list(HttpSession session, Model model) {
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return "redirect:/member/login";
    }

    List<CancelVO> list = cancelProc.listByMember(memberno);
    model.addAttribute("list", list);
    return "/cancel/list";
  }

  /** ✅ 관리자 전체 신청 목록 - 관리자만 접근 가능 */
  @GetMapping("/admin/list")
  public String adminList(HttpSession session, Model model) {
    Integer grade = (Integer) session.getAttribute("grade"); // 🔁 수정
    if (grade == null || grade < 1 || grade > 4) {
      return "redirect:/member/login";
    }

    List<CancelVO> list = cancelProc.list_all();
    model.addAttribute("list", list);
    return "/cancel/admin_list";
  }

  
  /** 관리자 상태 변경 처리 */
  @PostMapping("/admin/update_status")
  public String updateStatus(HttpSession session, 
                             @RequestParam("cancel_id") int cancel_id, 
                             @RequestParam("status") String status) {
      Integer grade = (Integer) session.getAttribute("grade");
      if (grade == null || grade < 1 || grade > 4) {
          return "redirect:/member/login";
      }

      CancelVO vo = new CancelVO();
      vo.setCancel_id(cancel_id);
      vo.setStatus(status);
      cancelProc.updateStatus(vo);

      return "redirect:/cancel/admin/list";
  }

}
