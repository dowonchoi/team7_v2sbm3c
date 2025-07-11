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

import dev.mvc.cart.CartProcInter;
import dev.mvc.cate.CateProcInter;
import dev.mvc.cate.CateVOMenu;
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
import dev.mvc.tool.Security;
import dev.mvc.tool.Tool;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RequestMapping("/member")
@Controller
public class MemberCont {
  @Autowired
  @Qualifier("dev.mvc.member.MemberProc")  // @Service("dev.mvc.member.MemberProc")
  private MemberProcInter memberProc;
  
  @Autowired
  @Qualifier("dev.mvc.cate.CateProc")
  private CateProcInter cateProc;
  
  @Autowired
  @Qualifier("dev.mvc.products.ProductsProc")
  private ProductsProcInter productsProc;
  
  @Autowired
  @Qualifier("dev.mvc.login.LoginProc") 
  private LoginProcInter loginProc;
  
  @Autowired
  @Qualifier("dev.mvc.order.OrderProc") 
  private OrderProcInter orderProc;
  
  @Autowired
  @Qualifier("dev.mvc.order_item.OrderItemProc") 
  private OrderItemProcInter orderItemProc;

  @Autowired
  @Qualifier("dev.mvc.cart.CartProc") 
  private CartProcInter cartProc;

  @Autowired
  @Qualifier("dev.mvc.qna.QnaProc") 
  private QnaProcInter qnaProc;
  
  @Autowired  // 자동 주입 어노테이션 꼭 붙이기
  private MemberService memberService;
  
  @Autowired  // 자동 주입 어노테이션 꼭 붙이기
  private MailService mailService;
  
  @Autowired
  private Security security;
  
  public MemberCont() {
    System.out.println("-> MemberCont created.");  
  }
  
  @GetMapping("/check_id")
  @ResponseBody
  public Map<String, Object> checkId(@RequestParam("id") String id) {
      Map<String, Object> response = new HashMap<>();
      int count = memberProc.checkID(id);
      response.put("available", count == 0);
      return response;
  }
  
  /** 회원 가입 폼 */
  @GetMapping("/create")
  public String create_form(Model model) {
      List<CateVOMenu> menu = cateProc.menu();
      model.addAttribute("menu", menu);
      return "member/create";
  }

  /** 회원 가입 처리 */
  @PostMapping("/create")
  public String create_proc(Model model,
                            @ModelAttribute MemberVO memberVO,
                            @RequestParam(name = "userType", defaultValue = "user") String userType,
                            @RequestParam(name = "business_fileMF", required = false) MultipartFile businessFile) throws Exception {

      // ✅ 아이디 중복 확인
      if (memberProc.checkID(memberVO.getId()) > 0) {
          model.addAttribute("code", "duplicate_id");
          model.addAttribute("msg", "이미 사용 중인 아이디입니다.");
          return "member/msg";
      }

      // ✅ 등급 할당
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
      if (assignedGrade == -1) {
          model.addAttribute("code", "grade_limit");
          model.addAttribute("msg", "회원 수 초과");
          return "member/msg";
      }
      memberVO.setGrade(assignedGrade);

      // ✅ 공급자 파일 업로드
      if ("supplier".equals(userType)) {
          memberVO.setSupplier_approved("N");
          if (businessFile != null && !businessFile.isEmpty()) {
              String uploadDir = "C:/kd/deploy/resort/member/storage/";
              File dir = new File(uploadDir);
              if (!dir.exists()) dir.mkdirs();

              String origin = businessFile.getOriginalFilename();
              String ext = origin.substring(origin.lastIndexOf("."));
              String saveName = UUID.randomUUID().toString() + ext;
              File saveFile = new File(dir, saveName);
              businessFile.transferTo(saveFile);

              memberVO.setBusiness_file(saveName);
              memberVO.setBusiness_file_origin(origin);
          }
      }

      // ✅ 회원 가입 처리
      int cnt = memberProc.create(memberVO);

      model.addAttribute("code", (cnt == 1) ? "create_success" : "create_fail");
      model.addAttribute("cnt", cnt);
      model.addAttribute("mname", memberVO.getMname());
      model.addAttribute("id", memberVO.getId());

      return "member/msg";
  }

