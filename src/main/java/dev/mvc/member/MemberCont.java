package dev.mvc.member;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import dev.mvc.cancel.CancelProcInter;
import dev.mvc.cancel.CancelVO;
import dev.mvc.cart.CartProcInter;
import dev.mvc.cate.CateProcInter;
import dev.mvc.cate.CateVOMenu;
import dev.mvc.inquiry.InquiryProcInter;
import dev.mvc.login.LoginProcInter;
import dev.mvc.login.LoginVO;
import dev.mvc.order.OrderProcInter;
import dev.mvc.order.OrderVO;
import dev.mvc.order_item.OrderItemProcInter;
import dev.mvc.order_item.OrderItemVO;
import dev.mvc.products.ProductsProc;
import dev.mvc.products.ProductsProcInter;
import dev.mvc.products.ProductsVO;
import dev.mvc.qna.QnaProcInter;
import dev.mvc.review.ReviewProc;
import dev.mvc.review.ReviewProcInter;
import dev.mvc.tool.Security;
import dev.mvc.tool.Tool;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RequestMapping("/member")
@Controller
public class MemberCont {
  
  //====================== DI 의존성 주입 ======================
  // @Autowired와 @Qualifier를 통해 각 서비스 클래스의 구현체를 주입받아 사용
  @Autowired
  @Qualifier("dev.mvc.member.MemberProc")  // 회원 처리 비즈니스 로직을 담당
  private MemberProcInter memberProc;
  
  @Autowired
  @Qualifier("dev.mvc.cate.CateProc")      // 카테고리(메뉴) 관련 처리
  private CateProcInter cateProc;
  
  @Autowired
  @Qualifier("dev.mvc.products.ProductsProc") // 상품 관련 처리
  private ProductsProcInter productsProc;
  
  @Autowired
  @Qualifier("dev.mvc.login.LoginProc")    // 로그인 이력 및 처리 담당
  private LoginProcInter loginProc;
  
  @Autowired
  @Qualifier("dev.mvc.order.OrderProc")    // 주문 처리
  private OrderProcInter orderProc;
  
  @Autowired
  @Qualifier("dev.mvc.order_item.OrderItemProc") // 주문 상세 처리
  private OrderItemProcInter orderItemProc;

  @Autowired
  @Qualifier("dev.mvc.cart.CartProc")      // 장바구니 처리
  private CartProcInter cartProc;

  @Autowired
  @Qualifier("dev.mvc.qna.QnaProc")        // QnA 처리
  private QnaProcInter qnaProc;
  
  @Autowired
  @Qualifier("dev.mvc.inquiry.InquiryProc") // 1:1 문의 처리
  private InquiryProcInter inquiryProc;
  
  @Autowired
  @Qualifier("dev.mvc.review.ReviewProc")   // 리뷰 처리
  private ReviewProcInter reviewProc;
  
  @Autowired
  @Qualifier("dev.mvc.cancel.CancelProc")   // 취소/반품/교환 처리
  private CancelProcInter cancelProc;
  
  @Autowired  // 회원 유틸리티 서비스 (추가 기능 처리용)
  private MemberService memberService;
  
  @Autowired  // 이메일 전송 서비스
  private MailService mailService;
  
  @Autowired  // 비밀번호 암호화 및 복호화를 위한 유틸
  private Security security;
  
  //===============================================================
  // 생성자: 컨트롤러가 생성될 때 콘솔 로그를 출력 (디버깅용)
  //===============================================================
  public MemberCont() {
    System.out.println("-> MemberCont created.");  
  }
  
  //====================== 아이디 중복 확인 ======================
  /**
   * AJAX 방식으로 아이디 중복 여부를 확인
   * @param id 사용자 입력 아이디
   * @return JSON 형태 ({"available": true/false})
   */
  @GetMapping("/check_id")
  @ResponseBody
  public Map<String, Object> checkId(@RequestParam("id") String id) {
      Map<String, Object> response = new HashMap<>();
      int count = memberProc.checkID(id); // DB에서 해당 ID의 존재 여부 확인
      response.put("available", count == 0); // 0이면 사용 가능
      return response;
  }
  
  //====================== 회원 가입 ======================
  /**
   * 회원 가입 폼 페이지 이동
   * @param model 뷰로 메뉴 데이터를 전달
   * @return 회원가입 HTML 페이지 경로
   */
  @GetMapping("/create")
  public String create_form(Model model) {
      // 상단 메뉴를 조회하여 화면에 전달 (헤더 UI에 사용)
      List<CateVOMenu> menu = cateProc.menu(); 
      model.addAttribute("menu", menu);
      return "member/create";
  }

  /**
   * 회원 가입 처리
   * @param model 뷰로 결과 메시지 전달
   * @param memberVO 사용자 입력 데이터를 VO로 바인딩
   * @param userType 가입 유형 (supplier/user)
   * @param businessFile 공급자 가입 시 업로드되는 인증 파일
   * @return 처리 결과 메시지 페이지
   */
  @PostMapping("/create")
  public String create_proc(Model model,
                            @ModelAttribute MemberVO memberVO,
                            @RequestParam(name = "userType", defaultValue = "user") String userType,
                            @RequestParam(name = "business_fileMF", required = false) MultipartFile businessFile) throws Exception {

      // ---------------------------------------------------------
      // 1. 아이디 중복 체크
      // ---------------------------------------------------------
      if (memberProc.checkID(memberVO.getId()) > 0) {
          model.addAttribute("code", "duplicate_id");
          model.addAttribute("msg", "이미 사용 중인 아이디입니다.");
          return "member/msg"; // 중복 시 안내 페이지로 이동
      }

      // ---------------------------------------------------------
      // 2. 회원 등급 자동 배정
      //    공급자: 5~15, 소비자: 16~39
      //    사용 중이지 않은 등급 번호를 순차적으로 탐색하여 배정
      // ---------------------------------------------------------
      int gradeStart = userType.equals("supplier") ? 5 : 16;
      int gradeEnd = userType.equals("supplier") ? 15 : 39;
      List<Integer> usedGrades = memberProc.getUsedGradesInRange(gradeStart, gradeEnd);
      int assignedGrade = -1;
      for (int i = gradeStart; i <= gradeEnd; i++) {
          if (!usedGrades.contains(i)) {
              assignedGrade = i;
              break;
          }
      }
      // 등급 번호를 모두 사용한 경우 오류 처리
      if (assignedGrade == -1) {
          model.addAttribute("code", "grade_limit");
          model.addAttribute("msg", "회원 수 초과");
          return "member/msg";
      }
      memberVO.setGrade(assignedGrade); // 결정된 등급 저장

      // ---------------------------------------------------------
      // 3. 공급자일 경우: 사업자 승인 상태를 '대기(N)'로 지정하고 파일 업로드 처리
      // ---------------------------------------------------------
      if ("supplier".equals(userType)) {
          memberVO.setSupplier_approved("N"); // 승인 대기 상태

          // 파일 업로드 로직
          if (businessFile != null && !businessFile.isEmpty()) {
              String uploadDir = "C:/kd/deploy/resort/member/storage/";
              File dir = new File(uploadDir);
              if (!dir.exists()) dir.mkdirs(); // 디렉토리가 없으면 생성

              String origin = businessFile.getOriginalFilename(); // 원본 파일명
              String ext = origin.substring(origin.lastIndexOf(".")); // 확장자 추출
              String saveName = UUID.randomUUID().toString() + ext;   // 고유한 파일명 생성
              File saveFile = new File(dir, saveName);
              businessFile.transferTo(saveFile); // 실제 서버 저장

              // VO에 파일 정보 저장
              memberVO.setBusiness_file(saveName);
              memberVO.setBusiness_file_origin(origin);
          }
      }

      // ---------------------------------------------------------
      // 4. DB에 회원 정보 등록
      // ---------------------------------------------------------
      int cnt = memberProc.create(memberVO);

      // ---------------------------------------------------------
      // 5. 처리 결과를 model에 담아 메시지 페이지로 전달
      // ---------------------------------------------------------
      model.addAttribute("code", (cnt == 1) ? "create_success" : "create_fail");
      model.addAttribute("cnt", cnt);
      model.addAttribute("mname", memberVO.getMname());
      model.addAttribute("id", memberVO.getId());

      return "member/msg"; // 결과 페이지
  }

