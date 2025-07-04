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

import jakarta.servlet.http.HttpSession;

@RequestMapping("/qna")
@Controller
public class QnaCont {

  @Autowired
  @Qualifier("dev.mvc.qna.QnaProc")
  private QnaProcInter qnaProc;
  
  //GET: 글쓰기 페이지
  @GetMapping("/create")
  public String createForm() {
     return "/qna/create";  // /templates/qna/create.html
  }
  
  //글쓰기 처리 (POST)
  @PostMapping("/create")
  public String createProc(QnaVO qnaVO, HttpSession session) {  // ✔️ HttpSession은 그냥 받기만 하면 돼
      // 세션에서 로그인 정보 가져와서 무조건 넣어야 함
      qnaVO.setWriter_id((String) session.getAttribute("id"));
      qnaVO.setWriter_name((String) session.getAttribute("name"));
      qnaVO.setUser_type("user");  // 글쓰기 페이지에서는 'user' 고정 (너가 필요하면 공급자도 넣을 수 있음)
      
      System.out.println("제목: " + qnaVO.getTitle());
      System.out.println("카테고리: " + qnaVO.getCate());
      System.out.println("내용: " + qnaVO.getContent());
      System.out.println("작성자 ID: " + qnaVO.getWriter_id());

      this.qnaProc.create(qnaVO);  // ✅ 글 등록 (DB 저장)

      return "redirect:/qna/list_user";
  }

  /**
   * 사용자 유형별 Q&A 목록
   */
  @GetMapping("/list")
  public String qnaList(@RequestParam("userType") String userType, Model model) {
      List<QnaVO> qnaList = this.qnaProc.listByUserType(userType);
      model.addAttribute("qnaList", qnaList);
      return "/qna/list";  // /templates/qna/list.html 필요
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
  
  //공급자 Q&A (로그인 필요)
  @GetMapping("/list_supplier")
  public String qnaSupplierList(HttpSession session, Model model) {
     String grade = (String) session.getAttribute("grade");
     if (grade == null) {
         return "redirect:/member/login";  // 비로그인 시 로그인 페이지로
     }
     List<QnaVO> list = qnaProc.listByUserType("supplier");
     model.addAttribute("qnaSupplierList", list);
     return "/qna/list_supplier";  // 공급자용 Q&A 화면
  }

  /**
   * Q&A 상세 조회 (모두 열람 가능)
   */
  @GetMapping("/read")
  public String qnaRead(@RequestParam("qna_id") int qna_id, Model model, HttpSession session) {
    QnaVO qnaVO = this.qnaProc.read(qna_id);
    model.addAttribute("qnaVO", qnaVO);
    
    // ✅ session 값 model에 추가
    model.addAttribute("grade", session.getAttribute("grade"));
    model.addAttribute("id", session.getAttribute("id"));
    model.addAttribute("name", session.getAttribute("name"));
    model.addAttribute("userType", session.getAttribute("userType"));

    return "/qna/read";
  }
  
  @GetMapping("/update")
  public String qnaUpdateForm(@RequestParam("qna_id") int qna_id, HttpSession session, Model model) {
      QnaVO qnaVO = qnaProc.read(qna_id);
      String sessionId = (String) session.getAttribute("id");

      if (!qnaVO.getWriter_id().equals(sessionId)) {
          return "redirect:/qna/list_user";  // 작성자 본인만 수정 가능
      }

      model.addAttribute("qnaVO", qnaVO);
      return "/qna/update";
  }
  
  @PostMapping("/update")
  public String qnaUpdateProc(QnaVO qnaVO, @RequestParam(value = "cate", required = false) String cate) {
      qnaProc.update(qnaVO);
      return "redirect:/qna/list_user?cate=" + (cate == null ? "" : cate);
  }

  /**
   * 댓글 등록 (소비자는 소비자 Q&A에만, 공급자는 공급자 Q&A에만 가능)
   */
  @PostMapping("/comment")
  public String addComment(@RequestParam("qna_id") int qna_id,
                           @RequestParam("comment") String comment,
                           HttpSession session) {
      String grade = (String) session.getAttribute("grade");

      // ✅ 관리자만 댓글 가능
      if (!"admin".equals(grade)) {
          return "redirect:/qna/read?qna_id=" + qna_id;  // 관리자 아니면 차단
      }

      qnaProc.addComment(qna_id, comment);
      return "redirect:/qna/read?qna_id=" + qna_id;
  }
  
//  @PostMapping("/comment")
//  public String addComment(@RequestParam("qna_id") int qna_id, @RequestParam("comment") String comment, HttpSession session) {
//      String grade = (String) session.getAttribute("grade");
//      if (!"admin".equals(grade)) {
//          return "redirect:/qna/read?qna_id=" + qna_id;  // 관리자만 댓글 가능
//      }
//
//      qnaProc.addComment(qna_id, comment);
//      return "redirect:/qna/read?qna_id=" + qna_id;
//  }

}
