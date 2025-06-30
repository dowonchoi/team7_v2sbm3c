package dev.mvc.order;

import dev.mvc.cart.CartVO;
import dev.mvc.cart.CartProcInter;
import dev.mvc.member.MemberVO;
import dev.mvc.order_item.OrderItemProcInter;
import dev.mvc.order_item.OrderItemVO;
import dev.mvc.products.ProductsVO;
import dev.mvc.products.ProductsProcInter;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderCont {

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
  @Qualifier("dev.mvc.products.ProductsProc")
  private ProductsProcInter productsProc;

  @GetMapping("/create")
  public String create_form(HttpSession session, Model model) {
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return "redirect:/member/login";  // 로그인 안 되어 있으면 로그인 페이지로
    }

    // 선택된 장바구니 항목 불러오기
    List<CartVO> cartList = cartProc.list_selected_by_memberno(memberno);
    model.addAttribute("cartList", cartList);

    // 총액 계산 (할인 적용된 가격들로)
    int total = cartProc.total_selected_by_memberno(memberno);
    model.addAttribute("total", total);

    return "/order/create";  // 배송지 + 결제수단 입력 폼
  }
  
  @GetMapping("/complete")
  public String complete(@RequestParam("orderno") int orderno, Model model) {
    model.addAttribute("orderno", orderno);
    return "/order/complete";
  }


}