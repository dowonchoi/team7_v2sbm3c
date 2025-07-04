package dev.mvc.notice;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import dev.mvc.qna.QnaProcInter;
import dev.mvc.qna.QnaVO;
import jakarta.servlet.http.HttpSession;

@RequestMapping("/notice")
@Controller
public class NoticeCont {

  @Autowired
  private NoticeProcInter noticeProc;
  
  @Autowired
  @Qualifier("dev.mvc.qna.QnaProc")
  private QnaProcInter qnaProc;  // 기존에도 있던 거

  /**
   * 공지사항 등록 폼
   */
  @GetMapping("/create")
  public String createForm(HttpSession session) {
      String grade = (String) session.getAttribute("grade");
      if (grade == null || !grade.equals("admin")) {
          return "redirect:/";  // 비정상 접근 시 홈으로
      }
      return "/notice/create";
  }

  /**
   * 공지사항 등록 처리
   */
  @PostMapping("/create")
  public String create(HttpSession session, NoticeVO noticeVO,
                       @RequestParam("uploadImage") MultipartFile uploadImage) throws Exception {
      String grade = (String) session.getAttribute("grade");
      if (grade == null || !grade.equals("admin")) {
          return "redirect:/";
      }

      noticeVO.setWriter_id((String) session.getAttribute("id"));
      noticeVO.setWriter_name((String) session.getAttribute("mname"));

      if (!uploadImage.isEmpty()) {
          String uploadDir = "C:/kd/deploy/resort/notice/storage/";
          String filename = System.currentTimeMillis() + "_" + uploadImage.getOriginalFilename();
          uploadImage.transferTo(new File(uploadDir + filename));
          noticeVO.setImage(filename);
      }

      this.noticeProc.create(noticeVO);
      return "redirect:/notice/list";
  }

  /**
   * 공지사항 목록
   */
  @GetMapping("/list")
  public String list(Model model) {
    List<NoticeVO> noticeList = this.noticeProc.list();  // ✅ 변수명 변경
    model.addAttribute("noticeList", noticeList);        // ✅ 맞춰줌
    return "/notice/list";
  }
  
  /**
   * COMMUNITY 메인 페이지 (공지사항 포함)
   */
  @GetMapping("/community")
  public String community(Model model) {
      List<NoticeVO> noticeList = noticeProc.list();
//      List<FaqVO> faqList = faqProc.list();
      List<QnaVO> qnaUserList = qnaProc.listByUserType("user");
      List<QnaVO> qnaSupplierList = qnaProc.listByUserType("supplier");

      model.addAttribute("noticeList", noticeList);
//      model.addAttribute("faqList", faqList);
      model.addAttribute("qnaUserList", qnaUserList);
      model.addAttribute("qnaSupplierList", qnaSupplierList);

      return "/community/list";  // 방금 네가 만든 이 화면!
  }

  /**
   * 공지사항 상세 보기
   */
  @GetMapping("/read")
  public String read(@RequestParam("notice_id") int notice_id, Model model) {
    this.noticeProc.increaseViewCount(notice_id);
    NoticeVO noticeVO = this.noticeProc.read(notice_id);
    model.addAttribute("noticeVO", noticeVO);
    return "/notice/read";  // Thymeleaf 상세보기 페이지 경로
  }

  /**
   * 공지사항 수정 폼 (관리자만)
   */
  @GetMapping("/update")
  public String updateForm(@RequestParam("notice_id") int notice_id, Model model, HttpSession session) {
      String grade = (String) session.getAttribute("grade");
      if (grade == null || !grade.equals("admin")) {
          return "redirect:/";
      }
      NoticeVO noticeVO = this.noticeProc.read(notice_id);
      model.addAttribute("noticeVO", noticeVO);
      return "/notice/update";
  }

  /**
   * 공지사항 수정 처리 (관리자만)
   */
  @PostMapping("/update")
  public String update(NoticeVO noticeVO, HttpSession session) {
      String grade = (String) session.getAttribute("grade");
      if (grade == null || !grade.equals("admin")) {
          return "redirect:/";
      }
      this.noticeProc.update(noticeVO);
      return "redirect:/notice/read?notice_id=" + noticeVO.getNotice_id();
  }

  /**
   * 공지사항 삭제 (관리자만)
   */
  @GetMapping("/delete")
  public String delete(@RequestParam("notice_id") int notice_id, HttpSession session) {
      String grade = (String) session.getAttribute("grade");
      if (grade == null || !grade.equals("admin")) {
          return "redirect:/";
      }
      this.noticeProc.delete(notice_id);
      return "redirect:/notice/list";
  }

}
