package dev.mvc.productsgood;

import java.util.ArrayList;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dev.mvc.calendar.CalendarVO;
import dev.mvc.cate.CateProcInter;
import dev.mvc.cate.CateVOMenu;
import dev.mvc.member.MemberProcInter;
import dev.mvc.products.ProductsVO;
import dev.mvc.productsgood.ProductsproductsgoodMemberVO;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;

@Controller
@RequestMapping(value = "/productsgood")
public class ProductsgoodCont {
  @Autowired
  @Qualifier("dev.mvc.member.MemberProc") // @Service("dev.mvc.member.MemberProc")
  private MemberProcInter memberProc;

  @Autowired
  @Qualifier("dev.mvc.cate.CateProc") // @Component("dev.mvc.cate.CateProc")
  private CateProcInter cateProc;

  @Autowired
  @Qualifier("dev.mvc.productsgood.ProductsgoodProc")
  ProductsgoodProcInter productsgoodProc;

  public ProductsgoodCont() {
    System.out.println("-> productsgoodCont created.");
  }

 //===============================================================
 // [공용 처리] POST → GET 변환
 // ===============================================================

 /**
  * POST → GET 전환용 (새로고침 시 중복 방지)
  * - POST 요청 처리 후 redirect 시 사용
  * - url 파라미터에 따라 해당 View로 forward
  * ---------------------------------------------------------------
  * URL: /productsgood/post2get
  * 파라미터:
  *   url: redirect할 뷰 경로
  * 리턴: url 경로의 Thymeleaf 뷰
  */
  @GetMapping(value = "/post2get")
  public String post2get(Model model, @RequestParam(name = "url", defaultValue = "") String url) {
    // 상단 카테고리 메뉴 추가
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);

    return url; // forward, /templates/...
  }

  
 //===============================================================
 // [추천 등록] AJAX 요청 처리
 // ===============================================================

 /**
  * 상품 추천 등록 (AJAX)
  * - 요청 JSON 예시: {"productsno": 101}
  * - 응답 JSON 예시: {"status":"success"} or {"status":"fail"}
  * ---------------------------------------------------------------
  * URL: /productsgood/create
  * 접근 권한: 로그인 사용자
  */
  @PostMapping(value = "/create")
  @ResponseBody
  public String create(HttpSession session, @RequestBody ProductsgoodVO productsgoodVO) {
    JSONObject json = new JSONObject();
    // (1) 로그인 체크
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      json.put("status", "fail");
      json.put("message", "로그인이 필요합니다.");
      return json.toString();
    }
    
    // (2) 추천 정보 설정 및 DB 등록
    productsgoodVO.setMemberno(memberno);
    int cnt = this.productsgoodProc.create(productsgoodVO);
    
    // (3) 응답 생성
    json.put("status", (cnt == 1) ? "success" : "fail");
    return json.toString();
  }

 //===============================================================
 // [추천 목록] 관리자용
 // ===============================================================

 /**
  * 추천 상품 전체 목록 (관리자)
  * - 상품 + 추천 회원 정보 조인 출력
  * ---------------------------------------------------------------
  * URL: /productsgood/list_all
  * View: productsgood/list_all_join.html
  */
  @GetMapping(value = "/list_all")
  public String list_all(Model model) {
    // (1) 추천 상품 + 회원 정보 JOIN 리스트
    ArrayList<ProductsproductsgoodMemberVO> list = this.productsgoodProc.list_all_join();
    model.addAttribute("list", list);

    // (2) 카테고리 메뉴 (헤더용)
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);

    return "productsgood/list_all_join"; // 관리자용 추천 목록 페이지
  }

  //===============================================================
  // [추천 삭제, 소비자·공급자 추천 목록 조회 기능]
  //===============================================================
  
  /**
  * 추천 삭제 (관리자 전용) 쓸 일은 없을 것이라고 예상.
  * ---------------------------------------------------------------
  * - 관리자만 특정 추천(productsgood) 레코드를 삭제 가능
  * - POST 요청 처리 후 추천 목록 페이지로 redirect
  * URL: /productsgood/delete
  * 파라미터:
  *   productsgoodno: 삭제할 추천 번호 (기본값 0)
  * 접근 권한: 관리자 (isAdmin 체크)
  */
  @PostMapping(value = "/delete")
  public String delete_proc(HttpSession session,
      @RequestParam(name = "productsgoodno", defaultValue = "0") int productsgoodno, RedirectAttributes ra) {
    // 관리자 여부 확인
    if (this.memberProc.isAdmin(session)) {
      // (1) 추천 삭제 처리
      this.productsgoodProc.delete(productsgoodno);
      // (2) 삭제 후 추천 목록 페이지로 이동
      return "redirect:/productsgood/list_all";
    } else {
      // (3) 권한 없음 → 로그인 필요 안내 페이지로 이동
      ra.addAttribute("url", "/member/login_cookie_need");
      return "redirect:/productsgood/post2get";
    }
  }

  //===============================================================
  //[소비자 전용] 내가 추천한 상품 목록
  //===============================================================
  
  /**
  * 소비자: 내가 추천한 상품 목록 조회
  * ---------------------------------------------------------------
  * - 로그인한 소비자가 "좋아요(하트)"한 상품 리스트 출력
  * - 비로그인 시 로그인 안내 페이지로 이동
  * URL: /productsgood/user_liked_list
  * View: products/user_liked_list.html
  */
  @GetMapping("/user_liked_list")
  public String userLikedList(HttpSession session, Model model) {
    // 로그인 확인
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return "redirect:/member/login_cookie_need";
    }
    // (1) 내가 추천한 상품 목록 조회
    ArrayList<ProductsVO> list = this.productsgoodProc.list_user_liked_products(memberno);
    // (2) View에 데이터 전달
    model.addAttribute("list", list);
    return "products/user_liked_list";
  }

//===============================================================
//[공급자 전용] 내가 등록한 상품 중 추천 받은 상품 목록
//===============================================================

  /**
  * 공급자: 내가 등록한 상품 중 "추천을 받은 상품" 목록 조회
  * ---------------------------------------------------------------
  * - 로그인한 공급자가 올린 상품 중 추천수 > 0 인 상품만 출력
  * - 비로그인 시 로그인 안내 페이지로 이동
  * URL: /productsgood/supplier_products_liked
  * View: products/supplier_liked_list.html
  */
  @GetMapping("/supplier_products_liked")
  public String supplierProductsLiked(HttpSession session, Model model) {
    // 로그인 확인
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return "redirect:/member/login_cookie_need";
    }
    // (1) 내가 등록한 상품 중 추천 받은 상품 목록 조회
    ArrayList<ProductsVO> list = this.productsgoodProc.list_supplier_products_liked(memberno);
    // (2) View에 데이터 전달
    model.addAttribute("list", list);
    return "products/supplier_liked_list";
  }

}
