package dev.mvc.faq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@RequestMapping("/faq")
@Controller
public class FaqCont {

  @Autowired
  @Qualifier("dev.mvc.faq.FaqProc")
  private FaqProcInter faqProc;

  // ✅ 등록 폼 (공지사항처럼 문자열 'admin' 체크)
  @GetMapping("/create")
  public String createForm(HttpSession session) {
      String grade = (String) session.getAttribute("grade");
      if (grade == null || !grade.equals("admin")) {
          return "redirect:/notice/list";
      }
      return "/faq/create";
  }

  // ✅ 등록 처리
  @PostMapping("/create")
  public String create(FaqVO faqVO, HttpSession session) {
      String grade = (String) session.getAttribute("grade");
      if (grade == null || !grade.equals("admin")) {
          return "redirect:/";
      }
      faqVO.setWriter_id((String) session.getAttribute("id"));
      faqProc.create(faqVO);
      return "redirect:/notice/list";
  }

  // ✅ 수정 폼
  @GetMapping("/update")
  public String updateForm(@RequestParam("faq_id") int faq_id, Model model, HttpSession session) {
      String grade = (String) session.getAttribute("grade");
      if (grade == null || !grade.equals("admin")) {
          return "redirect:/";
      }
      FaqVO faqVO = faqProc.read(faq_id);
      model.addAttribute("faqVO", faqVO);
      return "/faq/update";
  }

  // ✅ 수정 처리
  @PostMapping("/update")
  public String update(FaqVO faqVO, HttpSession session) {
      String grade = (String) session.getAttribute("grade");
      if (grade == null || !grade.equals("admin")) {
          return "redirect:/";
      }
      faqVO.setWriter_id((String) session.getAttribute("id"));
      faqProc.update(faqVO);
      return "redirect:/notice/list";
  }

  // ✅ 삭제
  @GetMapping("/delete")
  public String delete(@RequestParam("faq_id") int faq_id, HttpSession session) {
      String grade = (String) session.getAttribute("grade");
      if (grade == null || !grade.equals("admin")) {
          return "redirect:/";
      }
      faqProc.delete(faq_id);
      return "redirect:/notice/list";
  }
}