  /** 관리자 - 승인 대기 공급자 목록 */
  @GetMapping("/admin/pending_suppliers")
  public String pendingSuppliers(Model model) {
      List<MemberVO> list = memberProc.selectPendingSuppliers();
      model.addAttribute("supplierList", list);
      return "admin/pending_suppliers";
  }

  /** 관리자 - 공급자 승인 */
  @PostMapping("/admin/approveSupplier")
  @ResponseBody
  public String approveSupplier(@RequestParam("memberno") int memberno) {
      Map<String, Object> paramMap = new HashMap<>();
      paramMap.put("memberno", memberno);
      paramMap.put("supplier_approved", "Y");

      int cnt1 = memberProc.updateSupplierApproved(paramMap);
      int cnt2 = memberProc.updateGrade(memberno, 10);

      return (cnt1 == 1 && cnt2 == 1) ? "success" : "fail";
  }

  /** 관리자 - 공급자 거절 */
  @PostMapping("/admin/rejectSupplier")
  @ResponseBody
  public String rejectSupplier(@RequestParam("memberno") int memberno) {
      Map<String, Object> paramMap = new HashMap<>();
      paramMap.put("memberno", memberno);
      paramMap.put("supplier_approved", "R");

      int cnt1 = memberProc.updateSupplierApproved(paramMap);
      int cnt2 = memberProc.updateGrade(memberno, 16); // 소비자 등급으로 변경

      return (cnt1 == 1 && cnt2 == 1) ? "success" : "fail";
  }

  /** 관리자 - 승인 취소 (대기 상태로 전환) */
  @PostMapping("/admin/cancelApproval")
  @ResponseBody
  public String cancelApproval(@RequestParam("memberno") int memberno) {
      Map<String, Object> paramMap = new HashMap<>();
      paramMap.put("memberno", memberno);
      paramMap.put("supplier_approved", "N");

      int cnt1 = memberProc.updateSupplierApproved(paramMap);
      int cnt2 = memberProc.updateGrade(memberno, 5); // 대기 공급자 등급

      return (cnt1 == 1 && cnt2 == 1) ? "success" : "fail";
  }
  
  @GetMapping("/mypage")
  public String mypage(HttpSession session, Model model) {
      Integer memberno = (Integer) session.getAttribute("memberno");
      if (memberno == null) {
          return "redirect:/member/login";
      }

      MemberVO memberVO = memberProc.read(memberno);
      model.addAttribute("memberVO", memberVO);

      // 최근 주문 내역
      List<OrderVO> recentOrders = orderProc.getRecentOrders(memberno);
      model.addAttribute("recentOrders", recentOrders);
      
      // 주문별 주문 상세 목록 조회
      List<List<OrderItemVO>> allOrderItems = new ArrayList<>();
      for (OrderVO order : recentOrders) {
          int orderno = order.getOrderno();  // ✅ 주문번호 가져옴
          List<OrderItemVO> orderItems = orderItemProc.list_by_orderno(orderno);
          allOrderItems.add(orderItems);
      }
      model.addAttribute("allOrderItems", allOrderItems);

      // 최근 본 상품
      List<ProductsVO> recentViewedProducts = productsProc.getRecentlyViewed(memberno);
      model.addAttribute("recentViewedProducts", recentViewedProducts);

      // 요약 정보
      model.addAttribute("orderCount", orderProc.countOrders(memberno));
//      model.addAttribute("cancelCount", orderProc.countCancelledOrders(memberno));
      model.addAttribute("cartCount", cartProc.countItems(memberno));
//      model.addAttribute("couponCount", couponProc.countValidCoupons(memberno));
//      model.addAttribute("pointAmount", memberProc.getPoint(memberno));
//      model.addAttribute("reviewCount", reviewProc.countByMember(memberno));
      model.addAttribute("qnaCount", qnaProc.countByMember(memberno));
//      model.addAttribute("inquiryCount", inquiryProc.countByMember(memberno));

      return "/member/mypage";
  }


