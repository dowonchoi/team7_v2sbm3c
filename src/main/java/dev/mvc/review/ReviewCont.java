package dev.mvc.review;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/review")
public class ReviewCont {

  @Autowired
  @Qualifier("dev.mvc.review.ReviewProc")
  private ReviewProcInter reviewProc;
  
  @Autowired
  private ReviewLLMService reviewLLMService; // FastAPI 연동 서비스


  @GetMapping("/create")
  public String create_form(@RequestParam("productsno") int productsno, Model model, HttpSession session) {
    // 로그인 체크
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return "redirect:/member/login_cookie_need?url=/review/create?productsno=" + productsno;
    }

    model.addAttribute("productsno", productsno);
    return "review/create"; // review/create.html
  }

  @PostMapping("/create_proc")
  public String create_proc(ReviewVO reviewVO, HttpSession session) {
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return "redirect:/member/login_cookie_need?url=/review/create?productsno=" + reviewVO.getProductsno();
    }

    // 세션에서 작성자 정보 설정
    reviewVO.setMemberno(memberno);

    // 감정 분석 및 요약 처리 (FastAPI 연동 서비스 호출)
    reviewLLMService.process(reviewVO);  // 오류가 나더라도 예외 catch로 진행

    // DB에 리뷰 등록
    reviewProc.create(reviewVO);

    return "redirect:/products/read?productsno=" + reviewVO.getProductsno();
  }
  
  /**
   * 리뷰 수정 폼
   * http://localhost:9093/review/update?reviewno=3
   */
  @GetMapping("/update")
  public String update(HttpSession session, Model model,
                       @RequestParam(name = "reviewno") int reviewno) {
    // 1. 리뷰 번호로 기존 리뷰 정보 조회
    ReviewMemberVO reviewVO = this.reviewProc.read_with_member(reviewno);
    System.out.println("수정용 reviewVO: " + reviewVO); // ✅ 출력 확인
    
    // 2. View에 전달
    model.addAttribute("reviewVO", reviewVO);

    // 3. 리뷰 수정 폼으로 이동
    return "review/update"; // /templates/review/update.html
  }
  
  /**
   * 리뷰 수정 처리
 * POST 방식: /review/update_proc
 * 수정 가능한 항목: 리뷰 본문(content)만 수정
   */
  @PostMapping("/update_proc")
  public String update_proc(HttpSession session, Model model, ReviewVO reviewVO) {

    // 1. 세션에서 로그인 정보 가져오기
    Integer sessionMemberno = (Integer) session.getAttribute("memberno");
    String grade = (String) session.getAttribute("grade");

    if (sessionMemberno == null || grade == null) {
      return "redirect:/member/login_cookie_need";
    }

    // 2. 기존 리뷰 정보 가져오기
    ReviewVO dbVO = this.reviewProc.read(reviewVO.getReviewno());

    // 3. 작성자 본인만 수정 가능 (또는 관리자는 가능)
    if (sessionMemberno.equals(dbVO.getMemberno()) || grade.equals("admin")) {

      // 4. 감정 분석 및 요약 처리
      reviewLLMService.process(reviewVO); // 감정 분석 결과 및 요약 결과 reviewVO에 반영됨

      // 5. DB 반영
      int result = this.reviewProc.update(reviewVO);

      // 6. 성공 시 해당 상품 상세 페이지로 이동
      return "redirect:/products/read?productsno=" + dbVO.getProductsno();
    }

    // 권한 없음
    return "redirect:/member/login_cookie_need";
  }
  
  /**
   * 리뷰 삭제
   * GET 방식: /review/delete?reviewno=3
   */
  @GetMapping("/delete")
  public String delete(HttpSession session, @RequestParam(name = "reviewno") int reviewno) {

    // 1. 로그인 여부 확인
    Integer sessionMemberno = (Integer) session.getAttribute("memberno");
    String grade = (String) session.getAttribute("grade");

    if (sessionMemberno == null || grade == null) {
      return "redirect:/member/login_cookie_need";
    }

    // 2. 리뷰 정보 조회
    ReviewVO dbVO = this.reviewProc.read(reviewno);

    // 3. 권한 확인 (본인 or 관리자)
    if (sessionMemberno.equals(dbVO.getMemberno()) || grade.equals("admin")) {

      // 4. 삭제 수행
      int result = this.reviewProc.delete(reviewno);

      // 5. 성공 시 상품 상세로 redirect
      return "redirect:/products/read?productsno=" + dbVO.getProductsno();
    }

    // 권한 없음
    return "redirect:/member/login_cookie_need";
  }
  
  /**
   * 리뷰 삭제 처리 (POST 방식)
   * POST: /review/delete_proc
   */
  @PostMapping("/delete_proc")
  public String delete_proc(HttpSession session, @RequestParam("reviewno") int reviewno) {

    // 1. 로그인 정보 가져오기
    Integer sessionMemberno = (Integer) session.getAttribute("memberno");
    String grade = (String) session.getAttribute("grade");

    if (sessionMemberno == null || grade == null) {
      return "redirect:/member/login_cookie_need";
    }

    // 2. 리뷰 정보 가져오기
    ReviewVO dbVO = this.reviewProc.read(reviewno);

    // 3. 작성자 or 관리자만 삭제 가능
    if (sessionMemberno.equals(dbVO.getMemberno()) || grade.equals("admin")) {
      
      // 4. 삭제 수행
      int result = this.reviewProc.delete(reviewno);

      // 5. 해당 상품 상세로 redirect
      return "redirect:/products/read?productsno=" + dbVO.getProductsno();
    }

    // 6. 권한 없음
    return "redirect:/member/login_cookie_need";
  }



  




}
