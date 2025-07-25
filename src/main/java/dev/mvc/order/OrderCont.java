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
 * 주문 처리 컨트롤러 - 장바구니 선택 항목을 주문으로 전환 - 주문 기본정보와 상세항목을 각각 테이블에 insert
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

  /**
   * 주문 입력 폼 페이지
   * --------------------------------------------------------------
   * [기능]
   * - 로그인한 사용자가 장바구니에서 선택한 상품들로 주문 작성 화면을 출력
   * - 선택된 장바구니 상품 목록 + 결제 예상 금액 + 예상 적립 포인트를 계산하여 전달
   * - 주문 완료 후 돌아온 경우, 주문 번호를 뷰에 전달하여 "주문 완료 메시지" 출력 가능
   *
   * [접근 URL]
   * - GET: /order/create
   *
   * [요청 파라미터]
   * - orderno (Optional): 주문 완료 후 메시지 출력용 (Redirect 시 전달)
   *
   * [처리 순서]
   * 1) 로그인 상태 확인 → 비로그인 시 로그인 페이지로 이동
   * 2) 현재 로그인 회원의 "선택된 장바구니" 항목 조회
   * 3) 주문 예상 금액 및 적립 포인트 계산
   * 4) 필요한 데이터(Model)에 담아 주문 페이지로 전달
   *
   * [View 파일]
   * - /templates/order/create.html
   */
  @GetMapping("/create")
  public String create_form(@RequestParam(value = "orderno", required = false) Integer orderno, HttpSession session,
      Model model) {
    
    // (1) 로그인 여부 확인
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {  // 비로그인 상태 → 로그인 페이지로 강제 이동
      return "redirect:/member/login";
    }

    // (2) 선택된 장바구니 목록 조회
    // - 회원번호(memberno)로 로그인 사용자의 장바구니 중 "선택된(Y)" 상태인 항목만 가져옴
    List<CartVO> cartList = cartProc.list_selected_by_memberno(memberno);
    model.addAttribute("cartList", cartList); // View에서 반복 출력 가능

    // (3) 결제 예상 총액 계산
    // - 선택된 상품들의 합계 (할인 적용 가격 × 수량)
    int total = cartProc.total_selected_by_memberno(memberno);
    model.addAttribute("total", total); // 총 결제 금액

    // (4) 예상 적립 포인트 계산
    // - 선택된 장바구니 항목만 필터링 후 (상품 포인트 × 수량) 합계
    int totalPoint = cartList.stream().filter(c -> "Y".equals(c.getSelected()))
        .mapToInt(c -> c.getProductsVO().getPoint() * c.getCnt()).sum();
    model.addAttribute("totalPoint", totalPoint); // 예상 적립 포인트

    // (5) 주문 완료 후 돌아온 경우 → 성공 메시지용 데이터 전달
    if (orderno != null) {
      model.addAttribute("orderno", orderno);  // 주문번호 전달 → 주문완료 메시지 표시 가능
    }
    return "order/create"; // 주문/결제 폼
  }

  
  /**
   * 주문 처리 (포인트 적립 포함)
   * ------------------------------------------------------------
   * [기능 설명]
   * - 장바구니에서 "선택된" 상품들을 기반으로 주문 생성
   * - 배송지 정보 세팅, 포인트 적립, 주문 상세(OrderItem) 생성
   * - 트랜잭션 기반으로 주문 + 상세항목 + 장바구니 삭제 처리
   *
   * [요청 URL]
   * - POST /order/create
   *
   * [처리 단계]
   * 1) 로그인 여부 확인 (비로그인 시 로그인 페이지로 이동)
   * 2) 장바구니에서 선택된 상품 목록 가져오기
   * 3) 배송지 번호 유효성 검증 → 배송지 상세정보 OrderVO에 세팅
   * 4) 적립 포인트 총합 계산
   * 5) 주문자 정보 세팅 (회원번호, 주문 상태)
   * 6) OrderItemVO 리스트 구성 (상품 단가, 수량, 합계 등)
   * 7) 주문 + 상세항목 저장 (트랜잭션 처리)
   * 8) 성공 시 장바구니 비우기 후 결제 완료 페이지로 리다이렉트
   *
   * [View]
   * - 결제 완료 후 → redirect:/order/complete?orderno={orderno}
   */
  @PostMapping("/create")
  public String create_proc(OrderVO orderVO, HttpSession session) {
    System.out.println("  [OrderCont] 주문 처리 시작");
    // (1) 로그인 확인
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return "redirect:/member/login";
    }

    // (2) 선택된 장바구니 항목 조회
    List<CartVO> cartList = cartProc.list_selected_by_memberno(memberno);
    if (cartList.isEmpty()) {   // 장바구니에 선택된 항목이 없으면 다시 장바구니로 이동
      System.out.println("❌ 선택된 장바구니 항목 없음");
      return "redirect:/cart/list";
    }

    // (3) 배송지 번호 유효성 검증 및 배송지 정보 세팅
    int deliveryno = orderVO.getDeliveryno();  // 폼에서 전달된 배송지 번호
    DeliveryVO dvo = deliveryProc.read(deliveryno); // DB에서 배송지 읽기
    if (dvo == null) {
      // 유효하지 않은 배송지 번호 → 주문 페이지로 이동
      System.out.println("❌ 잘못된 배송지 번호: " + deliveryno);
      return "redirect:/order/create";
    }

    // 배송지 정보 → 주문 객체(OrderVO)에 복사
    orderVO.setRname(dvo.getRname());       // 수령자 이름
    orderVO.setRtel(dvo.getRtel());         // 수령자 연락처
    orderVO.setRzipcode(dvo.getRzipcode()); // 우편번호
    orderVO.setRaddress1(dvo.getRaddress1());// 기본주소
    orderVO.setRaddress2(dvo.getRaddress2());// 상세주소
    orderVO.setMessage(dvo.getMessage());   // 배송 메시지

    // (4) 적립 포인트 합계 계산
    int totalPoint = 0;
    for (CartVO cart : cartList) {
      ProductsVO products = productsProc.read(cart.getProductsno());
      totalPoint += products.getPoint() * cart.getCnt();
    }
    orderVO.setPoint(totalPoint);  // 주문 전체 예상 적립 포인트 저장

    // (5) 주문자 정보 세팅
    orderVO.setMemberno(memberno);      // 주문자 회원번호
    orderVO.setStatus("결제완료");      // 초기 주문 상태 설정

    // (6) 주문 상세항목(OrderItemVO) 리스트 생성
    List<OrderItemVO> orderItems = new java.util.ArrayList<>();
    for (CartVO cart : cartList) {
      ProductsVO products = productsProc.read(cart.getProductsno());

      OrderItemVO item = new OrderItemVO();
      item.setProductsno(products.getProductsno()); // 상품번호
      item.setPname(products.getTitle());           // 상품명
      item.setThumb1(products.getThumb1());         // 썸네일
      item.setPrice(products.getPrice());           // 정가
      item.setDc(products.getDc());                 // 할인율
      item.setSaleprice(products.getSaleprice());   // 판매가
      item.setCnt(cart.getCnt());                   // 주문 수량
      item.setPoint(products.getPoint());           // 적립 포인트(개당)

      // 계산 필드
      item.setTotalprice(products.getSaleprice() * cart.getCnt()); // 상품별 총액
      item.setTotalpoint(products.getPoint() * cart.getCnt());     // 상품별 총 적립 포인트
      
      orderItems.add(item); // 리스트에 추가
    }

    // (7) 주문 + 상세항목 저장 (트랜잭션 처리)
    int result = orderProc.create(orderVO, orderItems); // DB 저장
    int orderno = orderVO.getOrderno(); // PK 자동생성된 주문번호 가져오기

    if (result > 0) {
      // 주문 생성 성공 시 → 장바구니 비우기
      cartProc.delete_selected_by_memberno(memberno);
      System.out.println("  주문 생성 완료, 주문번호: " + orderno);
    } else {
      System.out.println("❌ 주문 생성 실패");
      return "redirect:/order/create";
    }

    // (8) 결제 완료 페이지로 리디렉트
    return "redirect:/order/complete?orderno=" + orderno;
  }

  /**
   * [회원 전용] 주문 목록 조회
   * ------------------------------------------------------------
   * [기능 설명]
   * - 로그인한 사용자가 자신의 주문 내역을 확인
   * - 주문 + 주문 상세 항목을 함께 조회하여 화면에 표시
   *
   * [요청 URL]
   * - GET /order/list_by_member
   *
   * [View]
   * - /templates/order/list_by_member.html
   */
  @GetMapping("/list_by_member")
  public String list_by_member(HttpSession session, Model model) {
    // (1) 로그인 확인
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      // 로그인하지 않은 경우 → 로그인 페이지로 이동
      return "redirect:/member/login";
    }

    // (2) 상단 카테고리 메뉴 구성 (공통)
    List<CateVOMenu> menu = cateProc.menu();
    model.addAttribute("menu", menu);

    // (3) 로그인한 회원의 주문 목록 + 상세 항목 조회
    // OrderWithItemsVO → 주문 기본정보 + 주문 상세(OrderItem) 리스트 포함
    List<OrderWithItemsVO> orderList = orderProc.list_with_items_by_member(memberno);
    model.addAttribute("orderList", orderList);
    
    // (4) 주문 내역 페이지로 이동
    return "order/list_by_member"; // HTML 템플릿
  }

  /**
   * [공통] 결제 완료 페이지
   * ------------------------------------------------------------
   * [기능 설명]
   * - 주문 완료 후, 주문번호(orderno)를 기반으로 주문 상세 정보를 화면에 출력
   *
   * [요청 URL]
   * - GET /order/complete?orderno=123
   *
   * [View]
   * - /templates/order/complete.html
   */
  @GetMapping("/complete")
  public String order_complete(@RequestParam("orderno") int orderno, Model model) {
    // (1) 주문 정보 조회 (orderno 기준)
    OrderVO orderVO = orderProc.read(orderno);
    
    // (2) 주문 정보 View에 전달
    model.addAttribute("order", orderVO);
    
    // (3) 결제 완료 페이지 이동
    return "order/complete"; // 템플릿 파일: /templates/order/complete.html
  }

  /**
   * [관리자 전용] 전체 주문 목록 조회
   * ------------------------------------------------------------
   * [기능 설명]
   * - 관리자(admin 등급)만 접근 가능
   * - 전체 회원의 모든 주문 목록을 조회
   *
   * [요청 URL]
   * - GET /order/list_all
   *
   * [View]
   * - /templates/order/list_all.html
   */
  @GetMapping("/list_all")
  public String list_all(HttpSession session, Model model) {
    // (1) 관리자 권한 확인
    String grade = (String) session.getAttribute("grade");
    System.out.println("session.getAttribute(\"grade\"): " + grade);

    // 로그인 안 했거나 관리자 등급이 아닌 경우 차단
    if (grade == null || !grade.equals("admin")) {
      return "redirect:/member/login_cookie_need";
    }

    // (2) 전체 주문 목록 조회
    List<OrderVO> orderList = orderProc.list_all(); // 전체 주문 목록
    model.addAttribute("orderList", orderList);

    return "order/list_all"; // templates/order/list_all.html
  }

  /**
   * [공급자 전용] 주문 목록 조회
   * ------------------------------------------------------------
   * [기능 설명]
   * - 로그인한 공급자(등급 5~15)가 자신이 등록한 상품이 포함된 주문 내역을 조회
   * - 주문 기본정보 + 주문 상세 항목(OrderItem) 리스트를 함께 전달
   *
   * [요청 URL]
   * - GET /order/list_by_supplier
   *
   * [View]
   * - /templates/order/list_by_supplier.html
   */
  @GetMapping("/list_by_supplier")
  public String list_by_supplier(HttpSession session, Model model) {
    // (1) 세션에서 등급(grade)과 회원번호(memberno) 가져오기
    Integer gradeObj = (Integer) session.getAttribute("grade");
    Integer memberno = (Integer) session.getAttribute("memberno");

    // 비로그인 또는 등급 정보 없음 -> 로그인 페이지로 리디렉트
    if (gradeObj == null || memberno == null) {
      return "redirect:/member/login_cookie_need";
    }

    int grade = gradeObj;

    // (2) 공급자 권한 확인
    // 공급자(5~15등급)만 접근 허용
    if (grade < 5 || grade > 15) {
      return "redirect:/member/login_cookie_need";
    }

    // (3) 공급자가 등록한 상품이 포함된 주문 목록 조회
    // orderProc.list_with_items_by_supplier(memberno)
    // → 해당 공급자가 등록한 상품이 하나라도 포함된 주문 목록을 반환
    List<OrderWithItemsVO> orderList = orderProc.list_with_items_by_supplier(memberno);
    model.addAttribute("orderList", orderList);

    // (4) 공급자 전용 주문 목록 페이지로 이동
    return "order/list_by_supplier";
  }

}
