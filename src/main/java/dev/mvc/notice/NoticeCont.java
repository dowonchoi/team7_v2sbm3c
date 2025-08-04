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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import dev.mvc.cate.CateProcInter;
import dev.mvc.cate.CateVOMenu;
import dev.mvc.faq.FaqProcInter;
import dev.mvc.qna.QnaProcInter;
import jakarta.servlet.http.HttpSession;

@RequestMapping("/notice")
@Controller
public class NoticeCont {

  // ======================== 의존성 주입 (DI) ========================

  /** 공지사항 비즈니스 로직 처리 객체 */
  @Autowired
  private NoticeProcInter noticeProc;

  /** FAQ 비즈니스 로직 처리 객체 (통합 목록에 포함) */
  @Autowired
  @Qualifier("dev.mvc.faq.FaqProc")
  private FaqProcInter faqProc;

  /** Q&A 비즈니스 로직 처리 객체 (통합 목록에 포함) */
  @Autowired
  @Qualifier("dev.mvc.qna.QnaProc")
  private QnaProcInter qnaProc;

  /** 카테고리 메뉴 처리 객체 */
  @Autowired
  @Qualifier("dev.mvc.cate.CateProc")
  private CateProcInter cateProc;

  // ======================== 공지사항 등록 ========================

  /**
   * 공지사항 등록 폼 이동
   * - 관리자만 접근 가능 (등급 1~4)
   * - 세션에서 grade(등급)를 확인하여 권한 체크
   *
   * @param session 현재 사용자 세션
   * @return 등록 페이지 (권한 없으면 에러 페이지로 이동)
   */
  @GetMapping("/create")
  public String createForm(HttpSession session) {
      Integer grade = (Integer) session.getAttribute("grade");
      if (grade == null || grade < 1 || grade > 4) {
          return "redirect:/error/permission"; // 권한 없음
      }
      return "notice/create"; // templates/notice/create.html
  }

  /**
   * 공지사항 등록 처리
   * - 이미지 업로드 처리 포함
   * - 작성자 ID/이름을 세션에서 추출하여 NoticeVO에 저장
   *
   * @param session     사용자 세션 (작성자 정보 추출)
   * @param noticeVO    등록할 공지사항 데이터
   * @param uploadImage 업로드 이미지 (선택)
   * @return 목록 페이지로 리다이렉트
   */
  @PostMapping("/create")
  public String create(HttpSession session, NoticeVO noticeVO,
                       @RequestParam("uploadImage") MultipartFile uploadImage) throws Exception {
      Integer grade = (Integer) session.getAttribute("grade");
      if (grade == null || grade < 1 || grade > 4) {
          return "redirect:/";
      }

      // 작성자 정보 (로그인한 사용자의 ID와 이름)
      noticeVO.setWriter_id((String) session.getAttribute("id"));
      noticeVO.setWriter_name((String) session.getAttribute("mname"));

      // 이미지 업로드 처리
      if (!uploadImage.isEmpty()) {
          String uploadDir = "C:/kd/deploy/team/notice/storage/";
          String filename = System.currentTimeMillis() + "_" + uploadImage.getOriginalFilename(); // 파일명 중복 방지
          uploadImage.transferTo(new File(uploadDir + filename));
          noticeVO.setImage(filename); // VO에 이미지 파일명 저장
      }

      this.noticeProc.create(noticeVO);
      return "redirect:/notice/list";
  }

  // ======================== 목록 출력 ========================

  /**
   * 공지사항 + FAQ + QnA 통합 목록 출력
   * - 공지사항, FAQ, QnA(사용자/공급자 구분)를 한 화면에 제공
   * - 상단 메뉴 데이터도 함께 전달
   *
   * @param model 뷰로 데이터 전달
   * @return 통합 목록 페이지
   */
  @GetMapping("/list")
  public String list(Model model) {
     model.addAttribute("noticeList", noticeProc.list());                 // 공지사항 목록
     model.addAttribute("faqList", faqProc.list());                       // FAQ 목록
     model.addAttribute("qnaUserList", qnaProc.listByUserType("user"));   // 사용자 QnA
     model.addAttribute("qnaSupplierList", qnaProc.listByUserType("supplier")); // 공급자 QnA

     List<CateVOMenu> menu = cateProc.menu(); // 상단 메뉴용 카테고리
     model.addAttribute("menu", menu);

     return "notice/list";
  }

