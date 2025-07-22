package dev.mvc.qna;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import dev.mvc.notification.NotificationProcInter;
import dev.mvc.notification.NotificationVO;
import jakarta.servlet.http.HttpSession;

@RequestMapping("/qna")
@Controller
public class QnaCont {

  @Autowired
  @Qualifier("dev.mvc.qna.QnaProc")
  private QnaProcInter qnaProc;
  
  @Autowired
  @Qualifier("dev.mvc.notification.NotificationProc")
  private NotificationProcInter notificationProc;
  
  //✅ 소비자용 글쓰기 폼
  @GetMapping("/create_user")
  public String createUserForm(HttpSession session) {
     Integer gradeObj = (Integer) session.getAttribute("grade");
     if (gradeObj == null || gradeObj < 16 || gradeObj > 39) { // 소비자 등급: 16~39
         return "redirect:/member/login";
     }
     return "/qna/create_user";
  }
  
  //✅ 공급자용 글쓰기 폼
  @GetMapping("/create_supplier")
  public String createSupplierForm(HttpSession session) {
     Integer gradeObj = (Integer) session.getAttribute("grade");
     if (gradeObj == null || gradeObj < 5 || gradeObj > 15) { // 공급자 등급: 5~15
         return "redirect:/member/login";
     }
     return "/qna/create_supplier";
  }
    
  //✅ 글쓰기 처리 (POST)
  @PostMapping("/create")
  public String createProc(QnaVO qnaVO, HttpSession session) {
     Integer gradeObj = (Integer) session.getAttribute("grade");
     String id = (String) session.getAttribute("id");
     String name = (String) session.getAttribute("name");
  
     if (gradeObj == null || id == null) {
         return "redirect:/member/login";
     }
  
     qnaVO.setWriter_id(id);
     qnaVO.setWriter_name(name);
     qnaVO.setMemberno((int) session.getAttribute("memberno"));
  
     // 공급자: 5~15, 소비자: 16~39
     if (gradeObj >= 5 && gradeObj <= 15) {
         qnaVO.setUser_type("supplier");
     } else if (gradeObj >= 16 && gradeObj <= 39) {
         qnaVO.setUser_type("user");
     } else {
         return "redirect:/qna/list_user"; // 비정상 접근 차단
     }
  
     this.qnaProc.create(qnaVO);
  
     // ✅ 관리자에게 알림 전송 (예: memberno 1)
     NotificationVO notificationVO = new NotificationVO();
     notificationVO.setMemberno(1); // 관리자 번호
     notificationVO.setType("qna");
  
     String title = qnaVO.getTitle();
     if (title.length() > 20) {
         title = title.substring(0, 20) + "...";
     }
  
     String writerType = qnaVO.getUser_type().equals("supplier") ? "공급자" : "소비자";
     notificationVO.setMessage(writerType + "의 Q&A [" + title + "]가 등록되었습니다.");
     notificationVO.setUrl("/notice/list");
     notificationVO.setIs_read("N");
  
     notificationProc.create(notificationVO);
  
     return "redirect:/notice/list";
  }

  // 사용자 유형별 Q&A 목록
  @GetMapping("/list")
  public String qnaList(@RequestParam(value = "userType", required = false, defaultValue = "user") String userType,
                        Model model) {
      List<QnaVO> qnaList = this.qnaProc.listByUserType(userType);
      model.addAttribute("qnaList", qnaList);
      return "/notice/list";
  }
    
  // 소비자 Q&A (비회원도 가능)
  @GetMapping("/list_user")
  public String qnaUserList(@RequestParam(value = "cate", required = false) String cate, Model model) {
      List<QnaVO> list;
      if (cate == null || cate.isEmpty()) {
          list = qnaProc.listByUserType("user");
      } else {
          list = qnaProc.listByUserTypeAndCate("user", cate);
      }
      model.addAttribute("qnaUserList", list);
      return "/qna/list_user";
  }
  
  // 공급자 Q&A (로그인 필요)
  @GetMapping("/list_supplier")
  public String qnaSupplierList(HttpSession session, Model model) {
     String grade = (String) session.getAttribute("grade");
     if (grade == null) {
         return "redirect:/member/login";
     }
     List<QnaVO> list = qnaProc.listByUserType("supplier");
     model.addAttribute("qnaSupplierList", list);
     return "/qna/list_supplier";
  }

