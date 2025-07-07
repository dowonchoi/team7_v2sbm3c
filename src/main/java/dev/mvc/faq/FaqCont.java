package dev.mvc.faq;

import java.util.List;

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

    // ✅ 등록 폼
    @GetMapping("/create")
    public String createForm() {
        return "/faq/create";
    }

    // ✅ 등록 처리
    @PostMapping("/create")
    public String create(FaqVO faqVO, HttpSession session) {
        faqVO.setWriter_id((String) session.getAttribute("id"));
        faqProc.create(faqVO);
        return "redirect:/notice/list";  // COMMUNITY 메인으로 리다이렉트
    }

    // ✅ 수정 폼
    @GetMapping("/update")
    public String updateForm(@RequestParam("faq_id") int faq_id, Model model) {
        FaqVO faqVO = faqProc.read(faq_id);
        model.addAttribute("faqVO", faqVO);
        return "/faq/update";
    }

    // ✅ 수정 처리
    @PostMapping("/update")
    public String update(FaqVO faqVO, HttpSession session) {
        faqVO.setWriter_id((String) session.getAttribute("id"));
        faqProc.update(faqVO);
        return "redirect:/notice/list";  // COMMUNITY 메인으로 리다이렉트
    }

    // ✅ 삭제
    @GetMapping("/delete")
    public String delete(@RequestParam("faq_id") int faq_id) {
        faqProc.delete(faq_id);
        return "redirect:/notice/list";  // COMMUNITY 메인으로 리다이렉트
    }
}