  //====================== 관리자 - 공급자 승인/거절/취소 ======================
  /**
  * 관리자 전용: 승인 대기 상태의 공급자 목록 페이지로 이동
  * - DB에서 supplier_approved='N' 상태의 회원들을 조회
  * - 뷰 템플릿: admin/pending_suppliers.html
  */
  @GetMapping("/admin/pending_suppliers")
  public String pendingSuppliers(Model model) {
     // 승인 대기 중인 공급자 목록 조회
     List<MemberVO> list = memberProc.selectPendingSuppliers(); 
     // 뷰로 목록 전달
     model.addAttribute("supplierList", list);
     return "admin/pending_suppliers";
  }
  
  /**
  * 관리자 전용: 공급자 승인 처리
  * - 해당 memberno를 가진 공급자를 승인(Y) 상태로 변경
  * - 공급자 등급을 정식 등급(10)으로 업데이트
  * - AJAX로 success/fail 결과를 반환
  */
  @PostMapping("/admin/approveSupplier")
  @ResponseBody
  public String approveSupplier(@RequestParam("memberno") int memberno) {
     Map<String, Object> paramMap = new HashMap<>();
     paramMap.put("memberno", memberno);
     paramMap.put("supplier_approved", "Y"); // 승인 완료 상태로 변경
  
     // 승인 상태 업데이트
     int cnt1 = memberProc.updateSupplierApproved(paramMap); 
     // 공급자 등급을 10으로 부여
     int cnt2 = memberProc.updateGrade(memberno, 10); 
  
     // 두 쿼리가 모두 성공하면 success 반환
     return (cnt1 == 1 && cnt2 == 1) ? "success" : "fail";
  }
  
  /**
  * 관리자 전용: 공급자 승인 거절 처리
  * - supplier_approved='R' (Reject)
  * - 등급을 소비자 기본 등급(16)으로 변경
  */
  @PostMapping("/admin/rejectSupplier")
  @ResponseBody
  public String rejectSupplier(@RequestParam("memberno") int memberno) {
     Map<String, Object> paramMap = new HashMap<>();
     paramMap.put("memberno", memberno);
     paramMap.put("supplier_approved", "R"); // 거절 상태
  
     int cnt1 = memberProc.updateSupplierApproved(paramMap); 
     int cnt2 = memberProc.updateGrade(memberno, 16); // 소비자 등급으로 재설정
  
     return (cnt1 == 1 && cnt2 == 1) ? "success" : "fail";
  }
  
  /**
  * 관리자 전용: 승인 취소 처리
  * - 이미 승인된 공급자를 다시 승인 대기(N) 상태로 되돌림
  * - 등급을 대기 공급자 기본 등급(5)으로 복귀
  */
  @PostMapping("/admin/cancelApproval")
  @ResponseBody
  public String cancelApproval(@RequestParam("memberno") int memberno) {
     Map<String, Object> paramMap = new HashMap<>();
     paramMap.put("memberno", memberno);
     paramMap.put("supplier_approved", "N"); // 대기 상태로 변경
  
     int cnt1 = memberProc.updateSupplierApproved(paramMap); 
     int cnt2 = memberProc.updateGrade(memberno, 5); // 대기 등급 복귀
  
     return (cnt1 == 1 && cnt2 == 1) ? "success" : "fail";
  }
   
  //====================== 마이페이지 ======================
  /**
  * 마이페이지 화면
  * - 로그인한 회원의 기본 정보, 최근 주문, 취소/반품/교환, 장바구니, 포인트 등 요약 정보를 제공
  * - 관리자일 경우 전체 취소/교환/반품 내역도 포함
  * - 공급자일 경우 공급자 관련 취소/교환 내역을 별도로 제공
  */
  @GetMapping("/mypage")
  public String mypage(HttpSession session, Model model) {
     // 로그인 여부 확인
     Integer memberno = (Integer) session.getAttribute("memberno");
     if (memberno == null) {
         return "redirect:/member/login";
     }
  
     // 1. 회원 기본 정보 조회
     MemberVO memberVO = memberProc.read(memberno);
     model.addAttribute("memberVO", memberVO);
     
     // 2. 상단 카테고리 메뉴 불러오기 (공용 헤더 사용)
     List<CateVOMenu> menu = cateProc.menu();
     model.addAttribute("menu", menu);
  
     // 3. 최근 주문 내역 조회 (예: 최근 3건)
     List<OrderVO> recentOrders = orderProc.getRecentOrders(memberno);
     model.addAttribute("recentOrders", recentOrders);
     
     // 3-1. 각 주문별 상세 아이템 목록(OrderItem) 조회
     List<List<OrderItemVO>> allOrderItems = new ArrayList<>();
     for (OrderVO order : recentOrders) {
         int orderno = order.getOrderno();  // 주문번호
         List<OrderItemVO> orderItems = orderItemProc.list_by_orderno(orderno);
         allOrderItems.add(orderItems); // 주문별로 묶어서 리스트에 추가
     }
     model.addAttribute("allOrderItems", allOrderItems);
  
     // 4. 최근 본 상품
     List<ProductsVO> recentViewedProducts = productsProc.getRecentlyViewed(memberno);
     model.addAttribute("recentViewedProducts", recentViewedProducts);
     
     // 5. 최근 취소/교환/반품 내역
     List<CancelVO> recentCancels = cancelProc.recentByMember(memberno);
     model.addAttribute("recentCancels", recentCancels);
     
     // 6. 관리자라면 전체 취소/교환/반품 내역도 함께 제공
     if (memberVO.getGrade() >= 1 && memberVO.getGrade() <= 4) {
         List<CancelVO> cancelList = cancelProc.list_all(); 
         model.addAttribute("cancelList", cancelList);
     }
     
     // 7. 공급자일 경우, 해당 공급자와 연관된 취소 내역
     List<CancelVO> supplierCancelList = cancelProc.list_by_supplier(memberno);
     model.addAttribute("supplierCancelList", supplierCancelList);
  
     // 8. 각종 요약 통계 정보 (마이페이지 대시보드용)
     model.addAttribute("orderCount", orderProc.countOrders(memberno));          // 총 주문 건수
     model.addAttribute("cancelCount", cancelProc.countByMember(memberno));      // 취소 건수
     model.addAttribute("cartCount", cartProc.countItems(memberno));             // 장바구니 아이템 수
     model.addAttribute("pointAmount", memberProc.getPoint(memberno));           // 포인트 잔액
     model.addAttribute("reviewCount", reviewProc.countByMember(memberno));      // 리뷰 작성 수
     model.addAttribute("qnaCount", qnaProc.countByMember(memberno));            // QnA 작성 수
     model.addAttribute("inquiryCount", inquiryProc.countByMember(memberno));    // 1:1 문의 수
  
     return "member/mypage"; // 뷰 페이지 반환
  }
  
