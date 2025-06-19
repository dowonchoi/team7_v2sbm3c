package dev.mvc.member;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import dev.mvc.cate.CateProcInter;
import dev.mvc.cate.CateVOMenu;
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
  
  @Autowired  // 자동 주입 어노테이션 꼭 붙이기
  private MemberService memberService;
  
  public MemberCont() {
    System.out.println("-> MemberCont created.");  
  }
  
  @GetMapping(value="/checkID") // http://localhost:9091/member/checkID?id=admin
  @ResponseBody
  public String checkID(@RequestParam(name="id", defaultValue = "") String id) {    
    System.out.println("-> id: " + id);
    int cnt = this.memberProc.checkID(id);
    
    // return "cnt: " + cnt;
    // return "{\"cnt\": " + cnt + "}";    // {"cnt": 1} JSON
    
    JSONObject obj = new JSONObject();
    obj.put("cnt", cnt);
    
    return obj.toString();
  }
  
  /** 회원 가입 폼 */
  @GetMapping(value="/create")
  public String create_form(Model model, @ModelAttribute("memberVO") MemberVO memberVO) {
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);
    
    return "member/create";    // /templates/member/create.html
  }
  
  /** 회원 가입 처리 */
  @PostMapping("/create")
  public String create_proc(Model model,
                            @ModelAttribute MemberVO memberVO,
                            @RequestParam(name = "userType", defaultValue = "user") String userType,
                            HttpServletRequest request) throws Exception {

      int gradeStart, gradeEnd;
      if ("supplier".equals(userType)) {
          gradeStart = 5;
          gradeEnd = 15;
      } else {
          gradeStart = 16;
          gradeEnd = 39;
      }

      // 사용 중인 등급 목록 조회
      List<Integer> usedGrades = memberProc.getUsedGradesInRange(gradeStart, gradeEnd);

      // 가능한 미사용 등급 찾기
      int assignedGrade = -1;
      for (int i = gradeStart; i <= gradeEnd; i++) {
          if (!usedGrades.contains(i)) {
              assignedGrade = i;
              break;
          }
      }

      if (assignedGrade == -1) {
          // 가입 가능한 등급 없음 → 가입 실패 메시지 처리
          model.addAttribute("code", "grade_limit_reached");
          model.addAttribute("msg", "해당 유형의 회원 수가 최대치에 도달했습니다. 가입이 불가합니다.");
          return "member/msg";
      }

      // 공급자일 경우 사업자 인증 처리
      if ("supplier".equals(userType)) {
          memberVO.setGrade(assignedGrade);
          memberVO.setSupplier_approved("N"); // 기본은 미승인

          MultipartFile businessFile = memberVO.getBusiness_file();
          if (businessFile != null && !businessFile.isEmpty()) {
              String uploadDir = "C:\\kd\\deploy\\resort\\member\\storage";
              File uploadDirFile = new File(uploadDir);
              if (!uploadDirFile.exists()) uploadDirFile.mkdirs();

              String originalFilename = businessFile.getOriginalFilename();
              String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
              String saveFilename = java.util.UUID.randomUUID().toString() + ext;

              File saveFile = new File(uploadDirFile, saveFilename);
              businessFile.transferTo(saveFile);

              memberVO.setBusiness_file_name(saveFilename); // DB에 저장
          }

      } else {
          // 일반 소비자
          memberVO.setGrade(assignedGrade);
      }

      int cnt = memberProc.create(memberVO);

      model.addAttribute("code", (cnt == 1) ? "create_success" : "create_fail");
      model.addAttribute("cnt", cnt);
      model.addAttribute("mname", memberVO.getMname());
      model.addAttribute("id", memberVO.getId());

      return "member/msg";
  }

  
  @GetMapping("/admin/pending_suppliers")
  public String pendingSuppliers(Model model) {
      List<MemberVO> list = memberProc.selectPendingSuppliers();
      model.addAttribute("supplierList", list); // 또는 "list"
      return "admin/pending_suppliers"; // templates/admin/pending_suppliers.html 로 이동
  }
  
  @PostMapping("/admin/approveSupplier")
  @ResponseBody
  public String approveSupplier(@RequestParam("memberno") int memberno) {
      Map<String, Object> paramMap = new HashMap<>();
      paramMap.put("memberno", memberno);
      paramMap.put("supplier_approved", "Y");  // 승인됨

      int result = memberProc.updateSupplierApproved(paramMap);
      return result == 1 ? "success" : "fail";
  }

  @PostMapping("/admin/rejectSupplier")
  @ResponseBody
  public String rejectSupplier(@RequestParam("memberno") int memberno) {
      Map<String, Object> paramMap = new HashMap<>();
      paramMap.put("memberno", memberno);
      paramMap.put("supplier_approved", "R");  // 거절

      int result = memberProc.updateSupplierApproved(paramMap);
      return result == 1 ? "success" : "fail";
  }


  @PostMapping("/admin/cancelApproval")
  @ResponseBody
  public String cancelApproval(@RequestParam("memberno") int memberno) {
      Map<String, Object> paramMap = new HashMap<>();
      paramMap.put("memberno", memberno);
      paramMap.put("supplier_approved", "N");  // 대기중으로 되돌림

      int result = memberProc.updateSupplierApproved(paramMap);
      return result == 1 ? "success" : "fail";
  }

  
  @PostMapping(value="/delete", produces = "text/plain; charset=UTF-8")
  @ResponseBody
  public String delete_ajax(@RequestParam("memberno") int memberno) {
      int cnt = this.memberProc.delete(memberno);
      return cnt == 1 ? "success" : "fail";
  }

  
  @GetMapping(value="/list")
  public String list(HttpSession session, Model model) {
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);
    
    if (this.memberProc.isAdmin(session)) {
      ArrayList<MemberVO> list = this.memberProc.list();
      model.addAttribute("list", list);
      return "member/list";
    } else {
      return "redirect:/member/login_cookie_need?url=/member/list";
    }
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
  @GetMapping(value="/read")
  public String read(HttpSession session, Model model,
                     @RequestParam(name="memberno", defaultValue = "") int memberno) {
    String grade = (String)session.getAttribute("grade"); // admin, member, guest
    
    if (grade == null) {
      return "redirect:/member/login_cookie_need";
    }
    
    if (grade.equals("member") && memberno == (int)session.getAttribute("memberno")) {
      MemberVO memberVO = this.memberProc.read(memberno);
      model.addAttribute("memberVO", memberVO);
      return "member/read";
    } else if (grade.equals("admin")) {
      MemberVO memberVO = this.memberProc.read(memberno);
      model.addAttribute("memberVO", memberVO);
      return "member/read";
    } else {
      return "redirect:/member/login_cookie_need";
    }
  }
 
  
  /**
   * 수정 처리
   * @param model
   * @param memberVO
   * @return
   */
  @PostMapping(value="/update")
  public String update_proc(Model model, 
                                       @ModelAttribute("memberVO") MemberVO memberVO) {
    int cnt = this.memberProc.update(memberVO); // 수정
    
    if (cnt == 1) {
      model.addAttribute("code", "update_success");
      model.addAttribute("mname", memberVO.getMname());
      model.addAttribute("id", memberVO.getId());
    } else {
      model.addAttribute("code", "update_fail");
    }
    
    model.addAttribute("cnt", cnt);
    
    return "member/msg"; // /templates/member/msg.html
  }

  /**
   * 삭제
   * @param model
   * @param memberno 회원 번호
   * @return 회원 정보
   */
  @GetMapping(value="/delete")
  public String delete(Model model,
                              @RequestParam(name="memberno", defaultValue = "") Integer memberno) {
    System.out.println("-> delete memberno: " + memberno);
    
    MemberVO memberVO = this.memberProc.read(memberno);
    model.addAttribute("memberVO", memberVO);
    
    return "member/delete";  // templates/member/delete.html
  }
  
  /**
   * 회원 Delete process
   * @param model
   * @param memberno 삭제할 레코드 번호
   * @return
   */
  @PostMapping(value="/delete")
  public String delete_process(Model model, 
                                          @RequestParam(name="memberno", defaultValue = "") Integer memberno) {
    int cnt = this.memberProc.delete(memberno);
    
    if (cnt == 1) {
      return "redirect:/member/list"; // @GetMapping(value="/list")
    } else {
      model.addAttribute("code", "delete_fail");
      return "member/msg"; // /templates/member/msg.html
    }
  }

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
                                     @RequestParam(value="url", defaultValue = "") String url
                                     ) {
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("id", id);
    map.put("passwd", passwd);
    
    int cnt = this.memberProc.login(map);
    // System.out.println("-> login_proc cnt: " + cnt);
    
    model.addAttribute("cnt", cnt);
    
    if (cnt == 1) {
      // id를 이용하여 회원 정보 조회
      MemberVO memverVO = this.memberProc.readById(id);
      session.setAttribute("memberno", memverVO.getMemberno());
      session.setAttribute("id", memverVO.getId()); // 시스템 변수와 중복됨, 권장하지 않음.
      session.setAttribute("mname", memverVO.getMname());
      session.setAttribute("grade", memverVO.getGrade());

      // -------------------------------------------------------------------
      // 회원 등급 처리
      // -------------------------------------------------------------------
      if (memverVO.getGrade() >= 1 && memverVO.getGrade() <=4) {
        session.setAttribute("grade", "admin");
      } else if (memverVO.getGrade() >= 5 && memverVO.getGrade() <= 15) {
        session.setAttribute("grade", "supplier");
      } else if (memverVO.getGrade() >= 16) {
        session.setAttribute("grade", "user");
      }
      
      System.out.println("-> grade: " + session.getAttribute("grade"));
      
      // Cookie 관련 코드---------------------------------------------------------
      // -------------------------------------------------------------------
      // id 관련 쿠기 저장
      // -------------------------------------------------------------------
      if (id_save.equals("Y")) { // id를 저장할 경우, Checkbox를 체크한 경우
        Cookie ck_id = new Cookie("ck_id", id);
        ck_id.setPath("/");  // root 폴더에 쿠키를 기록함으로 모든 경로에서 쿠기 접근 가능
        ck_id.setMaxAge(60 * 60 * 24 * 30); // 30 day, 초단위
        response.addCookie(ck_id); // id 저장
      } else { // N, id를 저장하지 않는 경우, Checkbox를 체크 해제한 경우
        Cookie ck_id = new Cookie("ck_id", "");
        ck_id.setPath("/");
        ck_id.setMaxAge(0);
        response.addCookie(ck_id); // id 저장
      }
      
      // id를 저장할지 선택하는  CheckBox 체크 여부
      Cookie ck_id_save = new Cookie("ck_id_save", id_save);
      ck_id_save.setPath("/");
      ck_id_save.setMaxAge(60 * 60 * 24 * 30); // 30 day
      response.addCookie(ck_id_save);
      // -------------------------------------------------------------------
  
      // -------------------------------------------------------------------
      // Password 관련 쿠기 저장
      // -------------------------------------------------------------------
      if (passwd_save.equals("Y")) { // 패스워드 저장할 경우
        Cookie ck_passwd = new Cookie("ck_passwd", passwd);
        ck_passwd.setPath("/");
        ck_passwd.setMaxAge(60 * 60 * 24 * 30); // 30 day
        response.addCookie(ck_passwd);
      } else { // N, 패스워드를 저장하지 않을 경우
        Cookie ck_passwd = new Cookie("ck_passwd", "");
        ck_passwd.setPath("/");
        ck_passwd.setMaxAge(0);
        response.addCookie(ck_passwd);
      }
      // passwd를 저장할지 선택하는  CheckBox 체크 여부
      Cookie ck_passwd_save = new Cookie("ck_passwd_save", passwd_save);
      ck_passwd_save.setPath("/");
      ck_passwd_save.setMaxAge(60 * 60 * 24 * 30); // 30 day
      response.addCookie(ck_passwd_save);
      // -------------------------------------------------------------------
      // ----------------------------------------------------------------------------     
      
      if (url.length() > 0) { // 접속 요청이 있었는지 확인
        return "redirect:" + url;  // redirect:/member/login_cookie_need?url=/cate/list_search
      } else {
        return "redirect:/";
      }
    } else {
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
  @GetMapping(value="/passwd_update")
  public String passwd_update_form(HttpSession session, Model model) {
    if (this.memberProc.isMember(session)) {
      int memberno = (int)session.getAttribute("memberno"); // session에서 가져오기
      
      MemberVO memberVO = this.memberProc.read(memberno);      
      model.addAttribute("memberVO", memberVO);
      
      return "member/passwd_update";
      
    } else {
      return "redirect:/member/login_cookie_need"; // redirect      
    }
  }
  
  /**
   * 현재 패스워드 확인
   * @param session
   * @param current_passwd
   * @return 1: 일치, 0: 불일치
   */
  @PostMapping(value="/passwd_check")
  @ResponseBody
  public String passwd_check(HttpSession session, @RequestBody String json_src) {
    System.out.println("-> json_src: " + json_src); // json_src: {"current_passwd":"1234"}
    
    JSONObject src = new JSONObject(json_src); // String -> JSON
    
    String current_passwd = (String)src.get("current_passwd"); // 값 가져오기
    System.out.println("-> current_passwd: " + current_passwd);
    
    try {
      Thread.sleep(3000);
    } catch(Exception e) {
      
    }
    
    int memberno = (int)session.getAttribute("memberno"); // session에서 가져오기
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("memberno", memberno);
    map.put("passwd", current_passwd);
    
    int cnt = this.memberProc.passwd_check(map);
    
    JSONObject json = new JSONObject();
    json.put("cnt", cnt); // 1: 현재 패스워드 일치
    System.out.println(json.toString());
    
    return json.toString();   
  }
  
  /**
   * 패스워드 변경
   * @param session
   * @param model
   * @param current_passwd 현재 패스워드
   * @param passwd 새로운 패스워드
   * @return
   */
  @PostMapping(value="/passwd_update_proc")
  public String update_passwd_proc(HttpSession session, 
                                                    Model model, 
                                                    @RequestParam(value="current_passwd", defaultValue = "") String current_passwd, 
                                                    @RequestParam(value="passwd", defaultValue = "") String passwd) {
    if (this.memberProc.isMember(session)) {
      int memberno = (int)session.getAttribute("memberno"); // session에서 가져오기
      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("memberno", memberno);
      map.put("passwd", current_passwd);
      
      int cnt = this.memberProc.passwd_check(map);
      
      if (cnt == 0) { // 패스워드 불일치
        model.addAttribute("code", "passwd_not_equal");
        model.addAttribute("cnt", 0);
        
      } else { // 패스워드 일치
        map = new HashMap<String, Object>();
        map.put("memberno", memberno);
        map.put("passwd", passwd); // 새로운 패스워드
        
        int passwd_change_cnt = this.memberProc.passwd_update(map);
        
        if (passwd_change_cnt == 1) {
          model.addAttribute("code", "passwd_change_success");
          model.addAttribute("cnt", 1);
        } else {
          model.addAttribute("code", "passwd_change_fail");
          model.addAttribute("cnt", 0);
        }
      }

      return "member/msg";   // /templates/member/msg.html
    } else {
      return "redirect:/member/login_cookie_need"; // redirect
      
    }
    

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

  
}



