package dev.mvc.cart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import dev.mvc.cate.CateProcInter;
import dev.mvc.cate.CateVOMenu;



@Controller
@RequestMapping("/cart")
public class CartCont {

  @Autowired
  @Qualifier("dev.mvc.cate.CateProc")
  private CateProcInter cateProc;

  @Autowired
  @Qualifier("dev.mvc.cart.CartProc") 
  private CartProcInter cartProc;
  
  /** 장바구니 담기 */
  @GetMapping("/add")
  public String add(HttpSession session,
                    @RequestParam(name = "productsno") int productsno,
                    @RequestParam(name = "cnt", defaultValue = "1") int cnt,
                    @RequestParam(name = "selected", defaultValue = "Y") String selected,
                    Model model) {

    // 등급 체크
    String grade = (String) session.getAttribute("grade");
    Integer memberno = (Integer) session.getAttribute("memberno");

    if (grade == null || memberno == null || 
        "withdrawn".equals(grade) || "supplier".equals(grade)) {
      return "redirect:/member/login_cookie_need?url=/products/read?productsno=" + productsno;
    }

    CartVO cartVO = new CartVO();
    cartVO.setMemberno(memberno);
    cartVO.setProductsno(productsno);
    cartVO.setCnt(cnt);
    cartVO.setSelected(selected);

    cartProc.create(cartVO);

    return "redirect:/cart/list";
  }

  /** 장바구니 목록 */
  @GetMapping("/list")
  public String list(HttpSession session, Model model) {
    String grade = (String) session.getAttribute("grade");
    Integer memberno = (Integer) session.getAttribute("memberno");

    if (grade == null || memberno == null || "withdrawn".equals(grade)) {
      return "redirect:/member/login_cookie_need?url=/cart/list";
    }

    // 상단 메뉴
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);

    // 장바구니 목록 + 총합
    ArrayList<CartVO> list = cartProc.list_by_memberno(memberno);
    int total = cartProc.sum_total_price(memberno); // 총 결제 금액
    int totalPriceOrigin = cartProc.sum_total_price_origin(memberno); //총 상품 가격(할인 전)
    model.addAttribute("totalPriceOrigin", totalPriceOrigin);
    int totalDiscount = cartProc.sum_total_discount(memberno); //총 할인
    model.addAttribute("totalDiscount", totalDiscount);
    int totalPoint = cartProc.sum_total_point(memberno); //  적립 총 포인트


    model.addAttribute("list", list);
    model.addAttribute("total", total);
    model.addAttribute("totalPoint", totalPoint);

    return "cart/list";  // /templates/cart/list.html
  }

  /** 수량 수정 (AJAX 또는 form용 POST) */
  @PostMapping("/update_cnt")
  public String update_cnt(HttpSession session,
                           @RequestParam("cartno") int cartno,
                           @RequestParam("cnt") int cnt) {

    String grade = (String) session.getAttribute("grade");
    Integer memberno = (Integer) session.getAttribute("memberno");

    if (grade == null || memberno == null || "withdrawn".equals(grade)) {
      return "redirect:/member/login_cookie_need?url=/cart/list";
    }

    Map<String, Object> map = new HashMap<>();
    map.put("cartno", cartno);
    map.put("cnt", cnt);

    cartProc.update_cnt(map);

    return "redirect:/cart/list";
  }

  /** 개별 항목 삭제 */
  @PostMapping("/delete")
  public String delete(HttpSession session,
                       @RequestParam("cartno") int cartno) {

    String grade = (String) session.getAttribute("grade");
    Integer memberno = (Integer) session.getAttribute("memberno");

    if (grade == null || memberno == null || "withdrawn".equals(grade)) {
      return "redirect:/member/login_cookie_need?url=/cart/list";
    }

    cartProc.delete(cartno);

    return "redirect:/cart/list";
  }

  /** 전체 비우기 */
  @PostMapping("/delete_all")
  public String delete_all(HttpSession session) {
    String grade = (String) session.getAttribute("grade");
    Integer memberno = (Integer) session.getAttribute("memberno");

    if (grade == null || memberno == null || "withdrawn".equals(grade)) {
      return "redirect:/member/login_cookie_need?url=/cart/list";
    }

    cartProc.delete_by_memberno(memberno);

    return "redirect:/cart/list";
  }
  
  @PostMapping("/update_selected")
  @ResponseBody
  public String update_selected(@RequestParam("cartno") int cartno,
                                @RequestParam("selected") String selected,
                                HttpSession session) {
    String grade = (String) session.getAttribute("grade");
    Integer memberno = (Integer) session.getAttribute("memberno");

    if (grade == null || memberno == null || "withdrawn".equals(grade)) {
      return "unauthorized";
    }

    Map<String, Object> map = new HashMap<>();
    map.put("cartno", cartno);
    map.put("selected", selected);

    int updated = cartProc.update_selected(map);

    return (updated == 1) ? "success" : "fail";
  }
  
  @PostMapping("/cart/delete_selected")
  public String deleteSelected(@RequestParam("cartnos") String cartnos, HttpSession session) {
      int memberno = (int) session.getAttribute("memberno"); // 세션 확인

      String[] cartnoArr = cartnos.split(",");
      for (String cartnoStr : cartnoArr) {
          int cartno = Integer.parseInt(cartnoStr);
          cartProc.delete(cartno); // 단건 삭제
      }

      return "redirect:/cart/list";
  }
  
  @PostMapping("/add_ajax")
  @ResponseBody
  public Map<String, Object> add_ajax(@RequestBody Map<String, Object> payload, HttpSession session) {
      Map<String, Object> response = new HashMap<>();

      Integer memberno = (Integer) session.getAttribute("memberno");
      String grade = (String) session.getAttribute("grade");

      if (memberno == null || "withdrawn".equals(grade) || "supplier".equals(grade)) {
          response.put("success", false);
          response.put("message", "로그인이 필요합니다.");
          return response;
      }

      try {
          int productsno = Integer.parseInt(payload.get("productsno").toString());
          int cnt = Integer.parseInt(payload.get("cnt").toString());

          CartVO cartVO = new CartVO();
          cartVO.setMemberno(memberno);
          cartVO.setProductsno(productsno);
          cartVO.setCnt(cnt);
          cartVO.setSelected("Y");

          cartProc.create(cartVO);

          response.put("success", true);
          response.put("message", "장바구니에 추가되었습니다.");
      } catch (Exception e) {
          response.put("success", false);
          response.put("message", "데이터 변환 오류 발생");
      }

      return response;
  }

  /**
   * 로그인한 사용자의 장바구니 아이템 개수 반환 (AJAX)
   */
  @GetMapping("/count")
  @ResponseBody
  public int getCartCount(HttpSession session) {
      // ✅ 로그인 확인
      Integer memberno = (Integer) session.getAttribute("memberno");
      if (memberno == null) {
          return 0; // 비로그인 사용자는 0 반환
      }

      // ✅ 장바구니 개수 조회
      return cartProc.count_by_member(memberno);
  }




}