  //====================== 회원 목록 조회 (관리자 전용) ======================
  /**
  * 관리자 전용: 전체 회원 목록 조회 및 검색 기능
  * - 검색어가 없으면 전체 회원 목록 출력
  * - 검색어가 있으면 해당 키워드를 포함하는 회원만 출력
  * - 관리자가 아닌 경우 로그인 페이지로 리다이렉트
  */
  @GetMapping("/list")
  public String list(
    @RequestParam(name = "word", defaultValue = "") String word, // 검색어 (기본값: 빈 문자열)
    Model model, HttpSession session) {
  
    // 1. 관리자 권한 체크
    if (!memberProc.isAdmin(session)) {
        // 관리자가 아니면 로그인 필요 페이지로 리다이렉트
        return "redirect:/member/login_cookie_need?url=/member/list";
    }
  
    // 2. 회원 목록 조회
    List<MemberVO> list;
    if (word.trim().isEmpty()) {
        list = memberProc.list(); // 검색어가 없으면 전체 목록
    } else {
        Map<String, Object> map = new HashMap<>();
        map.put("word", word);
        list = memberProc.list_search(map); // 검색어 조건으로 필터링
    }
  
    // 3. 뷰에 데이터 전달
    model.addAttribute("list", list); // 회원 목록
    model.addAttribute("word", word); // 검색어 유지
  
    return "member/list"; // templates/member/list.html
  }
  
  /**
  * 검색 전용 페이지 (단일 책임 원칙 적용)
  * - 위 list 메서드와 동일하나 검색 결과만 처리
  */
  @GetMapping("/list_search")
  public String list_search(@RequestParam(name = "word", defaultValue = "") String word,
                          Model model, HttpSession session) {
    
    // 1. 관리자 권한 확인
    if (!memberProc.isAdmin(session)) {
        return "redirect:/member/login_cookie_need?url=/member/list_search";
    }
  
    // 2. 검색 실행
    List<MemberVO> list;
    if (word.trim().isEmpty()) {
        list = memberProc.list(); // 전체 목록
    } else {
        Map<String, Object> map = new HashMap<>();
        map.put("word", word);
        list = memberProc.list_search(map);
    }
  
    // 3. 검색 결과를 모델에 전달
    model.addAttribute("list", list);
    model.addAttribute("word", word);
  
    return "member/list_search"; // templates/member/list_search.html
  }
  
  /**
  * 검색 + 페이징 처리
  * - 페이지 번호와 검색어를 조합하여 결과를 분리
  * - 페이징 박스를 생성하여 UI에서 페이지 이동 가능
  */
  @GetMapping("/list_search_paging")
  public String list_search_paging(HttpSession session, Model model,
                                 @RequestParam(name = "word", defaultValue = "") String word,
                                 @RequestParam(name = "now_page", defaultValue = "1") int now_page) {
    // 1. 관리자 권한 체크
    if (!memberProc.isAdmin(session)) {
        return "redirect:/member/login_cookie_need?url=/member/list_search_paging";
    }
  
    // 2. 검색어 null 처리
    word = Tool.checkNull(word);
  
    // 3. 페이지당 레코드 수 및 페이지 블록 크기
    int record_per_page = 10; // 한 페이지에 10개씩 출력
    int page_per_block = 5;   // 한 블록에 5페이지씩 표시
  
    // 4. 현재 페이지에 해당하는 회원 목록 조회
    List<MemberVO> list = memberProc.list_search_paging(word, now_page, record_per_page);
    model.addAttribute("list", list);
  
    // 5. 검색된 총 레코드 수
    int search_count = memberProc.list_search_count(word);
  
    // 6. 페이징 UI 생성
    String paging = memberProc.pagingBox(now_page, word, "/member/list_search_paging",
                                         search_count, record_per_page, page_per_block);
    model.addAttribute("paging", paging);
  
    // 7. 출력 번호 (예: 30,29,28...)
    int no = search_count - ((now_page - 1) * record_per_page);
    model.addAttribute("no", no);
  
    model.addAttribute("word", word);
    model.addAttribute("now_page", now_page);
  
    return "member/list_search_paging";
  }
    
  //====================== 회원 정보 수정 ======================
  /**
  * 회원 정보 수정 폼
  * - 로그인된 회원의 정보를 DB에서 조회하여 수정 화면에 출력
  * - 로그인하지 않은 경우 로그인 페이지로 이동
  */
  @GetMapping("/update")
  public String updateForm(HttpSession session, Model model) {
   Integer memberno = (Integer) session.getAttribute("memberno");
   if (memberno == null) {
       // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
       return "redirect:/member/login";
   }
  
   // 현재 로그인한 회원의 정보를 DB에서 조회
   MemberVO memberVO = memberProc.read(memberno);
   // 조회된 정보를 모델에 담아 뷰로 전달
   model.addAttribute("memberVO", memberVO);
  
   return "member/update"; // templates/member/update.html
  }
  
  /**
  * 회원 정보 수정 처리
  * - 비밀번호가 입력되면 암호화 후 저장
  * - 수정 성공 여부에 따라 플래시 메시지 전달
  */
  @PostMapping("/update")
  public String updateProc(MemberVO memberVO, RedirectAttributes ra) {
    // 1. 비밀번호 암호화 처리
    if (memberVO.getPasswd() != null && !memberVO.getPasswd().isEmpty()) {
        String encrypted = security.aesEncode(memberVO.getPasswd());
        memberVO.setPasswd(encrypted);
    }
  
    // 2. DB 업데이트 실행
    int cnt = memberProc.update(memberVO);
  
    // 3. 결과 메시지 설정
    if (cnt == 1) {
        ra.addFlashAttribute("msg", "회원 정보가 수정되었습니다.");
    } else {
        ra.addFlashAttribute("msg", "회원 정보 수정에 실패했습니다.");
    }
  
    // 4. 마이페이지로 리다이렉트
    return "redirect:/member/mypage";
  }
  
  //====================== 아이디/비밀번호 찾기 ======================
  /**
   * 아이디 찾기 폼 페이지 이동
   * - 사용자가 이름과 전화번호를 입력할 수 있는 화면 제공
   */
  @GetMapping("/find_id")
  public String findIdForm() {
      return "member/find_id";  // 아이디 찾기 입력 폼
  }
  
  /**
   * 아이디 찾기 처리
   * - 입력받은 이름(mname)과 전화번호(tel)로 회원 정보를 조회
   * - 일치하는 회원이 있으면 해당 아이디를 결과 페이지에 전달
   * - 없으면 'notFound' 플래그를 true로 설정하여 에러 메시지 표시
   */
  @PostMapping("/find_id")
  public String findIdProc(@RequestParam("mname") String mname,
                           @RequestParam("tel") String tel,
                           Model model) {
  
      MemberVO memberVO = memberProc.findIdByNameAndTel(mname, tel); 
  
      if (memberVO != null) {
          model.addAttribute("foundId", memberVO.getId()); // 찾은 아이디를 모델에 전달
      } else {
          model.addAttribute("notFound", true); // 아이디를 찾지 못했음을 표시
      }
  
      return "member/find_id_result";  // 아이디 찾기 결과 페이지
  } 
  