  @GetMapping("/list")
  public String list(
      @RequestParam(name = "word", defaultValue = "") String word,
      Model model, HttpSession session) {

      if (!memberProc.isAdmin(session)) {
          return "redirect:/member/login_cookie_need?url=/member/list";
      }

      List<MemberVO> list;
      if (word.trim().isEmpty()) {
          list = memberProc.list(); // 전체 목록
      } else {
          Map<String, Object> map = new HashMap<>();
          map.put("word", word);
          list = memberProc.list_search(map); // 검색 목록
      }

      model.addAttribute("list", list);
      model.addAttribute("word", word);

      return "member/list";
  }
  
  @GetMapping("/list_search")
  public String list_search(@RequestParam(name = "word", defaultValue = "") String word,
                            Model model, HttpSession session) {
      
      if (!memberProc.isAdmin(session)) {
          return "redirect:/member/login_cookie_need?url=/member/list_search";
      }

      List<MemberVO> list;
      if (word.trim().isEmpty()) {
          list = memberProc.list();
      } else {
          Map<String, Object> map = new HashMap<>();
          map.put("word", word);
          list = memberProc.list_search(map);
      }

      model.addAttribute("list", list);
      model.addAttribute("word", word);

      return "member/list_search";
  }

  
  @GetMapping("/list_search_paging")
  public String list_search_paging(HttpSession session, Model model,
                                   @RequestParam(name = "word", defaultValue = "") String word,
                                   @RequestParam(name = "now_page", defaultValue = "1") int now_page) {
      if (!memberProc.isAdmin(session)) {
          return "redirect:/member/login_cookie_need?url=/member/list_search_paging";
      }

      word = Tool.checkNull(word);

      int record_per_page = 10;
      int page_per_block = 5;

      List<MemberVO> list = memberProc.list_search_paging(word, now_page, record_per_page);
      model.addAttribute("list", list);

      int search_count = memberProc.list_search_count(word);
      String paging = memberProc.pagingBox(now_page, word, "/member/list_search_paging", search_count, record_per_page, page_per_block);
      model.addAttribute("paging", paging);

      int no = search_count - ((now_page - 1) * record_per_page);
      model.addAttribute("no", no);

      model.addAttribute("word", word);
      model.addAttribute("now_page", now_page);

      return "member/list_search_paging";
  }
    
  //회원 정보 수정 화면 이동
  @GetMapping("/update")
  public String updateForm(HttpSession session, Model model) {
     Integer memberno = (Integer) session.getAttribute("memberno");
     if (memberno == null) {
         return "redirect:/member/login";  // 로그인 안 했으면 로그인 페이지로
     }
  
     MemberVO memberVO = memberProc.read(memberno);  // DB에서 회원 정보 조회
     model.addAttribute("memberVO", memberVO);       // 수정 폼에 정보 전달
  
     return "/member/update";  // templates/member/update.html
  }
  
  //회원 정보 수정 처리
  @PostMapping("/update")
  public String updateProc(MemberVO memberVO, RedirectAttributes ra) {
     if (memberVO.getPasswd() != null && !memberVO.getPasswd().isEmpty()) {
         String encrypted = security.aesEncode(memberVO.getPasswd());
         memberVO.setPasswd(encrypted);
     }
  
     int cnt = memberProc.update(memberVO);
     if (cnt == 1) {
         ra.addFlashAttribute("msg", "회원 정보가 수정되었습니다.");
     } else {
         ra.addFlashAttribute("msg", "회원 정보 수정에 실패했습니다.");
     }
  
     return "redirect:/member/mypage";
  }
  
  //아이디 찾기 폼
  @GetMapping("/find_id")
  public String findIdForm() {
      return "/member/find_id";  // 입력 폼 화면
  }
  
