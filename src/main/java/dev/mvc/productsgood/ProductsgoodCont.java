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
  
  /**
   * POST 요청시 새로고침 방지, POST 요청 처리 완료 → redirect → url → GET → forward -> html 데이터
   * 전송
   * 
   * @return
   */
  @GetMapping(value = "/post2get")
  public String post2get(Model model, 
      @RequestParam(name="url", defaultValue = "") String url) {
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);

    return url; // forward, /templates/...
  }
  
  @PostMapping(value="/create")
  @ResponseBody
  public String create(HttpSession session, @RequestBody ProductsgoodVO productsgoodVO) {
      JSONObject json = new JSONObject();

      Integer memberno = (Integer) session.getAttribute("memberno");
      if (memberno == null) {
          json.put("status", "fail");
          json.put("message", "로그인이 필요합니다.");
          return json.toString();
      }

      productsgoodVO.setMemberno(memberno);
      int cnt = this.productsgoodProc.create(productsgoodVO);

      json.put("status", (cnt == 1) ? "success" : "fail");
      return json.toString();
  }


  
//  /**
//   * 목록
//   * 
//   * @param model
//   * @return
//   */
//  // http://localhost:9091/productsgood/list_all
//  @GetMapping(value = "/list_all")
//  public String list_all(Model model) {
//    ArrayList<productsgoodVO> list = this.productsgoodProc.list_all();
//    model.addAttribute("list", list);
//
//    ArrayList<CateVOMenu> menu = this.cateProc.menu();
//    model.addAttribute("menu", menu);
//
//    return "productsgood/list_all"; // /templates/productsgood/list_all.html
//  }
  
  /** 추천 목록 (관리자) */
  @GetMapping(value = "/list_all")
  public String list_all(Model model) {
      ArrayList<ProductsproductsgoodMemberVO> list = this.productsgoodProc.list_all_join();
      model.addAttribute("list", list);

      ArrayList<CateVOMenu> menu = this.cateProc.menu();
      model.addAttribute("menu", menu);

      return "productsgood/list_all_join";
  }

  /** 추천 삭제 (관리자) */
  @PostMapping(value = "/delete")
  public String delete_proc(HttpSession session, @RequestParam(name = "productsgoodno", defaultValue = "0") int productsgoodno, RedirectAttributes ra) {
      if (this.memberProc.isAdmin(session)) {
          this.productsgoodProc.delete(productsgoodno);
          return "redirect:/productsgood/list_all";
      } else {
          ra.addAttribute("url", "/member/login_cookie_need");
          return "redirect:/productsgood/post2get";
      }
  }

  /** 소비자: 내가 추천한 상품 */
  @GetMapping("/user_liked_list")
  public String userLikedList(HttpSession session, Model model) {
      Integer memberno = (Integer) session.getAttribute("memberno");
      if (memberno == null) {
          return "redirect:/member/login_cookie_need";
      }
      ArrayList<ProductsVO> list = this.productsgoodProc.list_user_liked_products(memberno);
      model.addAttribute("list", list);
      return "products/user_liked_list";
  }

  /** 공급자: 내가 등록한 상품 중 추천 받은 상품 */
  @GetMapping("/supplier_products_liked")
  public String supplierProductsLiked(HttpSession session, Model model) {
      Integer memberno = (Integer) session.getAttribute("memberno");
      if (memberno == null) {
          return "redirect:/member/login_cookie_need";
      }
      ArrayList<ProductsVO> list = this.productsgoodProc.list_supplier_products_liked(memberno);
      model.addAttribute("list", list);
      return "products/supplier_liked_list";
  }

  
}






