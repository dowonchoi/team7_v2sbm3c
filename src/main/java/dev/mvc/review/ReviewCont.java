package dev.mvc.review;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dev.mvc.order.OrderProcInter;
import dev.mvc.tool.Tool;
import dev.mvc.tool.Upload;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/review")
public class ReviewCont {

  @Autowired
  @Qualifier("dev.mvc.order.OrderProc")
  private OrderProcInter orderProc;

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
    // 🔥 구매 이력 확인
    int count = orderProc.count_by_member_products(memberno, productsno);
    if (count == 0) {
      model.addAttribute("code", "review_not_allowed");
      model.addAttribute("msg", "상품을 구매한 회원만 리뷰를 작성할 수 있습니다.");
      model.addAttribute("productsno", productsno);
      return "products/msg";  // msg.html 템플릿을 만들어 보여주기
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

      // 1. 작성자 설정
      reviewVO.setMemberno(memberno);

      // 2. 업로드 디렉토리
      String upDir = Review.getUploadDir();
      //Tool.makeDir(upDir); // 폴더 없으면 생성

      // 3. 파일 업로드 처리
      // ✅ file1
      MultipartFile mf1 = reviewVO.getFile1MF();
      String file1 = mf1.getOriginalFilename();
      String file1saved = "";
      long size1 = mf1.getSize();
      if (size1 > 0) {
          file1saved = Upload.saveFileSpring(mf1, upDir);
      }
      reviewVO.setFile1(file1);
      reviewVO.setFile1saved(file1saved);
      reviewVO.setSize1(size1);

      // ✅ file2
      MultipartFile mf2 = reviewVO.getFile2MF();
      String file2 = mf2.getOriginalFilename();
      String file2saved = "";
      long size2 = mf2.getSize();
      if (size2 > 0) {
          file2saved = Upload.saveFileSpring(mf2, upDir);
      }
      reviewVO.setFile2(file2);
      reviewVO.setFile2saved(file2saved);
      reviewVO.setSize2(size2);

      // ✅ file3
      MultipartFile mf3 = reviewVO.getFile3MF();
      String file3 = mf3.getOriginalFilename();
      String file3saved = "";
      long size3 = mf3.getSize();
      if (size3 > 0) {
          file3saved = Upload.saveFileSpring(mf3, upDir);
      }
      reviewVO.setFile3(file3);
      reviewVO.setFile3saved(file3saved);
      reviewVO.setSize3(size3);

      // 4. 감정 분석 & 요약 (FastAPI)
      reviewLLMService.process(reviewVO);

      // 5. DB 저장
      reviewProc.create(reviewVO);

      // 6. 상품 상세 페이지로 리다이렉트
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

  /**
   * 리뷰 이미지 수정 폼 출력
   * @param reviewno 리뷰 기본키
   * @param model Model 객체에 reviewVO 담아 뷰로 전달
   * @param session 로그인 세션 (권한 확인에 사용 가능)
   * @return 이미지 수정 페이지 템플릿 이름
   */
  @GetMapping("/update_file")
  public String update_file(@RequestParam("reviewno") int reviewno,
                            Model model,
                            HttpSession session) {

    // 🔸 (1) 리뷰 1건 조회 (파일 포함)
    ReviewVO reviewVO = this.reviewProc.read(reviewno);

    // 🔸 (2) 화면 출력용으로 model에 등록
    model.addAttribute("reviewVO", reviewVO);

    // 🔸 (3) (선택) 로그인 정보로 본인 확인, 관리자 권한 검사도 가능
    // Integer memberno = (Integer) session.getAttribute("memberno");
    // if (memberno != reviewVO.getMemberno()) { ... }

    return "review/update_file";  // ⬅ /templates/review/update_file.html로 이동
  }
  
  /**
   * 리뷰 이미지 파일 수정 처리
   * - 기존 이미지 삭제 → 새 이미지 업로드 → DB update
   * @param reviewVO 폼에서 전달받은 리뷰 객체 (reviewno 포함)
   * @param ra 리다이렉트 파라미터
   * @param session 세션 정보
   * @return 상세보기 리다이렉트
   */
  @PostMapping("/update_file_proc")
  public String update_file_proc(@ModelAttribute ReviewVO reviewVO,
                                 RedirectAttributes ra,
                                 HttpSession session) {

    // 1. 기존 리뷰 정보
    ReviewVO oldVO = this.reviewProc.read(reviewVO.getReviewno());

    // 2. 업로드 경로 확보 (폴더 자동 생성)
    String upDir = Review.getUploadDir();

    // 3. 기존 파일 삭제
    Tool.deleteFile(upDir, oldVO.getFile1saved());
    Tool.deleteFile(upDir, oldVO.getFile2saved());
    Tool.deleteFile(upDir, oldVO.getFile3saved());

    // 4. 파일 업로드
    // file1
    MultipartFile mf1 = reviewVO.getFile1MF();
    String file1 = mf1.getOriginalFilename();
    String file1saved = "";
    long size1 = mf1.getSize();
    if (size1 > 0) {
      file1saved = Upload.saveFileSpring(mf1, upDir);
    }
    reviewVO.setFile1(file1);
    reviewVO.setFile1saved(file1saved);
    reviewVO.setSize1(size1);

    // file2
    MultipartFile mf2 = reviewVO.getFile2MF();
    String file2 = mf2.getOriginalFilename();
    String file2saved = "";
    long size2 = mf2.getSize();
    if (size2 > 0) {
      file2saved = Upload.saveFileSpring(mf2, upDir);
    }
    reviewVO.setFile2(file2);
    reviewVO.setFile2saved(file2saved);
    reviewVO.setSize2(size2);

    // file3
    MultipartFile mf3 = reviewVO.getFile3MF();
    String file3 = mf3.getOriginalFilename();
    String file3saved = "";
    long size3 = mf3.getSize();
    if (size3 > 0) {
      file3saved = Upload.saveFileSpring(mf3, upDir);
    }
    reviewVO.setFile3(file3);
    reviewVO.setFile3saved(file3saved);
    reviewVO.setSize3(size3);

    // 5. DB update
    this.reviewProc.update_file(reviewVO);

    // 6. redirect → 상품 상세
    ra.addAttribute("productsno", reviewVO.getProductsno());
    return "redirect:/products/read";
  }



  




}
