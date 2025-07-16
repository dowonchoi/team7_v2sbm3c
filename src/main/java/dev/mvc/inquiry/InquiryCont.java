package dev.mvc.inquiry;

import java.util.List;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/inquiry")
public class InquiryCont {

  @Autowired
  @Qualifier("dev.mvc.inquiry.InquiryProc")
  private InquiryProcInter inquiryProc;

  @GetMapping("/create")
  public String createForm() {
    return "/inquiry/create";
  }

  @PostMapping("/create")
  public String create(HttpSession session, InquiryVO inquiryVO) {
      Integer memberno = (Integer) session.getAttribute("memberno");
      String id = (String) session.getAttribute("id");
      String name = (String) session.getAttribute("mname");
      String grade = (String) session.getAttribute("grade");  // ✅ 문자열로 받아옴

      inquiryVO.setMemberno(memberno);
      inquiryVO.setWriter_id(id);
      inquiryVO.setWriter_name(name);

      if ("user".equals(grade)) {
          inquiryVO.setUser_type("user");
      } else if ("supplier".equals(grade)) {
          inquiryVO.setUser_type("supplier");
      } else {
          inquiryVO.setUser_type("unknown");
      }

      inquiryProc.create(inquiryVO);
      return "redirect:/inquiry/list_by_member";
  }


  @GetMapping("/list")
  public String list(HttpSession session, Model model) {
    Integer memberno = (Integer) session.getAttribute("memberno");
    List<InquiryVO> list = inquiryProc.listByMember(memberno);
    model.addAttribute("list", list);
    return "/inquiry/list";
  }
  
  @GetMapping("/list_all")
  public String list_all(HttpSession session, Model model) {
      String grade = (String) session.getAttribute("grade");

      // 관리자만 접근 가능
      if (!"admin".equals(grade)) {
          return "redirect:/error/permission";
      }

      List<InquiryVO> list = inquiryProc.list_all();
      model.addAttribute("list", list);
      return "/inquiry/list_all";
  }
  
  @GetMapping("/read")
  public String read(@RequestParam("inquiry_id") int inquiry_id, Model model) {
      InquiryVO inquiryVO = inquiryProc.read(inquiry_id); // 조회수 증가 포함된 read
      model.addAttribute("inquiryVO", inquiryVO);
      return "/inquiry/read";
  }
  
  @GetMapping("/update")
  public String updateForm(@RequestParam("inquiry_id") int inquiry_id, Model model, HttpSession session) {
      InquiryVO inquiryVO = inquiryProc.read(inquiry_id);

      // 작성자 본인만 수정 가능하게 제어
      Integer memberno = (Integer) session.getAttribute("memberno");
      if (!memberno.equals(inquiryVO.getMemberno())) {
          return "redirect:/error/permission";
      }

      model.addAttribute("inquiryVO", inquiryVO);
      return "/inquiry/update";  // ✅ update.html 있어야 함
  }
  
  @PostMapping("/update")
  public String update(InquiryVO inquiryVO, HttpSession session) {
      Integer memberno = (Integer) session.getAttribute("memberno");

      InquiryVO dbVO = inquiryProc.read(inquiryVO.getInquiry_id());
      if (!memberno.equals(dbVO.getMemberno())) {
          return "redirect:/error/permission";
      }

      inquiryVO.setWriter_id(dbVO.getWriter_id());
      inquiryVO.setWriter_name(dbVO.getWriter_name());
      inquiryVO.setUser_type(dbVO.getUser_type());
      inquiryVO.setMemberno(memberno);

      // update 쿼리 구현 필요 (MyBatis)
      inquiryProc.update(inquiryVO);
      return "redirect:/inquiry/read?inquiry_id=" + inquiryVO.getInquiry_id();
  }
  
  @GetMapping("/delete")
  public String delete(@RequestParam("inquiry_id") int inquiry_id, HttpSession session) {
      InquiryVO inquiryVO = inquiryProc.read(inquiry_id);
      Integer memberno = (Integer) session.getAttribute("memberno");
      String grade = (String) session.getAttribute("grade");

      if (!memberno.equals(inquiryVO.getMemberno()) && !"admin".equals(grade)) {
          return "redirect:/error/permission";
      }

      inquiryProc.delete(inquiry_id);

      // 🔁 관리자면 list_all, 작성자면 list_by_member
      if ("admin".equals(grade)) {
          return "redirect:/inquiry/list_all";
      } else {
          return "redirect:/inquiry/list_by_member";
      }
  }
  
  @GetMapping("/list_by_member")
  public String listByMember(HttpSession session, Model model) {
      Integer memberno = (Integer) session.getAttribute("memberno");

      if (memberno == null) {
          return "redirect:/member/login";
      }

      List<InquiryVO> list = inquiryProc.listByMember(memberno); // ✅ 메서드명 정확히!
      model.addAttribute("list", list);

      return "/inquiry/list_by_member";
  }
  
  @PostMapping("/reply")
  public String reply(HttpSession session, InquiryVO inquiryVO) {
      String grade = (String) session.getAttribute("grade");
      String adminId = (String) session.getAttribute("id");

      if (!"admin".equals(grade)) {
          return "redirect:/error/permission";
      }

      // 관리자 정보 설정
      inquiryVO.setAnswer_admin(adminId);

      // 답변 등록
      inquiryProc.updateAnswer(inquiryVO);

      // ✅ 답변 후 전체 문의 목록으로 이동
      return "redirect:/inquiry/list_all";
  }
  
  @PostMapping("/delete_reply")
  public String deleteReply(@RequestParam("inquiry_id") int inquiry_id, HttpSession session) {
      String grade = (String) session.getAttribute("grade");

      if (!"admin".equals(grade)) {
          return "redirect:/error/permission";
      }

      // 답변 초기화
      inquiryProc.deleteAnswer(inquiry_id);

      return "redirect:/inquiry/read?inquiry_id=" + inquiry_id;
  }

  
}
