package dev.mvc.qnacomment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/qna/comment")
public class QnaCommentCont {

    @Autowired
    private QnaCommentProcInter qnaCommentProc;

    @PostMapping("/create")
    public String create(QnaCommentVO vo, RedirectAttributes ra) {
        qnaCommentProc.create(vo);
        ra.addAttribute("qna_id", vo.getQna_id());
        return "redirect:/qna/read"; 
    }

    @PostMapping("/update")
    public String update(QnaCommentVO vo, RedirectAttributes ra) {
        qnaCommentProc.update(vo);
        ra.addAttribute("qna_id", vo.getQna_id());
        return "redirect:/qna/read";
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("comment_id") int comment_id, @RequestParam("qna_id") int qna_id, RedirectAttributes ra) {
        qnaCommentProc.delete(comment_id);
        ra.addAttribute("qna_id", qna_id);
        return "redirect:/qna/read";
    }
}