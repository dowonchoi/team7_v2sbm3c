package dev.mvc.review;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  /**
   * [GET] 리뷰 작성 폼 출력
   * - 특정 상품(productsno)에 대한 리뷰 작성 페이지로 이동
   * - 접근 조건:
   *     1) 로그인한 사용자만 접근 가능
   *     2) 해당 상품을 실제 구매한 사용자만 리뷰 작성 가능 (구매 인증)
   *
   * URL 예시:
   *     http://localhost:9093/review/create?productsno=123
   *
   * 처리 단계:
   *  (1) 세션에서 로그인 상태 확인 (memberno)
   *  (2) 구매 이력 확인 → orderProc.count_by_member_products() 호출
   *  (3) 조건 불충족 시 메시지 페이지로 리턴
   *  (4) 조건 충족 시 리뷰 작성 폼으로 이동
   */
  @GetMapping("/create")
  public String create_form(@RequestParam("productsno") int productsno, Model model, HttpSession session) {
    // (1) 로그인 여부 확인
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      // 로그인 안 된 경우 → 로그인 필요 페이지로 리디렉트
      // 로그인 후 다시 돌아올 수 있도록 url 파라미터에 현재 요청 주소 포함
      return "redirect:/member/login_cookie_need?url=/review/create?productsno=" + productsno;
    }
    // (2) 구매 이력 확인
    // - 구매한 상품이 아니면 리뷰 작성 불가
    int count = orderProc.count_by_member_products(memberno, productsno);
    if (count == 0) {
      // 구매 이력이 없으면 메시지 페이지로 안내
      model.addAttribute("code", "review_not_allowed");
      model.addAttribute("msg", "상품을 구매한 회원만 리뷰를 작성할 수 있습니다.");
      model.addAttribute("productsno", productsno);  // 상품 번호 유지 → 이후 이동 시 사용 가능
      return "products/msg"; // msg.html 템플릿을 만들어 보여주기
    }
    // (3) 조건 충족 → 리뷰 작성 폼으로 이동
    model.addAttribute("productsno", productsno);
    return "review/create"; // review/create.html
  }

  
  /**
   * [POST] 리뷰 작성 처리 + LLM 감정 분석 및 요약 기능
   *
   * 기능 개요:
   *  - 사용자가 작성한 리뷰(텍스트 + 이미지)를 서버에 저장
   *  - LLM 기반 감정 분석 및 요약 수행 (FastAPI 연동)
   *  - 리뷰 등록 후 상품 상세 페이지로 리다이렉트
   *
   * 주요 단계:
   *  (1) 로그인 체크
   *  (2) 리뷰 작성자(memberno) 설정
   *  (3) 이미지 파일 업로드 처리 (최대 3장)
   *  (4) AI 감정 분석 & 요약 실행
   *  (5) DB 저장
   *  (6) 상품 상세 페이지로 리다이렉트
   */
  @PostMapping("/create_proc")
  public String create_proc(ReviewVO reviewVO, HttpSession session) {
    // (1) 로그인 여부 확인
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return "redirect:/member/login_cookie_need?url=/review/create?productsno=" + reviewVO.getProductsno();
    }

    // (2) 리뷰 작성자 정보 설정
    reviewVO.setMemberno(memberno); // DB 저장 시 작성자 식별용

    // (3) 업로드 경로 설정
    String upDir = Review.getUploadDir(); // 리뷰 이미지 저장 디렉토리
    // Tool.makeDir(upDir); // 폴더 없으면 생성 (현재는 주석 처리)

    // (4) 파일 업로드 처리 (최대 3장)
    // file1
    MultipartFile mf1 = reviewVO.getFile1MF();  // 첫 번째 파일 객체
    String file1 = mf1.getOriginalFilename();   // 원본 파일명
    String file1saved = "";                     // 서버 저장 파일명
    long size1 = mf1.getSize();                 // 파일 크기
    if (size1 > 0) { // 파일이 업로드된 경우
      file1saved = Upload.saveFileSpring(mf1, upDir); // 실제 저장
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

    // (5) AI 감정 분석 & 요약 실행
    // -----------------------------------------------------
    // reviewVO.content를 기반으로 FastAPI 호출 → 감정/요약 결과 저장
    reviewLLMService.process(reviewVO);

    // (6) DB 저장
    reviewProc.create(reviewVO);

    // (7) 리뷰 등록 완료 후 → 상품 상세 페이지로 리다이렉트
    return "redirect:/products/read?productsno=" + reviewVO.getProductsno();
  }

  /**
   * [GET] 리뷰 수정 폼
   * - URL: http://localhost:9093/review/update?reviewno=3
   * - 기능: 특정 리뷰를 조회하여 수정 폼에 기존 데이터를 출력
   */
  @GetMapping("/update")
  public String update(HttpSession session, Model model, @RequestParam(name = "reviewno") int reviewno) {
    // (1) 수정할 리뷰 데이터 조회
    // reviewno로 리뷰 상세 데이터 + 작성자 정보 조회 (JOIN 사용)
    ReviewMemberVO reviewVO = this.reviewProc.read_with_member(reviewno);
    System.out.println("수정용 reviewVO: " + reviewVO); // ✅ 출력 확인

    // (2) View에 데이터 전달
    model.addAttribute("reviewVO", reviewVO);  // 수정 폼에서 기존 값 표시

    // (3) 리뷰 수정 폼 페이지로 이동
    return "review/update"; // /templates/review/update.html
  }

  /**
   * [POST] 리뷰 수정 처리
   * - URL: /review/update_proc
   * - 기능: 리뷰 본문(content)만 수정 가능
   * - 특이사항: 수정 시 AI 분석(감정 분석 + 요약) 다시 실행
   *
   * 요청 데이터:
   *   - reviewVO: 수정할 리뷰 데이터 (본문)
   * 처리 절차:
   *   1) 로그인/권한 확인
   *   2) 기존 리뷰 정보 조회
   *   3) 작성자 본인 또는 관리자만 수정 가능
   *   4) AI 분석 재실행
   *   5) DB 업데이트
   *   6) 상품 상세 페이지로 리다이렉트
   */
  @PostMapping("/update_proc")
  public String update_proc(HttpSession session, Model model, ReviewVO reviewVO) {

    // (1) 로그인 및 권한 확인
    Integer sessionMemberno = (Integer) session.getAttribute("memberno");
    String grade = (String) session.getAttribute("grade");

    if (sessionMemberno == null || grade == null) {
      return "redirect:/member/login_cookie_need";
    }

    // (2) 기존 리뷰 정보 조회
    ReviewVO dbVO = this.reviewProc.read(reviewVO.getReviewno()); // DB에서 원본 리뷰 데이터 가져오기

    // (3) 권한 확인: 본인 또는 관리자(admin)만 수정 가능
    if (sessionMemberno.equals(dbVO.getMemberno()) || grade.equals("admin")) {
      // (4) 본문(content) 수정 → AI 감정 분석 & 요약 재실행
      reviewLLMService.process(reviewVO); // FastAPI 호출 → 감정 분석 + 요약 갱신
      // (5) DB 업데이트 실행
      reviewProc.update(reviewVO); // 본문 수정 반영
      // (6) 상품 상세 페이지로 리다이렉트
      return "redirect:/products/read?productsno=" + dbVO.getProductsno();
    }

    // (7) 권한 없음 → 로그인 필요 페이지로 이동
    return "redirect:/member/login_cookie_need";
  }

  /**
   * [GET] 리뷰 삭제
   * - URL: /review/delete?reviewno=3
   * - 기능: 특정 리뷰를 삭제 (작성자 본인 또는 관리자만 가능)
   *
   * 요청 파라미터:
   *   - reviewno: 삭제할 리뷰 번호 (int)
   * 처리 절차:
   *   1) 로그인 여부 확인
   *   2) 리뷰 정보 조회
   *   3) 권한 확인 (본인 or 관리자)
   *   4) 리뷰 삭제 실행
   *   5) 상품 상세 페이지로 리다이렉트
   */
  @GetMapping("/delete")
  public String delete(HttpSession session, @RequestParam(name = "reviewno") int reviewno) {

    // (1) 로그인 여부 확인
    Integer sessionMemberno = (Integer) session.getAttribute("memberno");
    String grade = (String) session.getAttribute("grade");

    if (sessionMemberno == null || grade == null) {
      return "redirect:/member/login_cookie_need";
    }

    // (2) 삭제 대상 리뷰 정보 조회
    ReviewVO dbVO = this.reviewProc.read(reviewno); // DB에서 리뷰 데이터 가져오기

    // 3. 권한 확인 (본인 or 관리자)
    // - 작성자 본인 OR 관리자(admin)일 경우만 삭제 가능
    if (sessionMemberno.equals(dbVO.getMemberno()) || grade.equals("admin")) {

      // 4. 삭제 수행
      int result = this.reviewProc.delete(reviewno);
      System.out.println("삭제 결과: " + (result > 0 ? "성공" : "실패"));

      // 5. 성공 시 상품 상세로 redirect
      return "redirect:/products/read?productsno=" + dbVO.getProductsno();
    }

    // (6) 권한 없음 → 로그인 안내 페이지로 이동
    return "redirect:/member/login_cookie_need";
  }

  /**
   * [POST] 리뷰 삭제 처리
   * - URL: /review/delete_proc
   * - 기능: 특정 리뷰를 삭제 (작성자 본인 또는 관리자만 가능)
   *
   * 요청 파라미터:
   *   - reviewno (int): 삭제할 리뷰 번호
   * 처리 흐름:
   *   1) 로그인 여부 확인
   *   2) 삭제 대상 리뷰 조회
   *   3) 권한 확인 (본인 or 관리자)
   *   4) 삭제 실행
   *   5) 삭제 후 상품 상세 페이지로 리디렉트
   */
  @PostMapping("/delete_proc")
  public String delete_proc(HttpSession session, @RequestParam("reviewno") int reviewno) {

    // (1) 로그인 정보 확인
    Integer sessionMemberno = (Integer) session.getAttribute("memberno");
    String grade = (String) session.getAttribute("grade");

    // 로그인 정보가 없는 경우 → 로그인 필요 안내 페이지로 이동
    if (sessionMemberno == null || grade == null) {
      return "redirect:/member/login_cookie_need";
    }

    // (2) DB에서 삭제 대상 리뷰 정보 조회
    // - reviewProc.read(reviewno): reviewno 기반으로 리뷰 상세 데이터 가져오기
    ReviewVO dbVO = this.reviewProc.read(reviewno);

    // (3) 권한 확인
    // - 조건: (로그인한 사용자 == 리뷰 작성자) OR (관리자)
    if (sessionMemberno.equals(dbVO.getMemberno()) || grade.equals("admin")) {

      // (4) 권한 통과 → 리뷰 삭제 실행
      // - reviewProc.delete(reviewno): 실제 DB에서 삭제 처리
      int result = this.reviewProc.delete(reviewno);

      // (5) 삭제 완료 후 → 리뷰가 속한 상품 상세 페이지로 리다이렉트
      // - 상품 상세 페이지: /products/read?productsno=xxx
      return "redirect:/products/read?productsno=" + dbVO.getProductsno();
    }

    // (6) 권한이 없는 경우 → 로그인 안내 페이지로 이동 (보안 처리)
    return "redirect:/member/login_cookie_need";
  }

  /**
   * [GET] 리뷰 이미지 수정 폼 출력
   * - 요청 URL: /review/update_file
   * - 기능: 특정 리뷰의 이미지 수정 화면을 제공
   *
   * <처리 절차>
   * 1. reviewno(리뷰 번호) 기반으로 해당 리뷰 정보를 DB에서 조회 (이미지 포함)
   * 2. 조회한 리뷰 정보를 Model에 담아 뷰 템플릿에 전달
   * 3. (선택) 로그인 세션을 활용한 권한 확인 가능 (본인 작성자 또는 관리자)
   * 4. review/update_file.html 페이지로 이동
   *
   * @param reviewno 리뷰 식별자 (PK)
   * @param model    Spring의 Model 객체 (뷰로 데이터 전달)
   * @param session  HttpSession (로그인 사용자 정보 확인용)
   * @return 이미지 수정 폼을 렌더링할 Thymeleaf 템플릿 경로
   */
  @GetMapping("/update_file")
  public String update_file(@RequestParam("reviewno") int reviewno, Model model, HttpSession session) {

    // (1) 리뷰 상세 정보 조회 (이미지 포함)
    // - reviewProc.read(reviewno): 해당 reviewno에 대한 DB 조회 수행
    ReviewVO reviewVO = this.reviewProc.read(reviewno);

    // (2) 조회된 리뷰 객체를 Model에 담아 뷰로 전달
    // - 키 이름: "reviewVO"
    // - 뷰에서 ${reviewVO}로 접근 가능
    model.addAttribute("reviewVO", reviewVO);

    // (3) (선택) 권한 확인 로직 추가 가능
    // - 현재는 주석 처리, 필요 시 구현:
    //   Integer memberno = (Integer) session.getAttribute("memberno");
    //   if (!memberno.equals(reviewVO.getMemberno()) && !"admin".equals(session.getAttribute("grade"))) {
    //       return "redirect:/member/login_cookie_need";
    //   }
    
    // (4) Thymeleaf 템플릿 경로 반환
    return "review/update_file"; // ⬅ /templates/review/update_file.html로 이동
  }

  /**
   * [POST] 리뷰 이미지 파일 수정 처리
   * - 요청 URL: /review/update_file_proc
   * - 기능:
   *    1) 기존 이미지 삭제
   *    2) 새 이미지 업로드
   *    3) DB에 새로운 파일 정보 업데이트
   *
   * <처리 흐름>
   * 1. 기존 리뷰 데이터 조회 (기존 이미지 정보 확보)
   * 2. 업로드 경로 준비 (폴더 자동 생성)
   * 3. 기존 이미지 파일 삭제
   * 4. 업로드된 새 이미지 저장 (파일명, 사이즈 저장)
   * 5. DB 업데이트
   * 6. 수정 완료 후 상품 상세 페이지로 리디렉션
   *
   * @param reviewVO ReviewVO (수정할 리뷰 정보, reviewno 포함)
   * @param ra       RedirectAttributes (리다이렉트 시 파라미터 전달)
   * @param session  HttpSession (로그인 정보 확인 가능)
   * @return 상품 상세 페이지로 redirect
   */
  @PostMapping("/update_file_proc")
  public String update_file_proc(@ModelAttribute ReviewVO reviewVO, RedirectAttributes ra, HttpSession session) {

    // (1) 기존 리뷰 데이터 조회
    // - 기존 파일명 확인 후 삭제하기 위해 DB에서 해당 리뷰 정보를 가져옴
    ReviewVO oldVO = this.reviewProc.read(reviewVO.getReviewno());

    // (2) 업로드 경로 확보
    // - Review.getUploadDir(): 리뷰 이미지 저장 디렉터리 경로 반환
    // - 폴더가 없으면 자동 생성
    String upDir = Review.getUploadDir();

    // (3) 기존 파일 삭제
    // - 리뷰에 등록된 기존 이미지 3장을 모두 삭제 (있으면)
    Tool.deleteFile(upDir, oldVO.getFile1saved());
    Tool.deleteFile(upDir, oldVO.getFile2saved());
    Tool.deleteFile(upDir, oldVO.getFile3saved());

    // (4) 새 이미지 업로드
    // file1
    MultipartFile mf1 = reviewVO.getFile1MF(); // 업로드된 첫 번째 파일
    String file1 = mf1.getOriginalFilename();  // 원본 파일명
    String file1saved = "";                    // 서버 저장용 파일명
    long size1 = mf1.getSize();                // 파일 크기
    if (size1 > 0) {     // 파일 크기가 0보다 크면 → 업로드 처리
      file1saved = Upload.saveFileSpring(mf1, upDir);
    }
    // VO에 저장 (DB update 시 반영)
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

    // (5) DB update
    // - 새로운 이미지 정보로 리뷰 테이블 업데이트
    this.reviewProc.update_file(reviewVO);

    // (6) 수정 완료 후 상품 상세 페이지로 리디렉트
    // - productsno 전달 (상품 상세 화면에서 리뷰 목록 갱신)
    ra.addAttribute("productsno", reviewVO.getProductsno());
    return "redirect:/products/read";
  }

  /**
   * [GET] 리뷰 추가 로딩 (AJAX)
   * - 요청 URL: /review/more
   * - 기능: 상품 상세 페이지에서 "더보기" 버튼 클릭 시,
   *         특정 상품(productsno)에 대한 추가 리뷰 목록을 가져옴.
   *
   * <처리 절차>
   * 1. 요청 파라미터(productsno, offset, limit) 수신
   * 2. reviewProc.list_more() 호출하여 리뷰 목록 가져오기
   * 3. JSON 형태(List<ReviewMemberVO>)로 응답 반환
   *
   * @param productsno int - 리뷰를 가져올 대상 상품 번호
   * @param offset     int - 페이징을 위한 시작 위치 (이미 로딩된 리뷰 개수)
   * @param limit      int - 한 번에 불러올 리뷰 개수 (기본값: 3)
   * @return List<ReviewMemberVO> - JSON으로 직렬화되어 클라이언트로 전송
   */
  @GetMapping("/more")
  @ResponseBody
  public List<ReviewMemberVO> loadMoreReviews(@RequestParam("productsno") int productsno,
      @RequestParam("offset") int offset, @RequestParam(value = "limit", defaultValue = "3") int limit) {

    System.out.println(
        "[ReviewCont] loadMoreReviews 호출됨 - productsno: " + productsno + ", offset: " + offset + ", limit: " + limit);

    // (2) Proc → DAO 호출
    // - 지정한 상품번호(productsno)와 페이징 파라미터(offset, limit)로 리뷰 목록 가져오기
    List<ReviewMemberVO> reviewList = reviewProc.list_more(productsno, offset, limit);

    // (3) 디버깅용: 조회된 리뷰 개수 출력
    System.out.println("[ReviewCont] 조회된 리뷰 개수: " + reviewList.size());
    // (4) JSON으로 반환 (Thymeleaf 페이지가 아닌, AJAX 응답)
    return reviewList; // JSON으로 반환
  }

  /**
   * [GET] 리뷰 감정 분석 및 요약 정보 제공 (AJAX)
   * - 요청 URL: /review/analysis_ajax
   * - 기능: 특정 리뷰(reviewno)에 대해
   *         DB에 저장된 감정 분석 결과(emotion)와 요약(summary)을 JSON으로 반환
   *
   * <처리 절차>
   * 1. reviewno(리뷰 번호)로 리뷰 데이터 조회
   * 2. emotion(감정 분석)과 summary(요약) 값 추출
   * 3. JSON(Map<String, Object>) 형태로 반환
   *
   * @param reviewno int - 분석 정보를 조회할 리뷰 번호
   * @return Map<String, Object> - { "emotion": "...", "summary": "..." }
   */
  @GetMapping("/analysis_ajax")
  @ResponseBody
  public Map<String, Object> getReviewAnalysis(@RequestParam("reviewno") int reviewno) {
    // (1) DB에서 reviewno로 리뷰 정보 조회
    ReviewVO reviewVO = reviewProc.read(reviewno);
    // (2) 결과 Map 생성 후 감정(emotion)과 요약(summary) 추가
    Map<String, Object> result = new HashMap<>();
    result.put("emotion", reviewVO.getEmotion());
    result.put("summary", reviewVO.getSummary());
    // (3) JSON 형태로 반환 (AJAX 응답)
    return result;
  }

}