  /**
   * 비밀번호 찾기 폼 페이지 이동
   * - 사용자가 아이디(이메일)를 입력할 수 있는 화면
   */
  @GetMapping("/find_passwd")
  public String findPasswdForm() {
      return "member/find_passwd";  // 비밀번호 찾기 입력 폼
  }
  
  /**
   * 비밀번호 찾기 처리
   * - 입력한 아이디가 유효한지 검사 후, 이메일 형식인지 확인
   * - 유효한 이메일이면 비밀번호 재설정 링크를 메일로 전송
   */
  @PostMapping("/find_passwd")
  public String findPasswdProc(@RequestParam("id") String id, Model model) {
      MemberVO member = memberProc.readById(id);
  
      // 1. 아이디 존재 여부 체크
      if (member == null) {
          model.addAttribute("msg", "존재하지 않는 아이디입니다.");
          return "member/find_passwd_fail";
      }
  
      // 2. 이메일 형식 여부 확인
      if (!isValidEmail(id)) {
          model.addAttribute("msg", "이메일 형식의 아이디만 비밀번호 재설정이 가능합니다.");
          return "member/find_passwd_fail";
      }
  
      // 3. 비밀번호 재설정 링크 생성 및 메일 전송
      String resetLink = "http://localhost:9093/member/reset_passwd_form?id=" + id;
      String subject = "[떨이몰] 비밀번호 재설정 링크입니다.";
      String content = "<p>안녕하세요.</p>"
                     + "<p>비밀번호 재설정을 원하신다면 아래 링크를 클릭해주세요.</p>"
                     + "<a href='" + resetLink + "'>비밀번호 재설정하기</a>";
  
      mailService.sendMail(id, subject, content);
  
      model.addAttribute("msg", "비밀번호 재설정 링크를 전송했습니다.");
      return "member/find_passwd_result";
  }
  
  /**
   * 이메일 유효성 검사
   * - 정규식을 이용하여 이메일 포맷 확인
   */
  public boolean isValidEmail(String email) {
     String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
     return email != null && email.matches(emailRegex);
  }
  
  //====================== 이메일 중복 확인 ======================
  /**
   * 이메일 중복 확인 (AJAX)
   * - DB에 동일 이메일이 있는지 확인하여 true/false 반환
   */
  @ResponseBody
  @GetMapping("/check_email")
  public Map<String, Object> checkEmail(@RequestParam("email") String email) {
     boolean available = memberProc.checkEmail(email) == 0; // 0이면 사용 가능
     Map<String, Object> response = new HashMap<>();
     response.put("available", available);
     return response;
  }
          