  //아이디 찾기 처리
  @PostMapping("/find_id")
  public String findIdProc(@RequestParam("mname") String mname,
                           @RequestParam("tel") String tel,
                           Model model) {

      MemberVO memberVO = memberProc.findIdByNameAndTel(mname, tel);  // 메서드 구현 필요

      if (memberVO != null) {
          model.addAttribute("foundId", memberVO.getId());
      } else {
          model.addAttribute("notFound", true);
      }

      return "/member/find_id_result";  // 결과 출력 페이지
  } 

  
  //비밀번호 찾기 폼
  @GetMapping("/find_passwd")
  public String findPasswdForm() {
      return "member/find_passwd";  // 확장자 .html 생략, 정상
  }
  
//비밀번호 찾기 처리
  @PostMapping("/find_passwd")
  public String findPasswdProc(@RequestParam("id") String id, Model model) {
      MemberVO member = memberProc.readById(id);

      // 아이디 존재 여부 체크
      if (member == null) {
          model.addAttribute("msg", "존재하지 않는 아이디입니다.");
          return "member/find_passwd_fail";
      }

      // 이메일 형식인지 체크
      if (!isValidEmail(id)) {
          model.addAttribute("msg", "이메일 형식의 아이디만 비밀번호 재설정이 가능합니다.");
          return "member/find_passwd_fail";
      }

      // 정상 이메일인 경우 메일 발송
      String resetLink = "http://localhost:9093/member/reset_passwd_form?id=" + id;

      String subject = "[떨이몰] 비밀번호 재설정 링크입니다.";
      String content = "<p>안녕하세요.</p>"
                     + "<p>비밀번호 재설정을 원하신다면 아래 링크를 클릭해주세요.</p>"
                     + "<a href='" + resetLink + "'>비밀번호 재설정하기</a>";

      mailService.sendMail(id, subject, content);

      model.addAttribute("msg", "비밀번호 재설정 링크를 전송했습니다.");
      return "member/find_passwd_result";
  }
  
  //🔥 이메일 형식 체크 메서드
  public boolean isValidEmail(String email) {
     String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
     return email != null && email.matches(emailRegex);
  }
  
