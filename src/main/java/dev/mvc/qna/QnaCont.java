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
  @Qualifier("dev.mvc.qna.QnaProc")  // QnaProc 구현체 주입
  private QnaProcInter qnaProc;
  
  @Autowired
  @Qualifier("dev.mvc.notification.NotificationProc")  // NotificationProc 구현체 주입
  private NotificationProcInter notificationProc;
  
  //===========================================================
  // ✅ 소비자용 Q&A 글쓰기 폼 (GET)
  // - 소비자 등급(16~39)만 접근 가능
  // - 등급이 없거나 범위를 벗어나면 로그인 페이지로 리다이렉트
  //===========================================================
  @GetMapping("/create_user")
  public String createUserForm(HttpSession session) {
     Integer gradeObj = (Integer) session.getAttribute("grade");
     if (gradeObj == null || gradeObj < 16 || gradeObj > 39) { // 소비자 등급: 16~39
         return "redirect:/member/login";
     }
     return "/qna/create_user"; // 소비자용 Q&A 작성 페이지
  }
  
  //===========================================================
  // ✅ 공급자용 Q&A 글쓰기 폼 (GET)
  // - 공급자 등급(5~15)만 접근 가능
  // - 등급이 없거나 범위를 벗어나면 로그인 페이지로 리다이렉트
  //===========================================================
  @GetMapping("/create_supplier")
  public String createSupplierForm(HttpSession session) {
     Integer gradeObj = (Integer) session.getAttribute("grade");
     if (gradeObj == null || gradeObj < 5 || gradeObj > 15) { // 공급자 등급: 5~15
         return "redirect:/member/login";
     }
     return "/qna/create_supplier"; // 공급자용 Q&A 작성 페이지
  }
    
  //===========================================================
  // ✅ Q&A 글쓰기 처리 (POST)
  // - 사용자 세션 정보(id, name, grade)를 QnaVO에 세팅 후 DB 저장
  // - 사용자 유형(공급자/소비자)에 따라 user_type 설정
  // - 글 작성 후 관리자에게 알림(Notification) 생성
  //===========================================================
  @PostMapping("/create")
  public String createProc(QnaVO qnaVO, HttpSession session) {
     Integer gradeObj = (Integer) session.getAttribute("grade");
     String id = (String) session.getAttribute("id");
     String name = (String) session.getAttribute("name");
  
     if (gradeObj == null || id == null) { // 비로그인 상태
         return "redirect:/member/login";
     }
  
     // 작성자 정보 세팅
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
  
     // Q&A 글 등록
     this.qnaProc.create(qnaVO);
  
     // ✅ 관리자에게 알림 전송 (관리자 memberno = 1 가정)
     NotificationVO notificationVO = new NotificationVO();
     notificationVO.setMemberno(1); // 관리자 번호
     notificationVO.setType("qna");
  
     // 알림 메시지에 사용할 제목 처리 (20자 이상이면 '...')
     String title = qnaVO.getTitle();
     if (title.length() > 20) {
         title = title.substring(0, 20) + "...";
     }
  
     String writerType = qnaVO.getUser_type().equals("supplier") ? "공급자" : "소비자";
     notificationVO.setMessage(writerType + "의 Q&A [" + title + "]가 등록되었습니다.");
     notificationVO.setUrl("/notice/list");  // 알림 클릭 시 이동할 URL
     notificationVO.setIs_read("N");         // 읽지 않음 상태
  
     notificationProc.create(notificationVO); // 알림 DB 저장
  
     return "redirect:/notice/list"; // 작성 후 COMMUNITY(공지/게시판) 화면 이동
  }

  //===========================================================
  // ✅ 사용자 유형별 Q&A 목록 (공급자 / 소비자)
  // - userType 파라미터에 따라 Q&A 목록 조회
  //===========================================================
  @GetMapping("/list")
  public String qnaList(@RequestParam(value = "userType", required = false, defaultValue = "user") String userType,
                        Model model) {
      List<QnaVO> qnaList = this.qnaProc.listByUserType(userType);
      model.addAttribute("qnaList", qnaList);
      return "/notice/list";
  }
    
  //===========================================================
  // ✅ 소비자 Q&A 목록 (비회원 접근 가능)
  // - cate 파라미터로 카테고리별 필터링 가능
  //===========================================================
  @GetMapping("/list_user")
  public String qnaUserList(@RequestParam(value = "cate", required = false) String cate, Model model) {
      List<QnaVO> list;
      if (cate == null || cate.isEmpty()) {
          list = qnaProc.listByUserType("user");  // 전체 소비자 Q&A 조회
      } else {
          list = qnaProc.listByUserTypeAndCate("user", cate); // 카테고리별 조회
      }
      model.addAttribute("qnaUserList", list);
      return "/qna/list_user";
  }
  
  //===========================================================
  // ✅ 공급자 Q&A 목록 (로그인 필요)
  // - 공급자/관리자 로그인 시 접근 가능
  //===========================================================
  @GetMapping("/list_supplier")
  public String qnaSupplierList(HttpSession session, Model model) {
     String grade = (String) session.getAttribute("grade");
     if (grade == null) {
         return "redirect:/member/login"; // 비로그인 시 접근 차단
     }
     List<QnaVO> list = qnaProc.listByUserType("supplier");
     model.addAttribute("qnaSupplierList", list);
     return "/qna/list_supplier";
  }

  //===========================================================
  // ✅ Q&A 상세 조회 (모든 사용자 접근 가능)
  // - 조회 시 조회수 증가 처리
  // - 세션에서 사용자 정보(grade, id, name)를 모델에 담아 뷰에 전달
  //===========================================================
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
  
  //===========================================================
  // ✅ Q&A 수정 폼
  // - 글 작성자 본인만 접근 가능
  //===========================================================
  @GetMapping("/update")
  public String qnaUpdateForm(@RequestParam("qna_id") int qna_id, HttpSession session, Model model) {
      QnaVO qnaVO = qnaProc.read(qna_id);
      String sessionId = (String) session.getAttribute("id");

      if (!qnaVO.getWriter_id().equals(sessionId)) {
          return "redirect:/qna/list_user"; // 본인 아님 → 목록 페이지로
      }

      model.addAttribute("qnaVO", qnaVO);
      return "/qna/update";
  }
  
  //===========================================================
  // ✅ Q&A 수정 처리 (POST)
  // - 수정 후 COMMUNITY(공지/게시판) 화면으로 리다이렉트
  //===========================================================
  @PostMapping("/update")
  public String qnaUpdateProc(QnaVO qnaVO, @RequestParam(value = "cate", required = false) String cate) {
      qnaProc.update(qnaVO);
      return "redirect:/notice/list";  // 수정 후 COMMUNITY 화면으로 이동
  }
  
  //===========================================================
  // ✅ Q&A 삭제
  // - 글 작성자 본인만 삭제 가능
  // - 삭제 후 COMMUNITY 화면 이동
  //===========================================================
  @GetMapping("/delete")
  public String delete(@RequestParam("qna_id") int qna_id, HttpSession session) {
     QnaVO qnaVO = qnaProc.read(qna_id);
     String sessionId = (String) session.getAttribute("id");
  
     if (qnaVO == null || !qnaVO.getWriter_id().equals(sessionId)) {
         return "redirect:/qna/read?qna_id=" + qna_id;  // 권한 없으면 글 보기로
     }
  
     qnaProc.delete(qna_id);
  
     return "redirect:/notice/list"; // 삭제 후 COMMUNITY 화면 이동
  }

  //===========================================================
  // ✅ 댓글 등록 (관리자 전용)
  // - 관리자 계정만 접근 가능
  //===========================================================
  @PostMapping("/comment")
  public String addComment(@RequestParam("qna_id") int qna_id,
                           @RequestParam("comment") String comment,
                           HttpSession session) {
      String grade = (String) session.getAttribute("grade");
      if (!"admin".equals(grade)) { // 관리자가 아니면 접근 불가
          return "redirect:/qna/read?qna_id=" + qna_id;
      }

      qnaProc.addComment(qna_id, comment);
      return "redirect:/qna/read?qna_id=" + qna_id;
  }

  //===========================================================
  // ✅ 답변 등록/수정 폼 (관리자 전용)
  // - 관리자 등급(1~4)만 접근 가능
  //===========================================================
  @GetMapping("/reply")
  public String replyForm(@RequestParam("qna_id") int qna_id, HttpSession session, Model model) {
    Integer grade = (Integer) session.getAttribute("grade");
    if (grade == null || grade < 1 || grade > 4) {
        return "redirect:/"; // 권한 없으면 홈으로
    } 
  
     QnaVO qnaVO = qnaProc.read(qna_id);
     model.addAttribute("qnaVO", qnaVO);
     return "/qna/reply";  // reply.html 로 이동
  }
  
  //===========================================================
  // ✅ 답변 등록/수정 처리 (POST)
  // - 관리자만 답변 작성 가능
  // - 답변 등록 후 질문 작성자에게 알림(Notification) 발송
  //===========================================================
  @PostMapping("/reply")
  public String replyProc(QnaVO qnaVO, HttpSession session) {
    Integer grade = (Integer) session.getAttribute("grade");
    if (grade == null || grade < 1 || grade > 4) {
        return "redirect:/"; // 권한 없으면 홈으로
    } 
  
      // 답변 내용 DB 저장
      qnaProc.updateReply(qnaVO);
  
      // 원본 질문글 정보 조회 (memberno와 제목)
      QnaVO originalQna = qnaProc.read(qnaVO.getQna_id());
      int targetMemberno = originalQna.getMemberno();  // 질문 작성자 memberno
      String title = originalQna.getTitle();

      // 제목 길면 잘라서 알림에 사용
      if (title.length() > 20) {
          title = title.substring(0, 20) + "...";
      }

      System.out.println("알림 대상 memberno: " + targetMemberno);  // 로그 확인
  
      // 알림 객체 생성 및 저장
      NotificationVO notificationVO = new NotificationVO();
      notificationVO.setMemberno(targetMemberno);
      notificationVO.setType("qna");
      notificationVO.setMessage("문의하신 글 [" + title + "]에 답변이 등록되었습니다.");
      notificationVO.setUrl("/qna/read?qna_id=" + qnaVO.getQna_id());
      notificationProc.create(notificationVO);

      return "redirect:/qna/read?qna_id=" + qnaVO.getQna_id();
  }
  
  //===========================================================
  // ✅ 답변 삭제 (관리자 전용)
  // - 답변 내용을 null로 초기화하여 삭제 처리
  //===========================================================
  @GetMapping("/reply_delete")
  public String replyDelete(@RequestParam("qna_id") int qna_id, HttpSession session) {
      // 관리자 권한 체크
      Integer grade = (Integer) session.getAttribute("grade");
      if (grade == null || grade < 1 || grade > 4) {
          return "redirect:/"; // 권한 없으면 홈으로
      }

      // 답변 내용 초기화
      QnaVO qnaVO = qnaProc.read(qna_id);
      if (qnaVO != null) {
          qnaVO.setReply(null);
          qnaVO.setReply_writer(null);
          qnaProc.updateReply(qnaVO);  // reply 컬럼만 업데이트하는 메서드
      }

      return "redirect:/qna/read?qna_id=" + qna_id;
  }

}
