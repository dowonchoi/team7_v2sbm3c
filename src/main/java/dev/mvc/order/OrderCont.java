package dev.mvc.order;

import dev.mvc.cart.CartVO;
import dev.mvc.delivery.DeliveryVO;
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
  
  @Autowired
  @Qualifier("dev.mvc.delivery.DeliveryProc")
  private dev.mvc.delivery.DeliveryProcInter deliveryProc;


  /** 주문 입력 폼 */
  @GetMapping("/create")
  public String create_form(
    @RequestParam(value = "orderno", required = false) Integer orderno,
    HttpSession session,
    Model model) {

    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return "redirect:/member/login";
    }

    // 선택된 장바구니만 불러오기
    List<CartVO> cartList = cartProc.list_selected_by_memberno(memberno);
    model.addAttribute("cartList", cartList);

    // 결제 총액
    int total = cartProc.total_selected_by_memberno(memberno);
    model.addAttribute("total", total);

    // 주문 완료 후 돌아온 경우 메시지용
    if (orderno != null) {
      model.addAttribute("orderno", orderno);
    }

    return "order/create";  // 주문/결제 폼
  }



  /** 주문 처리 */
  @PostMapping("/create")
  public String create_proc(OrderVO orderVO, HttpSession session) {
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return "redirect:/member/login";
    }

    // 선택된 장바구니 항목
    List<CartVO> cartList = cartProc.list_selected_by_memberno(memberno);
    if (cartList.isEmpty()) {
      // 선택된 항목이 없다면 실패 처리도 가능
      return "redirect:/cart/list"; 
    }
    
    DeliveryVO dvo = deliveryProc.read(orderVO.getDeliveryno());

    orderVO.setRname(dvo.getRname());
    orderVO.setRtel(dvo.getRtel());
    orderVO.setRzipcode(dvo.getRzipcode());
    orderVO.setRaddress1(dvo.getRaddress1());
    orderVO.setRaddress2(dvo.getRaddress2());
    orderVO.setMessage(dvo.getMessage());


    // 주문 기본 정보 저장
    orderVO.setMemberno(memberno);
    orderProc.create(orderVO);
    int orderno = orderVO.getOrderno();  // auto-increment된 주문 번호

    // 주문 상세 저장
    for (CartVO cart : cartList) {
      ProductsVO products = productsProc.read(cart.getProductsno());

      OrderItemVO item = new OrderItemVO();
      item.setOrderno(orderno);
      item.setProductsno(products.getProductsno());
      item.setPname(products.getTitle());
      item.setThumb1(products.getThumb1());
      item.setPrice(products.getPrice());
      item.setDc(products.getDc());
      item.setSaleprice(products.getSaleprice());
      item.setCnt(cart.getCnt());
      item.setPoint(products.getPoint());

      item.setTotalprice(products.getSaleprice() * cart.getCnt());
      item.setTotalpoint(products.getPoint() * cart.getCnt());

      orderItemProc.create(item);  // 주문 상세 insert
    }

    // 장바구니 선택 항목 삭제
    cartProc.delete_selected_by_memberno(memberno);

    // 다시 create 화면으로 이동하면서 주문번호 전달
    return "redirect:/order/create?orderno=" + orderno;
  }


}
