package dev.mvc.notice;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import dev.mvc.cate.CateProcInter;
import dev.mvc.cate.CateVOMenu;
import dev.mvc.faq.FaqProcInter;
import dev.mvc.faq.FaqVO;
import dev.mvc.qna.QnaProcInter;
import dev.mvc.qna.QnaVO;
import jakarta.servlet.http.HttpSession;

@RequestMapping("/notice")
@Controller
public class NoticeCont {

  @Autowired
  private NoticeProcInter noticeProc;
  
  @Autowired
  @Qualifier("dev.mvc.faq.FaqProc")
  private FaqProcInter faqProc;

  @Autowired
  @Qualifier("dev.mvc.qna.QnaProc")
  private QnaProcInter qnaProc;
  
  @Autowired
  @Qualifier("dev.mvc.cate.CateProc") // @Component("dev.mvc.cate.CateProc")
  private CateProcInter cateProc;

  // 공지사항 등록 폼
  @GetMapping("/create")
  public String createForm(HttpSession session) {
      Integer grade = (Integer) session.getAttribute("grade"); // ✅ 타입 일치
      if (grade == null || grade < 1 || grade > 4) {
          return "redirect:/error/permission";
      }
      return "/notice/create";
  }

  // 공지사항 등록 처리
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

  //COMMUNITY + 공지사항 통합 목록 (COMMUNITY 메인 화면)
  @GetMapping("/list")
  public String list(Model model) {
     model.addAttribute("noticeList", noticeProc.list());
     model.addAttribute("faqList", faqProc.list());
     model.addAttribute("qnaUserList", qnaProc.listByUserType("user"));
     model.addAttribute("qnaSupplierList", qnaProc.listByUserType("supplier"));
     
  // ✅ 전체 카테고리 메뉴 추가
     List<CateVOMenu> menu = cateProc.menu();
     model.addAttribute("menu", menu);
     
     return "/notice/list";  // COMMUNITY 탭형 메인 화면
  }

//  // COMMUNITY 메인 페이지
//  @GetMapping("/community")
//  public String community(Model model) {
//      List<NoticeVO> noticeList = noticeProc.list();
//      List<FaqVO> faqList = faqProc.list();
//      List<QnaVO> qnaUserList = qnaProc.listByUserType("user");
//      List<QnaVO> qnaSupplierList = qnaProc.listByUserType("supplier");
//
//      model.addAttribute("noticeList", noticeList);
//      model.addAttribute("faqList", faqProc.list());
//      model.addAttribute("qnaUserList", qnaUserList);
//      model.addAttribute("qnaSupplierList", qnaSupplierList);
//
//      return "/community/list";
//  }

  // 공지사항 상세 보기
  @GetMapping("/read")
  public String read(@RequestParam("notice_id") int notice_id, Model model) {
      this.noticeProc.increaseViewCount(notice_id);
      NoticeVO noticeVO = this.noticeProc.read(notice_id);
      model.addAttribute("noticeVO", noticeVO);
      return "/notice/read";
  }

  // 공지사항 수정 폼
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

  // 공지사항 수정 처리
  @PostMapping("/update")
  public String update(HttpSession session,
                       NoticeVO noticeVO,
                       @RequestParam("uploadImage") MultipartFile uploadImage,
                       @RequestParam("image") String image) throws Exception {
      String grade = (String) session.getAttribute("grade");
      if (grade == null || !grade.equals("admin")) {
          return "redirect:/";
      }

      NoticeVO oldNotice = this.noticeProc.read(noticeVO.getNotice_id());
      String uploadDir = "C:/kd/deploy/resort/notice/storage/";

      if (!uploadImage.isEmpty()) {
          String filename = System.currentTimeMillis() + "_" + uploadImage.getOriginalFilename();
          uploadImage.transferTo(new File(uploadDir + filename));

          if (oldNotice.getImage() != null) {
              File oldFile = new File(uploadDir + oldNotice.getImage());
              if (oldFile.exists()) {
                  oldFile.delete();
              }
          }

          noticeVO.setImage(filename);
      } else {
          noticeVO.setImage(image);
      }

      this.noticeProc.update(noticeVO);
      return "redirect:/notice/read?notice_id=" + noticeVO.getNotice_id();
  }

  // 공지사항 삭제
  @GetMapping("/delete")
  public String delete(@RequestParam("notice_id") int notice_id, HttpSession session) {
      String grade = (String) session.getAttribute("grade");
      if (grade == null || !grade.equals("admin")) {
          return "redirect:/";
      }

      NoticeVO noticeVO = this.noticeProc.read(notice_id);
      String uploadDir = "C:/kd/deploy/resort/notice/storage/";

      if (noticeVO.getImage() != null) {
          File file = new File(uploadDir + noticeVO.getImage());
          if (file.exists()) {
              file.delete();
          }
      }

      this.noticeProc.delete(notice_id);
      return "redirect:/notice/list";
  }

  // ✅ 이미지 제공 (꼭 추가)
  @GetMapping("/storage/{filename}")
  @ResponseBody
  public ResponseEntity<Resource> serveImage(@PathVariable("filename") String filename) {
      try {
          Path file = Paths.get("C:/kd/deploy/resort/notice/storage/").resolve(filename);
          Resource resource = new UrlResource(file.toUri());
          if (!resource.exists() || !resource.isReadable()) {
              throw new RuntimeException("파일을 읽을 수 없습니다.");
          }

          return ResponseEntity.ok()
                  .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                  .body(resource);
      } catch (Exception e) {
          return ResponseEntity.notFound().build();
      }
  }

}