  //====================== 이메일 인증 코드 전송 ======================
  /**
   * 회원가입 시 이메일 인증 코드 발송
   * - 이메일 형식 확인 후, 6자리 인증 코드 생성
   * - 인증 코드를 세션에 저장 (1분 유효)
   * - 이메일로 인증 코드를 전송
   */
  @PostMapping("/send_code")
  @ResponseBody
  public String sendCode(@RequestParam("email") String email, HttpSession session) {
    // 이메일 형식 유효성 검사
    if (!email.matches("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
      return "invalid_email";
    }
  
    // 6자리 숫자 코드 생성
    String code = String.format("%06d", new Random().nextInt(999999));
    long expire = System.currentTimeMillis() + 1 * 60 * 1000; // 1분 유효
  
    // 세션에 인증 정보 저장
    session.setAttribute("authInfo:" + email, new AuthInfo(code, expire));
    boolean sent = mailService.sendVerificationMail(email, code);
  
    return sent ? "success" : "fail";
  }
  
  //====================== 이메일 인증 코드 확인 ======================
  /**
   * 사용자가 입력한 인증 코드 확인
   * - 세션에 저장된 인증 코드와 비교
   * - 5회 이상 시도 시 too_many_attempts 반환
   */
  @PostMapping("/verify_code")
  @ResponseBody
  public String verifyCode(@RequestParam("email") String email, 
                           @RequestParam("code") String userCode, 
                           HttpSession session) {
    AuthInfo info = (AuthInfo) session.getAttribute("authInfo:" + email);
    if (info == null) return "no_code";      // 코드 없음
    if (info.isExpired()) return "expired";  // 만료됨
    if (info.getAttemptCount() >= 5) return "too_many_attempts"; // 5회 이상 시도
  
    info.setAttemptCount(info.getAttemptCount() + 1); // 시도 횟수 증가
    if (info.getCode().equals(userCode)) {
      session.setAttribute("verified:" + email, true);  // 인증 완료 플래그 저장
      return "verified";
    }
    return "invalid"; // 인증 코드 불일치
  }
  
  //====================== 테스트용: 임의로 재설정 메일 발송 ======================
  /**
   * 관리자용 테스트 API
   * - 특정 이메일 주소로 재설정 링크를 강제로 발송
   */
  @GetMapping("/member/send_reset_email")
  @ResponseBody
  public String sendResetEmail(@RequestParam String email) {
      try {
          String resetLink = "http://localhost:9093/member/reset?code=123456&email=" + email;
          mailService.sendResetPasswordMail(email, resetLink);
          return "메일 발송 완료";
      } catch (Exception e) {
          e.printStackTrace();
          return "메일 발송 실패: " + e.getMessage();
      }
  }
  
  //====================== 비밀번호 재설정 폼 출력 ======================
  /**
   * 이메일 인증 후 비밀번호 재설정 페이지 이동
   * - 세션에 저장된 resetCode와 code를 비교하여 유효성 검사
   */
  @GetMapping("/reset")
  public String resetPasswdForm(@RequestParam("code") String code,
                                @RequestParam("email") String email,
                                HttpSession session,
                                Model model) {
  
      AuthInfo info = (AuthInfo) session.getAttribute("resetCode:" + email);
  
      if (info == null || info.isExpired() || !info.getCode().equals(code)) {
          model.addAttribute("msg", "인증 정보가 일치하지 않거나 만료되었습니다.");
          return "member/msg";
      }
  
      model.addAttribute("code", code);
      model.addAttribute("email", email);
      return "member/reset_passwd_form"; // 비밀번호 재설정 폼
  }
  
  /**
   * 실제 비밀번호 재설정 처리
   * - 사용자가 입력한 새 비밀번호를 AES 암호화 후 DB에 업데이트
   * - 세션의 인증 코드 정보를 삭제하여 보안 강화
   */
  @PostMapping("/reset_proc")
  public String resetPassword(@RequestParam("email") String email,
                              @RequestParam("code") String code,
                              @RequestParam("passwd") String passwd,
                              HttpSession session,
                              RedirectAttributes ra) {
  
      AuthInfo info = (AuthInfo) session.getAttribute("resetCode:" + email);
  
      if (info == null || info.isExpired() || !info.getCode().equals(code)) {
          ra.addFlashAttribute("msg", "인증 정보가 유효하지 않습니다.");
          return "redirect:/member/reset_msg";
      }
  
      // 비밀번호 암호화 후 DB 업데이트
      String aesPasswd = security.aesEncode(passwd);
      memberProc.updatePasswdById(email, aesPasswd);
  
      // 인증 정보 제거
      session.removeAttribute("resetCode:" + email);
  
      ra.addFlashAttribute("msg", "비밀번호가 성공적으로 변경되었습니다.");
      return "redirect:/member/reset_msg";
  }
  
  /**
   * 비밀번호 재설정 완료 메시지 페이지
   */
  @GetMapping("/reset_msg")
  public String resetMsg() {
      return "member/reset_msg";
  }


//  /**
//   * 조회
//   * @param model
//   * @param memberno 회원 번호
//   * @return 회원 정보
//   */
//  @GetMapping(value="/read")
//  public String read(Model model,
//                            @RequestParam(name="memberno", defaultValue = "") int memberno) {
//    System.out.println("-> read memberno: " + memberno);
//    
//    MemberVO memberVO = this.memberProc.read(memberno);
//    model.addAttribute("memberVO", memberVO);
//    
//    ArrayList<CateVOMenu> menu = this.cateProc.menu();
//    model.addAttribute("menu", menu);
//    
//    return "member/read";  // templates/member/read.html
//  }

  //====================== 회원 조회 ======================
  /**
   * 조회
   * @param model
   * @param memberno 회원 번호
   * @return 회원 정보
   */
  @GetMapping("/read")
  public String read(HttpSession session, Model model,
                     @RequestParam(name = "memberno") int memberno) {

      Integer grade = (Integer) session.getAttribute("grade");
      String sessionMembernoStr = String.valueOf(session.getAttribute("memberno"));
      int sessionMemberno = (sessionMembernoStr != null && !sessionMembernoStr.equals("null")) 
                            ? Integer.parseInt(sessionMembernoStr) : -1;

      // 비로그인 상태 → 로그인 요청
      if (grade == null) {
          return "redirect:/member/login_cookie_need";
      }

      // 관리자는 모든 회원 조회 가능 (예: 등급 1~4)
      if (grade >= 1 && grade <= 4) {
          MemberVO memberVO = this.memberProc.read(memberno);
          model.addAttribute("memberVO", memberVO);
          return "member/read";
      }

      // 본인 조회 가능
      if (sessionMemberno == memberno) {
          MemberVO memberVO = this.memberProc.read(memberno);
          model.addAttribute("memberVO", memberVO);
          return "member/read";
      }

      // 권한 없음
      return "redirect:/member/login_cookie_need";
  }

//  /**
//   * 수정 처리
//   * @param model
//   * @param memberVO
//   * @return
//   */
//  @PostMapping(value="/update")
//  public String update_proc(Model model, 
//                                       @ModelAttribute("memberVO") MemberVO memberVO) {
//    int cnt = this.memberProc.update(memberVO); // 수정
//    
//    if (cnt == 1) {
//      model.addAttribute("code", "update_success");
//      model.addAttribute("mname", memberVO.getMname());
//      model.addAttribute("id", memberVO.getId());
//    } else {
//      model.addAttribute("code", "update_fail");
//    }
//    
//    model.addAttribute("cnt", cnt);
//    
//    return "member/msg"; // /templates/member/msg.html
//  }

//  /**
//   * 삭제
//   * @param model
//   * @param memberno 회원 번호
//   * @return 회원 정보
//   */
//  @GetMapping(value="/delete")
//  public String delete(Model model,
//                              @RequestParam(name="memberno", defaultValue = "") Integer memberno) {
//    System.out.println("-> delete memberno: " + memberno);
//    
//    MemberVO memberVO = this.memberProc.read(memberno);
//    model.addAttribute("memberVO", memberVO);
//    
//    return "member/delete";  // templates/member/delete.html
//  }
//  
//  /**
//   * 회원 Delete process
//   * @param model
//   * @param memberno 삭제할 레코드 번호
//   * @return
//   */
//  @PostMapping(value="/delete")
//  public String delete_process(Model model, 
//                                          @RequestParam(name="memberno", defaultValue = "") Integer memberno) {
//    int cnt = this.memberProc.delete(memberno);
//    
//    if (cnt == 1) {
//      return "redirect:/member/list"; // @GetMapping(value="/list")
//    } else {
//      model.addAttribute("code", "delete_fail");
//      return "member/msg"; // /templates/member/msg.html
//    }
//  }

  //====================== 회원 탈퇴 (AJAX 및 Form) ======================
  /**
   * 회원 탈퇴 처리 (AJAX 방식)
   * - 사용자가 입력한 비밀번호를 확인 후, 등급을 '탈퇴 회원 등급(40~59)'으로 변경하여 탈퇴 처리
   * - 비밀번호가 일치하면 세션을 무효화하고 'success' 반환
   * - 비밀번호 불일치 시 'fail', 처리 중 오류가 발생하면 'error' 반환
   *
   * @param memberno 탈퇴할 회원 번호
   * @param passwd   사용자가 입력한 비밀번호
   * @return         탈퇴 처리 결과 문자열 (success/fail/error)
   */
  @PostMapping(value = "/delete", produces = "text/plain; charset=UTF-8")
  @ResponseBody
  public String delete_ajax(@RequestParam("memberno") int memberno,
                            @RequestParam("passwd") String passwd,
                            HttpSession session) {
      try {
          // 1. 회원 정보 조회
          MemberVO memberVO = memberProc.read(memberno);
  
          if (memberVO == null) {
              System.out.println("회원 정보 없음");
              return "error"; // 회원 정보가 없을 경우
          }
  
          // 2. 사용자가 입력한 비밀번호를 암호화하여 DB 값과 비교
          String encryptedInput = security.aesEncode(passwd);
          String dbPasswd = memberVO.getPasswd();
  
          System.out.println("입력 암호화: " + encryptedInput);
          System.out.println("DB 암호화: " + dbPasswd);
  
          if (dbPasswd != null && dbPasswd.equals(encryptedInput)) {
              // 3. 비밀번호 일치 시 탈퇴 처리 (등급 변경 방식)
              int cnt = memberProc.withdraw(memberno); 
  
              if (cnt == 1) {
                  // 세션 무효화 (자동 로그아웃)
                  session.invalidate();
                  return "success";
              } else {
                  System.out.println("탈퇴 처리 실패 cnt: " + cnt);
                  return "error";
              }
          } else {
              // 4. 비밀번호 불일치 시
              System.out.println("비밀번호 불일치");
              return "fail";
          }
      } catch (Exception e) {
          e.printStackTrace();
          return "error";
      }
  }
  
  /**
   * 관리자 전용 - 회원 완전 삭제 처리
   * - 관리자만 접근 가능
   * - 회원 데이터를 DB에서 완전히 삭제 (탈퇴 등급이 아닌 완전 삭제)
   *
   * @param memberno 삭제할 회원 번호
   * @return         처리 결과 (success/fail/unauthorized)
   */
  @PostMapping(value = "/delete_by_admin", produces = "text/plain; charset=UTF-8")
  @ResponseBody
  public String deleteByAdmin(@RequestParam("memberno") int memberno, HttpSession session) {
      Object gradeObj = session.getAttribute("grade");
      if (gradeObj == null || !"admin".equals(gradeObj.toString())) {
          return "unauthorized"; // 관리자 권한이 아닐 경우
      }
  
      int cnt = this.memberProc.delete(memberno);  // DB 완전 삭제
      return cnt == 1 ? "success" : "fail";
  }
  
  /**
   * 회원 탈퇴용 비밀번호 확인 폼 페이지 이동
   * - 현재 로그인한 회원의 정보를 조회하여 탈퇴 폼에 출력
   */
  @GetMapping("/delete")
  public String deleteForm(HttpSession session, Model model) {
     Integer memberno = (Integer) session.getAttribute("memberno");
     if (memberno == null) {
         return "redirect:/member/login"; // 로그인하지 않은 경우 로그인 페이지로 이동
     }
  
     MemberVO memberVO = memberProc.read(memberno);
     model.addAttribute("memberVO", memberVO); // 현재 회원 정보 전달
  
     return "member/delete"; // member/delete.html
  }
  
  /**
   * 회원 탈퇴 처리 (Form 방식)
   * - 사용자가 입력한 비밀번호 확인 후 탈퇴 처리
   * - 성공 시 세션 종료 및 완료 메시지 출력
   */
  @PostMapping("/delete")
  public String deleteProc(HttpSession session,
                           @RequestParam("memberno") int memberno,
                           @RequestParam("passwd") String passwd,
                           Model model) {
      // 1. 회원 정보 조회
      MemberVO memberVO = memberProc.read(memberno);
      String encrypted = security.aesEncode(passwd); // 비밀번호 암호화
  
      // 2. 비밀번호 비교
      if (memberVO != null && memberVO.getPasswd().equals(encrypted)) {
          int cnt = memberProc.withdraw(memberno); // 탈퇴 처리 (등급 변경 방식)
          if (cnt == 1) {
              session.invalidate(); // 로그아웃 처리
              model.addAttribute("msg", "회원 탈퇴가 완료되었습니다.");
          } else {
              model.addAttribute("msg", "회원 탈퇴 처리 중 오류 발생");
          }
      } else {
          model.addAttribute("msg", "비밀번호가 일치하지 않습니다.");
      }
  
      return "member/delete_msg"; // 결과 메시지 출력 페이지
  }
  
  //====================== 탈퇴 회원 조회 및 복구 ======================
  /**
   * 탈퇴 완료 메시지 페이지
   * - 탈퇴 처리 후 안내 메시지를 출력하는 뷰로 이동
   */
  @GetMapping("/delete_msg")
  public String deleteMsg() {
     return "member/delete_msg";
  }
  
  /**
   * 관리자 전용 - 탈퇴 회원 목록 페이지
   * - 등급 1~4(관리자)만 접근 가능
   * - 탈퇴한 회원(등급 40~59) 목록을 조회하여 뷰에 전달
   */
  @GetMapping("/withdrawn_list")
  public String withdrawnList(HttpSession session, Model model) {
     Integer grade = (Integer) session.getAttribute("grade");
  
     if (grade == null || grade > 4) {
         return "redirect:/member/login"; // 권한 없는 경우 로그인 페이지로 이동
     }
  
     List<MemberVO> withdrawnList = memberProc.selectWithdrawnMembers();
     model.addAttribute("withdrawnList", withdrawnList);
  
     return "member/withdrawn_list"; // 탈퇴 회원 목록 페이지
  }
  
  /**
   * 관리자 전용 - 탈퇴 회원 복구 처리
   * - 선택한 탈퇴 회원을 정상 회원(소비자/공급자)으로 복구
   */
  @PostMapping("/restore")
  public String restore(@RequestParam("memberno") int memberno,
                        @RequestParam("userType") String userType,
                        RedirectAttributes ra) {
      int cnt = memberProc.restoreMember(memberno, userType);
  
      if (cnt == 1) {
          ra.addFlashAttribute("msg", "복구 성공");
      } else {
          ra.addFlashAttribute("msg", "복구 실패");
      }
  
      return "redirect:/member/withdrawn_list"; // 목록 페이지로 리다이렉트
  }
  
  //====================== 회원 등급 확인 메서드 ======================
  /**
   * 현재 세션 사용자가 관리자(1~4)인지 확인
   */
  public boolean isAdmin(HttpSession session) {
    Integer grade = (Integer) session.getAttribute("grade");
    return grade != null && (grade >= 1 && grade <= 4);
  }
  
  /**
   * 현재 세션 사용자가 공급자(5~15)인지 확인
   */
  public boolean isSupplier(HttpSession session) {
      Integer grade = (Integer) session.getAttribute("grade");
      return grade != null && (grade >= 5 && grade <= 15);
  }
  
  /**
   * 현재 세션 사용자가 일반 사용자(16~39)인지 확인
   */
  public boolean isUser(HttpSession session) {
      Integer grade = (Integer) session.getAttribute("grade");
      return grade != null && (grade >= 16 && grade <= 39);
  }
  
  /**
   * 현재 세션 사용자가 탈퇴 회원(40~59)인지 확인
   */
  public boolean isWithdrawn(HttpSession session) {
      Integer grade = (Integer) session.getAttribute("grade");
      return grade != null && (grade >= 40 && grade <= 59);
  }


//  @GetMapping("/delete_confirm")
//  public String deleteConfirm() {
//      return "member/delete_confirm";
//  }
  
//  /**
//   * 로그인
//   * @param model
//   * @param memberno 회원 번호
//   * @return 회원 정보
//   */
//  @GetMapping(value="/login")
//  public String login_form(Model model) {
//    return "/member/login";   // templates/member/login.html
//  }
//  
//  /**
//   * 로그인 처리
//   * @param model
//   * @param memberno 회원 번호
//   * @return 회원 정보
//   */
//  @PostMapping(value="/login")
//  public String login_proc(HttpSession session, Model model, 
//                                    @RequestParam(name="id", defaultValue = "") String id,
//                                    @RequestParam(name="passwd", defaultValue = "") String passwd) {
//    HashMap<String, Object> map = new HashMap<String, Object>();
//    map.put("id", id);
//    map.put("passwd", passwd);
//    
//    int cnt = this.memberProc.login(map);
//    System.out.println("-> login_proc cnt: " + cnt);
//    
//    model.addAttribute("cnt", cnt);
//    
//    if (cnt == 1) {
//      MemberVO memverVO = this.memberProc.readById(id); // 로그인한 회원 정보를 읽어 session에 저장
//      session.setAttribute("memberno", memverVO.getMemberno());
//      session.setAttribute("id", memverVO.getId());
//      session.setAttribute("mname", memverVO.getMname());
//      session.setAttribute("grade", memverVO.getGrade());
//      
//      return "redirect:/";
//    } else {
//      model.addAttribute("code", "login_fail");
//      return "member/msg";
//    }
//    
//  }
  
  //====================== 로그인/로그아웃/쿠키 로그인 ======================
  /**
   * 로그아웃 처리
   * - 현재 로그인 세션을 완전히 종료(모든 세션 속성 삭제)
   * - 로그아웃 후 메인 페이지로 리다이렉트
   */
  @GetMapping(value="/logout")
  public String logout(HttpSession session, Model model) {
    session.invalidate();  // 모든 세션 변수 삭제 (로그아웃)
    return "redirect:/";   // 메인 페이지로 이동
  }
  
  // ----------------------------------------------------------------------------------
  // Cookie 사용 로그인 관련 코드 시작
  // ----------------------------------------------------------------------------------
  /**
   * 로그인 폼 출력 (쿠키 기반)
   * - 기존에 저장된 아이디/비밀번호 쿠키값을 읽어서 로그인 폼에 미리 세팅
   * - 아이디/비밀번호 저장 체크박스 상태도 쿠키 값에 따라 설정
   *
   * @param request  클라이언트의 쿠키 정보를 가져오기 위함
   * @param model    로그인 폼으로 전달할 쿠키 정보
   * @return         로그인 폼 뷰 (member/login_cookie.html)
   */
  @GetMapping(value="/login")
  public String login_form(Model model, HttpServletRequest request, HttpSession session) {
    // 쿠키 읽기
    Cookie[] cookies = request.getCookies();
    Cookie cookie = null;
  
    String ck_id = "";          // 저장된 아이디
    String ck_id_save = "";     // 아이디 저장 여부
    String ck_passwd = "";      // 저장된 비밀번호
    String ck_passwd_save = ""; // 비밀번호 저장 여부
  
    // 쿠키가 존재할 경우 반복하여 값 세팅
    if (cookies != null) {
      for (int i=0; i < cookies.length; i++){
        cookie = cookies[i];
        if (cookie.getName().equals("ck_id")){
          ck_id = cookie.getValue();
        } else if(cookie.getName().equals("ck_id_save")){
          ck_id_save = cookie.getValue();
        } else if (cookie.getName().equals("ck_passwd")){
          ck_passwd = cookie.getValue();
        } else if(cookie.getName().equals("ck_passwd_save")){
          ck_passwd_save = cookie.getValue();
        }
      }
    }
  
    // 쿠키 값을 로그인 폼으로 전달
    model.addAttribute("ck_id", ck_id);
    model.addAttribute("ck_id_save", ck_id_save);
    model.addAttribute("ck_passwd", ck_passwd);
    model.addAttribute("ck_passwd_save", ck_passwd_save);
  
    return "member/login_cookie";  // 로그인 폼 페이지
  }
  
  /**
   * Cookie 기반 로그인 처리
   * - 사용자가 입력한 ID/PW를 암호화하여 DB와 비교 후 로그인 성공 여부 판단
   * - 로그인 성공 시 세션에 회원 정보 저장 및 쿠키 옵션 처리 (아이디/비밀번호 저장)
   * - 로그인 실패 시 실패 메시지를 출력
   */
  @PostMapping(value="/login")
  public String login_proc(HttpSession session,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           Model model,
                           @RequestParam(value="id", defaultValue = "") String id,
                           @RequestParam(value="passwd", defaultValue = "") String passwd,
                           @RequestParam(value="id_save", defaultValue = "") String id_save,
                           @RequestParam(value="passwd_save", defaultValue = "") String passwd_save,
                           @RequestParam(value="url", defaultValue = "") String url) {
  
    // 1. 비밀번호 암호화
    String encrypted = security.aesEncode(passwd);
    HashMap<String, Object> map = new HashMap<>();
    map.put("id", id);
    map.put("passwd", encrypted);
  
    // 2. 로그인 기록 생성
    LoginVO loginVO = new LoginVO();
    loginVO.setId(id);
    loginVO.setIp(request.getRemoteAddr()); // 접속 IP
  
    // 3. 회원 인증
    MemberVO memberVO = this.memberProc.login(map);
  
    if (memberVO != null) {
      // 로그인 성공 시
      session.setAttribute("memberno", memberVO.getMemberno());
      session.setAttribute("id", memberVO.getId());
      session.setAttribute("mname", memberVO.getMname());
      session.setAttribute("grade", memberVO.getGrade()); // 숫자 등급 저장
  
      // 문자열 등급 (admin/supplier/user) 별도 저장
      if (memberVO.getGrade() >= 1 && memberVO.getGrade() <= 4) {
        session.setAttribute("gradeStr", "admin");
      } else if (memberVO.getGrade() >= 5 && memberVO.getGrade() <= 15) {
        session.setAttribute("gradeStr", "supplier");
      } else {
        session.setAttribute("gradeStr", "user");
      }
  
      // 로그인 성공 기록 DB 저장
      loginVO.setSw("Y");
      loginProc.create(loginVO);
  
      // 4. 쿠키 처리 (아이디 저장)
      Cookie ck_id = new Cookie("ck_id", id_save.equals("Y") ? id : "");
      ck_id.setPath("/");
      ck_id.setMaxAge(id_save.equals("Y") ? 60 * 60 * 24 * 30 : 0);
      response.addCookie(ck_id);
  
      // 쿠키 처리 (아이디 저장 여부)
      Cookie ck_id_save = new Cookie("ck_id_save", id_save);
      ck_id_save.setPath("/");
      ck_id_save.setMaxAge(60 * 60 * 24 * 30);
      response.addCookie(ck_id_save);
  
      // 쿠키 처리 (비밀번호 저장)
      Cookie ck_passwd = new Cookie("ck_passwd", passwd_save.equals("Y") ? passwd : "");
      ck_passwd.setPath("/");
      ck_passwd.setMaxAge(passwd_save.equals("Y") ? 60 * 60 * 24 * 30 : 0);
      response.addCookie(ck_passwd);
  
      // 쿠키 처리 (비밀번호 저장 여부)
      Cookie ck_passwd_save = new Cookie("ck_passwd_save", passwd_save);
      ck_passwd_save.setPath("/");
      ck_passwd_save.setMaxAge(60 * 60 * 24 * 30);
      response.addCookie(ck_passwd_save);
  
      // URL 리다이렉트 처리
      return (url.length() > 0) ? "redirect:" + url : "redirect:/";
  
    } else {
      // 로그인 실패 시
      loginVO.setSw("N");
      loginProc.create(loginVO);
  
      model.addAttribute("code", "login_fail");
      return "member/msg";
    }
  }
  
  /**
   * 로그인 필요 시 호출되는 로그인 폼
   * - 특정 URL 접근 시 인증이 필요할 경우 이 메서드로 유도됨
   * - 쿠키값을 읽어 로그인 폼에 전달
   */
  @GetMapping(value="/login_cookie_need")
  public String login_cookie_need(Model model, HttpServletRequest request,
                                  @RequestParam(name="url", defaultValue = "") String url) {
    Cookie[] cookies = request.getCookies();
    Cookie cookie = null;
  
    String ck_id = "";
    String ck_id_save = "";
    String ck_passwd = "";
    String ck_passwd_save = "";
  
    if (cookies != null) {
      for (int i=0; i < cookies.length; i++){
        cookie = cookies[i];
        if (cookie.getName().equals("ck_id")){
          ck_id = cookie.getValue();
        } else if(cookie.getName().equals("ck_id_save")){
          ck_id_save = cookie.getValue();
        } else if (cookie.getName().equals("ck_passwd")){
          ck_passwd = cookie.getValue();
        } else if(cookie.getName().equals("ck_passwd_save")){
          ck_passwd_save = cookie.getValue();
        }
      }
    }
  
    model.addAttribute("ck_id", ck_id);
    model.addAttribute("ck_id_save", ck_id_save);
    model.addAttribute("ck_passwd", ck_passwd);
    model.addAttribute("ck_passwd_save", ck_passwd_save);
    model.addAttribute("url", url);
  
    return "member/login_cookie_need";
  }
  // ----------------------------------------------------------------------------------
  // Cookie 사용 로그인 관련 코드 종료
  // ----------------------------------------------------------------------------------
  
  //====================== 관리자 전용 비밀번호 변경 ======================
  /**
   * 비밀번호 변경 폼 (관리자 전용)
   * - 관리자만 접근 가능, 비관리자는 비밀번호 찾기 페이지로 리다이렉트
   */
  @GetMapping(value="/passwd_update")
  public String passwd_update_form(HttpSession session, Model model) {
     String grade = (String) session.getAttribute("grade");
  
     if (grade != null && grade.equals("admin")) {
         int memberno = (int) session.getAttribute("memberno");
         MemberVO memberVO = this.memberProc.read(memberno);
         model.addAttribute("memberVO", memberVO);
         return "member/passwd_update";
     } else {
         return "redirect:/member/find_passwd";
     }
  }
  
  /**
   * 현재 비밀번호 확인 (AJAX, 관리자 전용)
   * - 비밀번호 변경 시 현재 비밀번호가 맞는지 검증
   */
  @PostMapping(value="/passwd_check")
  @ResponseBody
  public String passwd_check(HttpSession session, @RequestBody String json_src) {
     String grade = (String) session.getAttribute("grade");
     if (grade == null || !grade.equals("admin")) {
         return "{\"cnt\": 0}";
     }
  
     JSONObject src = new JSONObject(json_src);
     String current_passwd = src.getString("current_passwd");
  
     String encrypted = security.aesEncode(current_passwd);
     int memberno = (int)session.getAttribute("memberno");
  
     HashMap<String, Object> map = new HashMap<>();
     map.put("memberno", memberno);
     map.put("passwd", encrypted);
  
     int cnt = this.memberProc.passwd_check(map);
  
     JSONObject json = new JSONObject();
     json.put("cnt", cnt);
     return json.toString();
  }
  
  /**
   * 비밀번호 변경 처리 (관리자 전용)
   * - 현재 비밀번호 검증 후 새 비밀번호로 변경
   */
  @PostMapping(value="/passwd_update_proc")
  public String update_passwd_proc(HttpSession session, 
                                  Model model, 
                                  @RequestParam(value="current_passwd", defaultValue = "") String current_passwd, 
                                  @RequestParam(value="passwd", defaultValue = "") String passwd) {
     String grade = (String) session.getAttribute("grade");
     if (grade == null || !grade.equals("admin")) {
         return "redirect:/member/find_passwd";
     }
  
     int memberno = (int)session.getAttribute("memberno");
     String encryptedCurrent = security.aesEncode(current_passwd);
  
     HashMap<String, Object> map = new HashMap<>();
     map.put("memberno", memberno);
     map.put("passwd", encryptedCurrent);
  
     int cnt = this.memberProc.passwd_check(map);
  
     if (cnt == 0) { 
         model.addAttribute("code", "passwd_not_equal");
         model.addAttribute("cnt", 0);
     } else {
         map = new HashMap<>();
         String encryptedNew = security.aesEncode(passwd);
         map.put("memberno", memberno);
         map.put("passwd", encryptedNew);
  
         int passwd_change_cnt = this.memberProc.passwd_update(map);
  
         if (passwd_change_cnt == 1) {
             model.addAttribute("code", "passwd_change_success");
             model.addAttribute("cnt", 1);
         } else {
             model.addAttribute("code", "passwd_change_fail");
             model.addAttribute("cnt", 0);
         }
     }
  
     return "member/msg";
  }
  
  // ====================== 파일 미리보기 및 다운로드 ======================
  /**
   * 파일 미리보기
   * - 서버에 저장된 파일을 직접 브라우저에서 볼 수 있도록 반환
   */
  @GetMapping("/storage/{filename}")
  @ResponseBody
  public ResponseEntity<Resource> viewFile(@PathVariable("filename") String filename) {
      try {
          String filePath = "C:/kd/deploy/resort/member/storage/" + filename;
          Path path = Paths.get(filePath);
  
          if (!Files.exists(path)) {
              return ResponseEntity.notFound().build();
          }
  
          Resource resource = new UrlResource(path.toUri());
          String contentType = Files.probeContentType(path);
          if (contentType == null) {
              contentType = "application/octet-stream";
          }
  
          return ResponseEntity.ok()
                  .contentType(MediaType.parseMediaType(contentType))
                  .body(resource);
  
      } catch (Exception e) {
          e.printStackTrace();
          return ResponseEntity.internalServerError().build();
      }
  }
  
  /**
   * 파일 다운로드
   * - 서버에 저장된 파일을 다운로드 할 수 있도록 HTTP 응답 생성
   */
  @GetMapping("/download")
  public ResponseEntity<Resource> downloadFile(
          @RequestParam("filename") String filename,
          @RequestParam(value = "orgname", required = false) String orgname) {
      try {
          String filePath = "C:\\kd\\deploy\\resort\\member\\storage\\" + filename;
          Path path = Paths.get(filePath);
  
          if (!Files.exists(path)) {
              return ResponseEntity.notFound().build();
          }
  
          Resource resource = new UrlResource(path.toUri());
          String downloadName = (orgname != null && !orgname.isEmpty()) ? orgname : filename;
          String encodedFileName = URLEncoder.encode(downloadName, "UTF-8").replace("+", "%20");
  
          return ResponseEntity.ok()
                  .contentType(MediaType.APPLICATION_OCTET_STREAM)
                  .header(HttpHeaders.CONTENT_DISPOSITION,
                          "attachment; filename=\"" + encodedFileName + "\"")
                  .body(resource);
  
      } catch (Exception e) {
          e.printStackTrace();
          return ResponseEntity.internalServerError().build();
      }
  }
  
  //====================== 약관 페이지 출력 ======================
  /** 필수 약관: 만 14세 이상 동의 */
  @GetMapping("/terms/age")
  public String termsAge() {
     return "terms/age";  // templates/terms/age.html
  }
  
  /** 필수 약관: 이용약관 */
  @GetMapping("/terms/terms")
  public String termsTerms() {
     return "terms/terms";  // templates/terms/terms.html
  }
  
  /** 필수 약관: 개인정보 처리방침 */
  @GetMapping("/terms/privacy")
  public String termsPrivacy() {
     return "terms/privacy";  // templates/terms/privacy.html
  }
  
  /** 필수 약관: 개인정보 제3자 제공 동의 */
  @GetMapping("/terms/thirdparty")
  public String termsThirdparty() {
     return "terms/thirdparty";  // templates/terms/thirdparty.html
  }
  
  /** 선택 약관: 마케팅 정보 수신 동의 */
  @GetMapping("/terms/marketing")
  public String termsMarketing() {
     return "terms/marketing";  // templates/terms/marketing.html
  }
  
  /** 선택 약관: 광고성 정보 수신 */
  @GetMapping("/terms/ad")
  public String termsAd() {
     return "terms/ad";  // templates/terms/ad.html
  }
  
  /** 선택 약관: 이메일 수신 */
  @GetMapping("/terms/email")
  public String termsEmail() {
     return "terms/email";  // templates/terms/email.html
  }
  
  /** 선택 약관: SMS 수신 */
  @GetMapping("/terms/sms")
  public String termsSms() {
     return "terms/sms";  // templates/terms/sms.html
  }
  
  /** 선택 약관: 앱 푸시 수신 */
  @GetMapping("/terms/push")
  public String termsPush() {
     return "terms/push";  // templates/terms/push.html
  }

}


