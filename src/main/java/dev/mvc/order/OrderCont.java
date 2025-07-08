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

/**
 * 주문 처리 컨트롤러
 * - 장바구니 선택 항목을 주문으로 전환
 * - 주문 기본정보와 상세항목을 각각 테이블에 insert
 */
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

  /** 주문 입력 폼 */
  @GetMapping("/create")
  public String create_form(HttpSession session, Model model) {
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return "redirect:/member/login";  // 비로그인 → 로그인 유도
    }

    // 선택된 장바구니 항목 불러오기
    List<CartVO> cartList = cartProc.list_selected_by_memberno(memberno);
    model.addAttribute("cartList", cartList);

    // 총액 계산 (할인 적용된 가격들로)
    int total = cartProc.total_selected_by_memberno(memberno);
    model.addAttribute("total", total);

    return "order/create";  // 배송지 + 결제수단 입력 폼 출력
  }

  /** 주문 완료 화면 */
  @GetMapping("/complete")
  public String complete(@RequestParam("orderno") int orderno, Model model) {
    model.addAttribute("orderno", orderno);
    return "/order/complete";
  }

  /** 주문 처리 */
  @PostMapping("/create")
  public String create_proc(OrderVO orderVO, HttpSession session) {
    System.out.println(" [DEBUG] 주문 요청 도달 - 수령자: " + orderVO.getRname());
    Integer memberno = (Integer) session.getAttribute("memberno");
    System.out.println(" [DEBUG] 주문 요청 도달 - 수령자: " + orderVO.getRname());

    if (memberno == null) {
      System.out.println("[DEBUG] 비로그인 상태로 주문 요청됨.");
      return "redirect:/member/login";
    }
    
    List<CartVO> cartList = cartProc.list_selected_by_memberno(memberno);
    System.out.println("[DEBUG] 선택된 장바구니 수: " + cartList.size());
    for (CartVO cart : cartList) {
      System.out.println("[DEBUG] cartno: " + cart.getCartno()
                         + ", productsno: " + cart.getProductsno()
                         + ", cnt: " + cart.getCnt()
                         + ", title: " + (cart.getProductsVO() != null ? cart.getProductsVO().getTitle() : "null"));
    }

    // 2. 주문 기본 정보 저장
    orderVO.setMemberno(memberno);
    orderProc.create(orderVO);
    int orderno = orderVO.getOrderno();
    System.out.println("[DEBUG] 주문 등록 완료 - orderno: " + orderno);
    
    // 3. 주문 상세로 전환
    for (CartVO cart : cartList) {
      int productsno = cart.getProductsno();
      ProductsVO products = productsProc.read(productsno);

      OrderItemVO item = new OrderItemVO();
      item.setOrderno(orderno);
      item.setProductsno(productsno);
      item.setPname(products.getTitle());
      item.setThumb1(products.getThumb1());
      item.setPrice(products.getPrice());
      item.setDc(products.getDc());
      item.setSaleprice(products.getSaleprice());
      item.setCnt(cart.getCnt());
      item.setPoint(products.getPoint());

      int totalprice = products.getSaleprice() * cart.getCnt();
      int totalpoint = products.getPoint() * cart.getCnt();

      item.setTotalprice(totalprice);
      item.setTotalpoint(totalpoint);

      orderItemProc.create(item);

      System.out.println("[DEBUG] 주문 상세 등록 - productsno: " + productsno + ", cnt: " + cart.getCnt());
    }

    // 4. 장바구니 선택 항목 삭제
    int deleted = cartProc.delete_selected_by_memberno(memberno);
    System.out.println("[DEBUG] 장바구니 삭제 완료 - 삭제된 항목 수: " + deleted);

    // 5. 완료 페이지로 이동
    System.out.println("[DEBUG] 주문 프로세스 완료 - 리디렉션 수행");
    return "redirect:/order/complete?orderno=" + orderno;
  }

}