  // Q&A 상세 조회 (모두 열람 가능)
  @GetMapping("/read")
  public String qnaRead(@RequestParam("qna_id") int qna_id, Model model, HttpSession session) {
    qnaProc.increaseViewCount(qna_id);  // 조회수 증가
    QnaVO qnaVO = this.qnaProc.read(qna_id);
    model.addAttribute("qnaVO", qnaVO);
    
    model.addAttribute("grade", session.getAttribute("grade"));
    model.addAttribute("id", session.getAttribute("id"));
    model.addAttribute("name", session.getAttribute("name"));
    model.addAttribute("userType", session.getAttribute("userType"));

    return "/qna/read";
  }
  
  // Q&A 수정 폼
  @GetMapping("/update")
  public String qnaUpdateForm(@RequestParam("qna_id") int qna_id, HttpSession session, Model model) {
      QnaVO qnaVO = qnaProc.read(qna_id);
      String sessionId = (String) session.getAttribute("id");

      if (!qnaVO.getWriter_id().equals(sessionId)) {
          return "redirect:/qna/list_user";
      }

      model.addAttribute("qnaVO", qnaVO);
      return "/qna/update";
  }
  
  // ✅ Q&A 수정 처리 → COMMUNITY 화면 이동
  @PostMapping("/update")
  public String qnaUpdateProc(QnaVO qnaVO, @RequestParam(value = "cate", required = false) String cate) {
      qnaProc.update(qnaVO);
      return "redirect:/notice/list";  // 수정 후 COMMUNITY 화면으로 이동
  }
  
  //✅ Q&A 삭제 (본인만 가능)
  @GetMapping("/delete")
  public String delete(@RequestParam("qna_id") int qna_id, HttpSession session) {
     QnaVO qnaVO = qnaProc.read(qna_id);
     String sessionId = (String) session.getAttribute("id");
  
     if (qnaVO == null || !qnaVO.getWriter_id().equals(sessionId)) {
         return "redirect:/qna/read?qna_id=" + qna_id;  // 권한 없으면 글 보기로
     }
  
     qnaProc.delete(qna_id);
  
     // 삭제 후 COMMUNITY 화면으로 이동
     return "redirect:/notice/list";
  }

  // 댓글 등록 (관리자만 가능)
  @PostMapping("/comment")
  public String addComment(@RequestParam("qna_id") int qna_id,
                           @RequestParam("comment") String comment,
                           HttpSession session) {
      String grade = (String) session.getAttribute("grade");
      if (!"admin".equals(grade)) {
          return "redirect:/qna/read?qna_id=" + qna_id;
      }

      qnaProc.addComment(qna_id, comment);
      return "redirect:/qna/read?qna_id=" + qna_id;
  }

  //답변 등록/수정 폼 (관리자만 접근 가능)
  @GetMapping("/reply")
  public String replyForm(@RequestParam("qna_id") int qna_id, HttpSession session, Model model) {
     String grade = (String) session.getAttribute("grade");
     if (!"admin".equals(grade)) {
         return "redirect:/qna/read?qna_id=" + qna_id;
     }
  
     QnaVO qnaVO = qnaProc.read(qna_id);
     model.addAttribute("qnaVO", qnaVO);
     return "/qna/reply";  // reply.html 로 이동
  }
  
  //답변 등록/수정 처리 (알림 포함)
  @PostMapping("/reply")
  public String replyProc(QnaVO qnaVO, HttpSession session) {
      String grade = (String) session.getAttribute("grade");
      if (!"admin".equals(grade)) {
          return "redirect:/qna/read?qna_id=" + qnaVO.getQna_id();
      }
  
      // ✅ 답변 저장
      qnaProc.updateReply(qnaVO);
  
      // ✅ 반드시 원래 질문 글에서 memberno 가져오기 (폼에는 안 넘어옴)
      QnaVO originalQna = qnaProc.read(qnaVO.getQna_id());
      int targetMemberno = originalQna.getMemberno();  // 질문 작성자의 memberno
      String title = originalQna.getTitle();  // ✅ 제목 가져오기

      // ✅ 제목 너무 길면 자르기 (선택)
      if (title.length() > 20) {
          title = title.substring(0, 20) + "...";
      }

      System.out.println("알림 대상 memberno: " + targetMemberno);  // 로그 확인용

      // ✅ 알림 저장
      NotificationVO notificationVO = new NotificationVO();
      notificationVO.setMemberno(targetMemberno);
      notificationVO.setType("qna");
      notificationVO.setMessage("문의하신 글 [" + title + "]에 답변이 등록되었습니다.");
      notificationVO.setUrl("/qna/read?qna_id=" + qnaVO.getQna_id());
      notificationProc.create(notificationVO);

      return "redirect:/qna/read?qna_id=" + qnaVO.getQna_id();
  }

}