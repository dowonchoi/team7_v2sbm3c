package dev.mvc.order;

import dev.mvc.cart.CartVO;
import dev.mvc.cate.CateProcInter;
import dev.mvc.cate.CateVOMenu;
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
  @Qualifier("dev.mvc.cate.CateProc")
  private CateProcInter cateProc;

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

      // ✅ 선택된 장바구니만 불러오기
      List<CartVO> cartList = cartProc.list_selected_by_memberno(memberno);
      model.addAttribute("cartList", cartList);

      // ✅ 결제 총액
      int total = cartProc.total_selected_by_memberno(memberno);
      model.addAttribute("total", total);

      // ✅ 총 포인트 계산 (기존 Thymeleaf 코드 대체)
      int totalPoint = cartList.stream()
              .filter(c -> "Y".equals(c.getSelected()))
              .mapToInt(c -> c.getProductsVO().getPoint() * c.getCnt())
              .sum();
      model.addAttribute("totalPoint", totalPoint);

      // ✅ 주문 완료 후 돌아온 경우 메시지용
      if (orderno != null) {
          model.addAttribute("orderno", orderno);
      }

      return "order/create";  // 주문/결제 폼
  }




  /** 주문 처리 (포인트 적립까지) */
  @PostMapping("/create")
  public String create_proc(OrderVO orderVO, HttpSession session) {
      System.out.println("✅ [OrderCont] 주문 처리 시작");

      Integer memberno = (Integer) session.getAttribute("memberno");
      if (memberno == null) {
          return "redirect:/member/login";
      }

      // ✅ 선택된 장바구니 항목 가져오기
      List<CartVO> cartList = cartProc.list_selected_by_memberno(memberno);
      if (cartList.isEmpty()) {
          System.out.println("❌ 선택된 장바구니 항목 없음");
          return "redirect:/cart/list";
      }

      // ✅ 배송지 유효성 검증
      int deliveryno = orderVO.getDeliveryno();
      DeliveryVO dvo = deliveryProc.read(deliveryno);
      if (dvo == null) {
          System.out.println("❌ 잘못된 배송지 번호: " + deliveryno);
          return "redirect:/order/create";
      }

      // ✅ 배송지 정보 세팅
      orderVO.setRname(dvo.getRname());
      orderVO.setRtel(dvo.getRtel());
      orderVO.setRzipcode(dvo.getRzipcode());
      orderVO.setRaddress1(dvo.getRaddress1());
      orderVO.setRaddress2(dvo.getRaddress2());
      orderVO.setMessage(dvo.getMessage());

   // ✅ 포인트 합계 계산
      int totalPoint = 0;
      for (CartVO cart : cartList) {
          ProductsVO products = productsProc.read(cart.getProductsno());
          totalPoint += products.getPoint() * cart.getCnt();
      }
      orderVO.setPoint(totalPoint);

      // ✅ 주문자 정보 세팅
      orderVO.setMemberno(memberno);
      orderVO.setStatus("결제완료");

      // ✅ OrderItemVO 리스트 생성
      List<OrderItemVO> orderItems = new java.util.ArrayList<>();
      for (CartVO cart : cartList) {
          ProductsVO products = productsProc.read(cart.getProductsno());

          OrderItemVO item = new OrderItemVO();
          item.setProductsno(products.getProductsno());
          item.setPname(products.getTitle());
          item.setThumb1(products.getThumb1());
          item.setPrice(products.getPrice());
          item.setDc(products.getDc());
          item.setSaleprice(products.getSaleprice());
          item.setCnt(cart.getCnt());
          item.setPoint(products.getPoint());

          // 계산 필드
          item.setTotalprice(products.getSaleprice() * cart.getCnt());
          item.setTotalpoint(products.getPoint() * cart.getCnt());

          orderItems.add(item);
      }

      // ✅ 주문 + 상세 저장 + 포인트 적립 (트랜잭션)
      int result = orderProc.create(orderVO, orderItems);
      int orderno = orderVO.getOrderno();

      if (result > 0) {
          // ✅ 장바구니 비우기
          cartProc.delete_selected_by_memberno(memberno);
          System.out.println("✅ 주문 생성 완료, 주문번호: " + orderno);
      } else {
          System.out.println("❌ 주문 생성 실패");
          return "redirect:/order/create";
      }

      // ✅ 결제 완료 페이지로 이동
      return "redirect:/order/complete?orderno=" + orderno;
  }



  
  //5. OrderCont.java - 주문 목록 조회 기능 추가
  @GetMapping("/list_by_member")
  public String list_by_member(HttpSession session, Model model) {
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return "redirect:/member/login";
    }
    
 // ✅ 전체 카테고리 메뉴 추가
    List<CateVOMenu> menu = cateProc.menu();
    model.addAttribute("menu", menu);

    List<OrderWithItemsVO> orderList = orderProc.list_with_items_by_member(memberno);
    model.addAttribute("orderList", orderList);
    return "order/list_by_member";  // HTML 템플릿
  }



  /** 결제 완료 페이지 */
  @GetMapping("/complete")
  public String order_complete(@RequestParam("orderno") int orderno, Model model) {
    OrderVO orderVO = orderProc.read(orderno);
    model.addAttribute("order", orderVO); //  주문 정보 전달
    return "order/complete"; // 템플릿 파일: /templates/order/complete.html
  }
  
  /**
   *  관리자용 주문 목록 조회
   * 관리자: 전체 주문 목록 조회
   * grade: 1~4만 허용
   */
  @GetMapping("/list_all")
  public String list_all(HttpSession session, Model model) {
    String grade = (String) session.getAttribute("grade");

    System.out.println("🔍 session.getAttribute(\"grade\"): " + grade);

    // 로그인 안 했거나 관리자 등급이 아닌 경우
    if (grade == null || !grade.equals("admin")) {
      return "redirect:/member/login_cookie_need";
    }

    List<OrderVO> orderList = orderProc.list_all();  // 전체 주문 목록
    model.addAttribute("orderList", orderList);

    return "order/list_all";  // templates/order/list_all.html
  }


  
  /**
   * 공급자용 주문 목록 조회
   * 공급자: 내가 등록한 상품을 소비자가 구매한 주문 목록
   * grade: 5~15만 허용
   */
  /**
   * 공급자용 주문 목록 조회
   * 공급자: 내가 등록한 상품이 포함된 주문들만 출력
   * grade: 5~15만 허용
   */
  @GetMapping("/list_by_supplier")
  public String list_by_supplier(HttpSession session, Model model) {
    String grade = (String) session.getAttribute("grade");
    Integer memberno = (Integer) session.getAttribute("memberno");

    if (grade == null || !grade.equals("supplier")) {
      return "redirect:/member/login_cookie_need";
    }

    List<OrderWithItemsVO> orderList = orderProc.list_with_items_by_supplier(memberno);
    model.addAttribute("orderList", orderList);

    return "order/list_by_supplier";
  }





}