  // ======================== 상세 보기 ========================

  /**
   * 공지사항 상세보기
   * - 조회수 증가 로직 포함
   *
   * @param notice_id 공지사항 ID
   * @param model     뷰에 데이터 전달
   * @return 상세보기 페이지
   */
  @GetMapping("/read")
  public String read(@RequestParam("notice_id") int notice_id, Model model) {
      this.noticeProc.increaseViewCount(notice_id);      // 조회수 증가
      NoticeVO noticeVO = this.noticeProc.read(notice_id); // 공지사항 조회
      model.addAttribute("noticeVO", noticeVO);
      return "notice/read";
  }

  // ======================== 수정 ========================

  /**
   * 공지사항 수정 폼
   * - 관리자(1~4)만 접근 가능
   * - 기존 데이터 조회 후 뷰에 전달
   */
  @GetMapping("/update")
  public String updateForm(@RequestParam("notice_id") int notice_id, Model model, HttpSession session) {
      Integer grade = (Integer) session.getAttribute("grade");
      if (grade == null || grade < 1 || grade > 4) {
          return "redirect:/";
      }
      NoticeVO noticeVO = this.noticeProc.read(notice_id);
      model.addAttribute("noticeVO", noticeVO);
      return "notice/update";
  }

  /**
   * 공지사항 수정 처리
   * - 이미지 변경 시 기존 이미지 삭제 후 새 이미지 저장
   * - 이미지 미변경 시 기존 이미지 유지
   */
  @PostMapping("/update")
  public String update(HttpSession session,
                       NoticeVO noticeVO,
                       @RequestParam("uploadImage") MultipartFile uploadImage,
                       @RequestParam("image") String image) throws Exception {
      Integer grade = (Integer) session.getAttribute("grade");
      if (grade == null || grade < 1 || grade > 4) {
          return "redirect:/";
      }

      NoticeVO oldNotice = this.noticeProc.read(noticeVO.getNotice_id());
      String uploadDir = "C:/kd/deploy/team/notice/storage/";

      // 새 이미지 업로드
      if (!uploadImage.isEmpty()) {
          String filename = System.currentTimeMillis() + "_" + uploadImage.getOriginalFilename();
          uploadImage.transferTo(new File(uploadDir + filename));

          // 기존 이미지 삭제
          if (oldNotice.getImage() != null) {
              File oldFile = new File(uploadDir + oldNotice.getImage());
              if (oldFile.exists()) {
                  oldFile.delete();
              }
          }
          noticeVO.setImage(filename);
      } else {
          // 새 이미지 미업로드 시 기존 이미지 유지
          noticeVO.setImage(image);
      }

      this.noticeProc.update(noticeVO);
      return "redirect:/notice/read?notice_id=" + noticeVO.getNotice_id();
  }

  // ======================== 삭제 ========================

  /**
   * 공지사항 삭제 처리
   * - 이미지가 존재할 경우 서버에서도 삭제
   * - 관리자만 접근 가능
   */
  @GetMapping("/delete")
  public String delete(@RequestParam("notice_id") int notice_id, HttpSession session) {
      Integer grade = (Integer) session.getAttribute("grade");
      if (grade == null || grade < 1 || grade > 4) {
          return "redirect:/";
      }

      NoticeVO noticeVO = this.noticeProc.read(notice_id);
      String uploadDir = "C:/kd/deploy/team/notice/storage/";

      // 서버 내 이미지 삭제
      if (noticeVO.getImage() != null) {
          File file = new File(uploadDir + noticeVO.getImage());
          if (file.exists()) {
              file.delete();
          }
      }

      this.noticeProc.delete(notice_id);
      return "redirect:/notice/list";
  }

  // ======================== 이미지 제공 ========================

  /**
   * 이미지 제공
   * - 브라우저에서 직접 이미지 출력 가능
   * - 이미지 파일이 존재하지 않을 경우 404 반환
   *
   * @param filename 요청한 이미지 파일명
   * @return 이미지 리소스 ResponseEntity
   */
  @GetMapping("/storage/{filename}")
  @ResponseBody
  public ResponseEntity<Resource> serveImage(@PathVariable("filename") String filename) {
      try {
          Path file = Paths.get("C:/kd/deploy/team/notice/storage/").resolve(filename);
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