  //이메일 중복 확인
  @ResponseBody
  @GetMapping("/check_email")
  public Map<String, Object> checkEmail(@RequestParam("email") String email) {
     boolean available = memberProc.checkEmail(email) == 0;
     Map<String, Object> response = new HashMap<>();
     response.put("available", available);
     return response;
  }

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
      return "member/reset_passwd_form";
  }
  
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

      String aesPasswd = security.aesEncode(passwd);
      memberProc.updatePasswdById(email, aesPasswd);

      // ✔️ 인증 정보 세션에서 삭제
      session.removeAttribute("resetCode:" + email);

      ra.addFlashAttribute("msg", "비밀번호가 성공적으로 변경되었습니다.");
      return "redirect:/member/reset_msg";
  }
  
  @GetMapping("/reset_msg")
  public String resetMsg() {
      return "member/reset_msg";
  }
  
  // 테스트용 고객 메일 전송
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
  
  // 회원가입시 인증
  @PostMapping("/send_code")
  @ResponseBody
  public String sendCode(@RequestParam("email") String email, HttpSession session) {
    // ✅ 이메일 유효성 검사 정규식 수정 (도메인 끝자리가 6자 이상인 경우도 허용)
    if (!email.matches("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
      return "invalid_email";
    }

    String code = String.format("%06d", new Random().nextInt(999999));
    long expire = System.currentTimeMillis() + 1 * 60 * 1000; // 3분

    session.setAttribute("authInfo:" + email, new AuthInfo(code, expire));
    boolean sent = mailService.sendVerificationMail(email, code);

    return sent ? "success" : "fail";
  }

  
  @PostMapping("/verify_code")
  @ResponseBody
  public String verifyCode(@RequestParam("email") String email, @RequestParam("code") String userCode, HttpSession session) {
    AuthInfo info = (AuthInfo) session.getAttribute("authInfo:" + email);
    if (info == null) return "no_code";
    if (info.isExpired()) return "expired";
    if (info.getAttemptCount() >= 5) return "too_many_attempts";

    info.setAttemptCount(info.getAttemptCount() + 1);
    if (info.getCode().equals(userCode)) {
      session.setAttribute("verified:" + email, true);  // ✅ 인증 완료 여부 저장
      return "verified";
    }
    return "invalid";
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

  /**
  * 조회
  * @param model
  * @param memberno 회원 번호
  * @return 회원 정보
  */
  @GetMapping("/read")
  public String read(HttpSession session, Model model,
                     @RequestParam(name = "memberno") int memberno) {

      String grade = (String) session.getAttribute("grade");
      String sessionMembernoStr = String.valueOf(session.getAttribute("memberno"));
      int sessionMemberno = (sessionMembernoStr != null && !sessionMembernoStr.equals("null")) 
                            ? Integer.parseInt(sessionMembernoStr) : -1;

      // 비로그인 상태
      if (grade == null) {
          return "redirect:/member/login_cookie_need";
      }

      // 관리자는 모든 회원 조회 가능
      if ("admin".equals(grade)) {
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

  //🔥 회원 탈퇴 (비밀번호 확인 후 탈퇴 처리)
  @PostMapping(value = "/delete", produces = "text/plain; charset=UTF-8")
  @ResponseBody
  public String delete_ajax(@RequestParam("memberno") int memberno,
                            @RequestParam("passwd") String passwd,
                            HttpSession session) {
      try {
          MemberVO memberVO = memberProc.read(memberno);

          if (memberVO == null) {
              System.out.println("회원 정보 없음");
              return "error";
          }

          String encryptedInput = security.aesEncode(passwd);
          String dbPasswd = memberVO.getPasswd();

          System.out.println("입력 암호화: " + encryptedInput);
          System.out.println("DB 암호화: " + dbPasswd);

          if (dbPasswd != null && dbPasswd.equals(encryptedInput)) {
              int cnt = memberProc.withdraw(memberno);

              if (cnt == 1) {
                  session.invalidate();
                  return "success";
              } else {
                  System.out.println("탈퇴 처리 실패 cnt: " + cnt);
                  return "error";
              }
          } else {
              System.out.println("비밀번호 불일치");
              return "fail";
          }
      } catch (Exception e) {
          e.printStackTrace();
          return "error";
      }
  }
  
  //🔥 관리자 전용 회원 완전 삭제
  @PostMapping(value = "/delete_by_admin", produces = "text/plain; charset=UTF-8")
  @ResponseBody
  public String deleteByAdmin(@RequestParam("memberno") int memberno, HttpSession session) {
      Object gradeObj = session.getAttribute("grade");
      if (gradeObj == null || !"admin".equals(gradeObj.toString())) {
          return "unauthorized";
      }

      int cnt = this.memberProc.delete(memberno);  // 완전 삭제
      return cnt == 1 ? "success" : "fail";
  }

    //🔥 탈퇴 비밀번호 확인 폼
  @GetMapping("/delete")
  public String deleteForm(HttpSession session, Model model) {
     Integer memberno = (Integer) session.getAttribute("memberno");
     if (memberno == null) {
         return "redirect:/member/login";
     }
  
     MemberVO memberVO = memberProc.read(memberno);
     model.addAttribute("memberVO", memberVO);
  
     return "member/delete";
  }
  
  //🔥 회원 탈퇴 처리 (form 방식)
  @PostMapping("/delete")
  public String deleteProc(HttpSession session,
                           @RequestParam("memberno") int memberno,
                           @RequestParam("passwd") String passwd,
                           Model model) {
      MemberVO memberVO = memberProc.read(memberno);
      String encrypted = security.aesEncode(passwd);

      if (memberVO != null && memberVO.getPasswd().equals(encrypted)) {
          int cnt = memberProc.withdraw(memberno); // 🔥 등급 변경 처리
          if (cnt == 1) {
              session.invalidate();
              model.addAttribute("msg", "회원 탈퇴가 완료되었습니다.");
          } else {
              model.addAttribute("msg", "회원 탈퇴 처리 중 오류 발생");
          }
      } else {
          model.addAttribute("msg", "비밀번호가 일치하지 않습니다.");
      }

      return "member/delete_msg";
  }

  
  //🔥 탈퇴 완료 메시지
  @GetMapping("/delete_msg")
  public String deleteMsg() {
     return "member/delete_msg";
  }
  
  //🔥 탈퇴 회원 목록 조회 (관리자 전용)
  @GetMapping("/withdrawn_list")
  public String withdrawnList(Model model, HttpSession session) {
      String grade = (String) session.getAttribute("grade");
      System.out.println("현재 세션 grade: " + grade);

      if (grade == null || !grade.equals("admin")) {
          return "redirect:/member/login_cookie_need?url=/member/withdrawn_list";
      }

      List<MemberVO> list = memberProc.selectWithdrawnMembers();
      model.addAttribute("list", list);

      return "member/withdrawn_list";
  }
  
  //복구 처리
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

      return "redirect:/member/withdrawn_list";
  }
  
  // 회원 등급 설명
  public boolean isAdmin(HttpSession session) {
    Integer grade = (Integer) session.getAttribute("grade");
    return grade != null && (grade >= 1 && grade <= 4);
  }
  
  public boolean isSupplier(HttpSession session) {
      Integer grade = (Integer) session.getAttribute("grade");
      return grade != null && (grade >= 5 && grade <= 15);
  }
  
  public boolean isUser(HttpSession session) {
      Integer grade = (Integer) session.getAttribute("grade");
      return grade != null && (grade >= 16 && grade <= 39);
  }
  
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
  
  /**
   * 로그아웃
   * @param model
   * @param memberno 회원 번호
   * @return 회원 정보
   */
  @GetMapping(value="/logout")
  public String logout(HttpSession session, Model model) {
    session.invalidate();  // 모든 세션 변수 삭제
    return "redirect:/";
  }
  
  // ----------------------------------------------------------------------------------
  // Cookie 사용 로그인 관련 코드 시작
  // ----------------------------------------------------------------------------------
  /**
   * 로그인
   * @param model
   * @param memberno 회원 번호
   * @return 회원 정보
   */
  @GetMapping(value="/login")
  public String login_form(Model model, HttpServletRequest request, HttpSession session) {
    // System.out.println("-> 시스템 session.id: " + session.getId());
    
    // Cookie 관련 코드---------------------------------------------------------
    Cookie[] cookies = request.getCookies();
    Cookie cookie = null;
  
    String ck_id = ""; // id 저장
    String ck_id_save = ""; // id 저장 여부를 체크
    String ck_passwd = ""; // passwd 저장
    String ck_passwd_save = ""; // passwd 저장 여부를 체크
  
    if (cookies != null) { // 쿠키가 존재한다면
      for (int i=0; i < cookies.length; i++){
        cookie = cookies[i]; // 쿠키 객체 추출
      
        if (cookie.getName().equals("ck_id")){
          ck_id = cookie.getValue();  // email
        }else if(cookie.getName().equals("ck_id_save")){
          ck_id_save = cookie.getValue();  // Y, N
        }else if (cookie.getName().equals("ck_passwd")){
          ck_passwd = cookie.getValue();         // 1234
        }else if(cookie.getName().equals("ck_passwd_save")){
          ck_passwd_save = cookie.getValue();  // Y, N
        }
      }
    }
    // ----------------------------------------------------------------------------
    
    //    <input type='text' class="form-control" name='id' id='id' 
    //            th:value='${ck_id }' required="required" 
    //            style='width: 30%;' placeholder="아이디" autofocus="autofocus">
    model.addAttribute("ck_id", ck_id);
  
    //    <input type='checkbox' name='id_save' value='Y' 
    //            th:checked="${ck_id_save == 'Y'}"> 저장
    model.addAttribute("ck_id_save", ck_id_save);
  
    model.addAttribute("ck_passwd", ck_passwd);
    model.addAttribute("ck_passwd_save", ck_passwd_save);

//    model.addAttribute("ck_id_save", "Y");
//    model.addAttribute("ck_passwd_save", "Y");
    
    return "member/login_cookie";  // templates/member/login_cookie.html
  }

  /**
   * Cookie 기반 로그인 처리
   * @param session
   * @param request
   * @param response
   * @param model
   * @param id 아이디
   * @param passwd 패스워드
   * @param id_save 아이디 저장 여부
   * @param passwd_save 패스워드 저장 여부
   * @return
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

    System.out.println("입력한 평문 비밀번호: " + passwd);
    String encrypted = security.aesEncode(passwd);
    System.out.println("암호화된 로그인 비밀번호: " + encrypted);

    HashMap<String, Object> map = new HashMap<>();
    map.put("id", id);
    map.put("passwd", encrypted);
    
    // 로그인 기록 객체 생성
    LoginVO loginVO = new LoginVO();
    loginVO.setId(id);
    loginVO.setIp(request.getRemoteAddr()); // 접속 IP

    MemberVO memberVO = this.memberProc.login(map);

    if (memberVO != null) {
      session.setAttribute("memberno", memberVO.getMemberno());
      session.setAttribute("id", memberVO.getId());
      session.setAttribute("mname", memberVO.getMname());
      session.setAttribute("grade", memberVO.getGrade());

      if (memberVO.getGrade() >= 1 && memberVO.getGrade() <= 4) {
        session.setAttribute("grade", "admin");
      } else if (memberVO.getGrade() >= 5 && memberVO.getGrade() <= 15) {
        session.setAttribute("grade", "supplier");
      } else {
        session.setAttribute("grade", "user");
      }

      System.out.println("-> grade: " + session.getAttribute("grade"));
      
      // 로그인 성공 기록
      loginVO.setSw("Y");
      loginProc.create(loginVO);

      if (id_save.equals("Y")) {
        Cookie ck_id = new Cookie("ck_id", id);
        ck_id.setPath("/");
        ck_id.setMaxAge(60 * 60 * 24 * 30);
        response.addCookie(ck_id);
      } else {
        Cookie ck_id = new Cookie("ck_id", "");
        ck_id.setPath("/");
        ck_id.setMaxAge(0);
        response.addCookie(ck_id);
      }

      Cookie ck_id_save = new Cookie("ck_id_save", id_save);
      ck_id_save.setPath("/");
      ck_id_save.setMaxAge(60 * 60 * 24 * 30);
      response.addCookie(ck_id_save);

      if (passwd_save.equals("Y")) {
        Cookie ck_passwd = new Cookie("ck_passwd", passwd);
        ck_passwd.setPath("/");
        ck_passwd.setMaxAge(60 * 60 * 24 * 30);
        response.addCookie(ck_passwd);
      } else {
        Cookie ck_passwd = new Cookie("ck_passwd", "");
        ck_passwd.setPath("/");
        ck_passwd.setMaxAge(0);
        response.addCookie(ck_passwd);
      }

      Cookie ck_passwd_save = new Cookie("ck_passwd_save", passwd_save);
      ck_passwd_save.setPath("/");
      ck_passwd_save.setMaxAge(60 * 60 * 24 * 30);
      response.addCookie(ck_passwd_save);

      if (url.length() > 0) {
        return "redirect:" + url;
      } else {
        return "redirect:/";
      }

    } else {
      // 로그인 실패 기록
      loginVO.setSw("N");
      loginProc.create(loginVO);

      model.addAttribute("code", "login_fail");
      return "member/msg";
    }
  }
  
  // ----------------------------------------------------------------------------------
  // Cookie 사용 로그인 관련 코드 종료
  // ----------------------------------------------------------------------------------

  /**
   * 패스워드 수정 폼
   * @param model
   * @param memberno
   * @return
   */
//🔥 패스워드 수정 폼 (관리자만)
@GetMapping(value="/passwd_update")
public String passwd_update_form(HttpSession session, Model model) {
   String grade = (String) session.getAttribute("grade");

   if (grade != null && grade.equals("admin")) {
       int memberno = (int) session.getAttribute("memberno");
       MemberVO memberVO = this.memberProc.read(memberno);
       model.addAttribute("memberVO", memberVO);
       return "member/passwd_update";
   } else {
       return "redirect:/member/find_passwd"; // 소비자/공급자는 비밀번호 찾기로 유도
   }
}

//🔥 현재 패스워드 확인 (관리자만)
@PostMapping(value="/passwd_check")
@ResponseBody
public String passwd_check(HttpSession session, @RequestBody String json_src) {
   String grade = (String) session.getAttribute("grade");
   if (grade == null || !grade.equals("admin")) {
       return "{\"cnt\": 0}"; // 관리자 외 접근 불가
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

//🔥 패스워드 변경 처리 (관리자만)
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
  
  /**
   * 로그인 요구에 따른 로그인 폼 출력 
   * @param model
   * @param memberno 회원 번호
   * @return 회원 정보
   */
  @GetMapping(value="/login_cookie_need") 
  public String login_cookie_need(Model model, HttpServletRequest request,
                                              @RequestParam(name="url", defaultValue = "") String url) {
    // Cookie 관련 코드---------------------------------------------------------
    Cookie[] cookies = request.getCookies();
    Cookie cookie = null;
  
    String ck_id = ""; // id 저장
    String ck_id_save = ""; // id 저장 여부를 체크
    String ck_passwd = ""; // passwd 저장
    String ck_passwd_save = ""; // passwd 저장 여부를 체크
  
    if (cookies != null) { // 쿠키가 존재한다면
      for (int i=0; i < cookies.length; i++){
        cookie = cookies[i]; // 쿠키 객체 추출
      
        if (cookie.getName().equals("ck_id")){
          ck_id = cookie.getValue();  // email
        }else if(cookie.getName().equals("ck_id_save")){
          ck_id_save = cookie.getValue();  // Y, N
        }else if (cookie.getName().equals("ck_passwd")){
          ck_passwd = cookie.getValue();         // 1234
        }else if(cookie.getName().equals("ck_passwd_save")){
          ck_passwd_save = cookie.getValue();  // Y, N
        }
      }
    }
    // ----------------------------------------------------------------------------
    
    //    <input type='text' class="form-control" name='id' id='id' 
    //            th:value='${ck_id }' required="required" 
    //            style='width: 30%;' placeholder="아이디" autofocus="autofocus">
    model.addAttribute("ck_id", ck_id);
  
    //    <input type='checkbox' name='id_save' value='Y' 
    //            th:checked="${ck_id_save == 'Y'}"> 저장
    model.addAttribute("ck_id_save", ck_id_save);
  
    model.addAttribute("ck_passwd", ck_passwd);
    model.addAttribute("ck_passwd_save", ck_passwd_save);

//    model.addAttribute("ck_id_save", "Y");
//    model.addAttribute("ck_passwd_save", "Y");
    
    model.addAttribute("url", url);
    
    return "member/login_cookie_need";  // templates/member/login_cookie_need.html
  }

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
  
  // 여기서부터 전부 약관 동의
  @GetMapping("/terms/age")
  public String termsAge() {
     return "terms/age";  // templates/terms/age.html
  }
  
  @GetMapping("/terms/terms")
  public String termsTerms() {
     return "terms/terms";  // templates/terms/terms.html
  }
  
//  @GetMapping("/terms/finance")
//  public String termsFinance() {
//     return "terms/finance";  // templates/terms/finance.html
//  }
  
  @GetMapping("/terms/privacy")
  public String termsPrivacy() {
     return "terms/privacy";  // templates/terms/privacy.html
  }
  
  @GetMapping("/terms/thirdparty")
  public String termsThirdparty() {
     return "terms/thirdparty";  // templates/terms/thirdparty.html
  }
  
  @GetMapping("/terms/marketing")
  public String termsMarketing() {
     return "terms/marketing";  // templates/terms/marketing.html
  }
  
  @GetMapping("/terms/ad")
  public String termsAd() {
     return "terms/ad";  // templates/terms/ad.html
  }
  
  @GetMapping("/terms/email")
  public String termsEmail() {
     return "terms/email";  // templates/terms/email.html
  }
  
  @GetMapping("/terms/sms")
  public String termsSms() {
     return "terms/sms";  // templates/terms/sms.html
  }
  
  @GetMapping("/terms/push")
  public String termsPush() {
     return "terms/push";  // templates/terms/push.html
  }

  
  

}


