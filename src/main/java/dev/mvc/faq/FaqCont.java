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

  // ===================== 의존성 주입 =====================
  /** FAQ 처리용 서비스 객체 */
  @Autowired
  @Qualifier("dev.mvc.faq.FaqProc")
  private FaqProcInter faqProc;

  // ===================== FAQ 등록 =====================

  /**
   * FAQ 등록 폼 페이지로 이동
   * - 관리자(admin, grade 1~4)만 접근 가능
   * - 비관리자는 접근 시 홈("/")으로 리다이렉트
   * @param session 현재 로그인 사용자 세션
   * @return 등록 페이지(templates/faq/create.html)
   */
  @GetMapping("/create")
  public String createForm(HttpSession session) {
    Integer grade = (Integer) session.getAttribute("grade");
    if (grade == null || grade < 1 || grade > 4) {
        return "redirect:/";  // 접근 권한 없음 → 홈으로 이동
    }
    
    return "/faq/create";
  }

  /**
   * FAQ 등록 처리
   * - 관리자(admin, grade 1~4)만 등록 가능
   * - 작성자 ID를 세션에서 추출해 VO에 저장
   * - 등록 후 notice/list로 리다이렉트 (통합 리스트로 이동)
   * @param faqVO 등록할 FAQ 정보
   * @param session 현재 로그인 사용자 세션
   */
  @PostMapping("/create")
  public String create(FaqVO faqVO, HttpSession session) {
    Integer grade = (Integer) session.getAttribute("grade");
    if (grade == null || grade < 1 || grade > 4) {
        return "redirect:/";  // 접근 권한 없음
    }
    
    // 작성자 ID 설정
    faqVO.setWriter_id((String) session.getAttribute("id"));
    
    // DB에 FAQ 생성
    faqProc.create(faqVO);
    return "redirect:/notice/list";
  }

  // ===================== FAQ 수정 =====================

  /**
   * FAQ 수정 폼 페이지
   * - 관리자(admin, grade 1~4)만 접근 가능
   * - 선택한 FAQ 데이터를 읽어서 수정 폼에 전달
   * @param faq_id 수정할 FAQ의 PK
   * @param model 뷰로 데이터 전달
   * @param session 현재 로그인 사용자 세션
   * @return 수정 페이지(templates/faq/update.html)
   */
  @GetMapping("/update")
  public String updateForm(@RequestParam("faq_id") int faq_id, Model model, HttpSession session) {
    Integer grade = (Integer) session.getAttribute("grade");
    if (grade == null || grade < 1 || grade > 4) {
        return "redirect:/";  // 접근 권한 없음
    }
    
    // 수정할 FAQ 데이터 조회
    FaqVO faqVO = faqProc.read(faq_id);
    model.addAttribute("faqVO", faqVO);
    
    return "/faq/update";
  }

  /**
   * FAQ 수정 처리
   * - 관리자(admin, grade 1~4)만 수정 가능
   * - 작성자 ID를 세션에서 설정
   * - 수정 후 notice/list로 이동
   * @param faqVO 수정된 FAQ 정보
   * @param session 현재 로그인 사용자 세션
   */
  @PostMapping("/update")
  public String update(FaqVO faqVO, HttpSession session) {
    Integer grade = (Integer) session.getAttribute("grade");
    if (grade == null || grade < 1 || grade > 4) {
        return "redirect:/";  // 접근 권한 없음
    }
    
    // 작성자 ID 갱신
    faqVO.setWriter_id((String) session.getAttribute("id"));
    
    // DB에 수정 반영
    faqProc.update(faqVO);
    return "redirect:/notice/list";
  }

  // ===================== FAQ 삭제 =====================

  /**
   * FAQ 삭제 처리
   * - 관리자(admin, grade 1~4)만 삭제 가능
   * - 삭제 후 notice/list로 이동
   * @param faq_id 삭제할 FAQ의 PK
   * @param session 현재 로그인 사용자 세션
   */
  @GetMapping("/delete")
  public String delete(@RequestParam("faq_id") int faq_id, HttpSession session) {
    Integer grade = (Integer) session.getAttribute("grade");
    if (grade == null || grade < 1 || grade > 4) {
        return "redirect:/";  // 접근 권한 없음
    }
    
    // DB에서 해당 FAQ 삭제
    faqProc.delete(faq_id);
    return "redirect:/notice/list";
  }
}
