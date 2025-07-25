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

/**
 * ===============================================================
 * CartCont (장바구니 컨트롤러)
 * ===============================================================
 * 기능:
 *   - 장바구니 담기 (add)
 *   - 장바구니 목록 조회 (list)
 *
 * 접근 제어:
 *   - 권한 정책: admin, user만 접근 가능
 *   - supplier / withdrawn 등급은 제한
 *
 * View 경로:
 *   - /templates/cart/list.html
 */
@Controller
@RequestMapping("/cart")
public class CartCont {

  @Autowired
  @Qualifier("dev.mvc.cate.CateProc")
  private CateProcInter cateProc;

  @Autowired
  @Qualifier("dev.mvc.cart.CartProc")
  private CartProcInter cartProc;

  /**
   * ===============================================================
   * [GET] 장바구니 담기
   * ===============================================================
   * URL:
   *   /cart/add?productsno=10&cnt=1&selected=Y
   *
   * 요청 파라미터:
   *   - productsno (필수): 담을 상품 번호
   *   - cnt        (선택): 수량 (기본값: 1)
   *   - selected   (선택): 선택 여부 (기본값: 'Y')
   *
   * 접근 권한:
   *   - admin / user만 가능
   *   - supplier, withdrawn → 접근 불가 (리다이렉트)
   *
   * 처리 내용:
   *   1) 로그인 여부 + 등급 체크
   *   2) CartVO 객체 생성 후 DB 저장
   *   3) 장바구니 목록 페이지로 리다이렉트
   *
   * View:
   *   - 성공 시: redirect:/cart/list
   *   - 실패 시: redirect:/member/login_cookie_need
   */
  @GetMapping("/add")
  public String add(HttpSession session, @RequestParam(name = "productsno") int productsno,
      @RequestParam(name = "cnt", defaultValue = "1") int cnt,
      @RequestParam(name = "selected", defaultValue = "Y") String selected, Model model) {

    // (1) 로그인 및 권한 체크
    Object gradeObj = session.getAttribute("grade");
    String grade = (gradeObj != null) ? gradeObj.toString() : null;
    Integer memberno = (Integer) session.getAttribute("memberno");

    // supplier 또는 withdrawn → 접근 제한
    if (grade == null || memberno == null || "withdrawn".equals(grade) || "supplier".equals(grade)) {
      return "redirect:/member/login_cookie_need?url=/products/read?productsno=" + productsno;
    }

    // (2) CartVO 생성 및 DB 등록
    CartVO cartVO = new CartVO();
    cartVO.setMemberno(memberno);       // 로그인 회원 번호
    cartVO.setProductsno(productsno);   // 상품 번호
    cartVO.setCnt(cnt);                 // 구매 수량
    cartVO.setSelected(selected);       // 선택 여부

    cartProc.create(cartVO); // DB 저장
    
    // (3) 장바구니 목록 페이지로 이동
    return "redirect:/cart/list";
  }

  /**
   * ===============================================================
   * [GET] 장바구니 목록
   * ===============================================================
   * URL:
   *   /cart/list
   *
   * 접근 권한:
   *   - admin / user만 가능
   *   - supplier, withdrawn → 접근 제한
   *
   * 처리 내용:
   *   1) 로그인 및 권한 체크
   *   2) 장바구니 목록 조회
   *   3) 총 상품 가격 / 총 할인 / 총 적립 포인트 / 결제 예상 금액 계산
   *   4) View에 데이터 바인딩
   *
   * View:
   *   - /templates/cart/list.html
   */
  @GetMapping("/list")
  public String list(HttpSession session, Model model) {
    // (1) 로그인 및 권한 체크
    Object gradeObj = session.getAttribute("grade");
    String grade = (gradeObj != null) ? gradeObj.toString() : null;
    Integer memberno = (Integer) session.getAttribute("memberno");

    if (grade == null || memberno == null || "withdrawn".equals(grade) || "supplier".equals(grade)) {
      return "redirect:/member/login_cookie_need?url=/cart/list";
    }

    // (2) 상단 메뉴 카테고리 데이터
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);

    // (3) 장바구니 목록 및 금액 계산
    ArrayList<CartVO> list = cartProc.list_by_memberno(memberno);
    // 총 결제 금액 (할인 적용 후)
    int total = cartProc.sum_total_price(memberno);
    int totalPriceOrigin = cartProc.sum_total_price_origin(memberno); // 총 상품 가격(할인 전)
    int totalDiscount = cartProc.sum_total_discount(memberno); // 총 할인
    int totalPoint = cartProc.sum_total_point(memberno); // 적립 총 포인트

    // (4) View로 데이터 전달
    model.addAttribute("list", list);
    model.addAttribute("total", total);
    model.addAttribute("totalPriceOrigin", totalPriceOrigin);
    model.addAttribute("totalDiscount", totalDiscount);
    model.addAttribute("totalPoint", totalPoint);

    // (5) 장바구니 목록 화면으로 이동
    return "cart/list"; // /templates/cart/list.html
  }

  
  /**
   * ===============================================================
   * [POST] 장바구니 수량 수정
   * ===============================================================
   * URL:
   *   /cart/update_cnt
   *
   * 요청 파라미터:
   *   - cartno (int): 장바구니 항목 번호
   *   - cnt    (int): 변경할 수량
   *
   * 접근 권한:
   *   - 로그인 사용자 (admin, user)
   *   - withdrawn(탈퇴) → 접근 제한
   *
   * 처리 흐름:
   *   1) 로그인 여부 및 회원 등급 확인
   *   2) 수정할 cartno와 cnt를 Map에 담아 CartProc로 전달
   *   3) DB의 해당 장바구니 항목 수량 업데이트
   *   4) 장바구니 목록 페이지로 리다이렉트
   *
   * View:
   *   - redirect:/cart/list
   */  
  @PostMapping("/update_cnt")
  public String update_cnt(HttpSession session, @RequestParam("cartno") int cartno, @RequestParam("cnt") int cnt) {
    // (1) 로그인 및 권한 체크
    Object gradeObj = session.getAttribute("grade");
    String grade = (gradeObj != null) ? gradeObj.toString() : null;
    Integer memberno = (Integer) session.getAttribute("memberno");

    if (grade == null || memberno == null || "withdrawn".equals(grade)) {
      return "redirect:/member/login_cookie_need?url=/cart/list";
    }
    
    // (2) 파라미터 → Map 생성 (MyBatis용)
    Map<String, Object> map = new HashMap<>();
    map.put("cartno", cartno);
    map.put("cnt", cnt);
    
    // (3) DB 업데이트 실행
    cartProc.update_cnt(map);

    // (4) 장바구니 목록 페이지로 리다이렉트
    return "redirect:/cart/list";
  }

  /**
   * ===============================================================
   * [POST] 장바구니 개별 항목 삭제
   * ===============================================================
   * URL:
   *   /cart/delete
   *
   * 요청 파라미터:
   *   - cartno (int): 삭제할 장바구니 항목 번호
   *
   * 접근 권한:
   *   - 로그인 사용자 (admin, user)
   *   - withdrawn(탈퇴) → 접근 제한
   *
   * 처리 흐름:
   *   1) 로그인 및 회원 상태 확인
   *   2) cartProc.delete(cartno) 호출 → DB에서 해당 항목 삭제
   *   3) 장바구니 목록 페이지로 이동
   *
   * View:
   *   - redirect:/cart/list
   */
  @PostMapping("/delete")
  public String delete(HttpSession session, @RequestParam("cartno") int cartno) {
    // (1) 로그인 및 권한 체크
    Object gradeObj = session.getAttribute("grade");
    String grade = (gradeObj != null) ? gradeObj.toString() : null;
    Integer memberno = (Integer) session.getAttribute("memberno");

    if (grade == null || memberno == null || "withdrawn".equals(grade)) {
      return "redirect:/member/login_cookie_need?url=/cart/list";
    }
    
    // (2) 개별 항목 삭제 실행
    cartProc.delete(cartno);
    
    // (3) 장바구니 목록 페이지로 이동
    return "redirect:/cart/list";
  }

  /**
   * ===============================================================
   * [POST] 장바구니 전체 비우기
   * ===============================================================
   * URL:
   *   /cart/delete_all
   *
   * 요청 파라미터:
   *   - 없음 (세션의 memberno 사용)
   *
   * 접근 권한:
   *   - 로그인 사용자 (admin, user)
   *   - withdrawn(탈퇴) → 접근 제한
   *
   * 처리 흐름:
   *   1) 로그인 여부 및 회원 상태 확인
   *   2) 현재 로그인 회원(memberno)의 장바구니 항목 전체 삭제
   *   3) 장바구니 목록 페이지로 이동
   *
   * View:
   *   - redirect:/cart/list
   */
  @PostMapping("/delete_all")
  public String delete_all(HttpSession session) {
    // (1) 로그인 및 권한 체크
    Object gradeObj = session.getAttribute("grade");
    String grade = (gradeObj != null) ? gradeObj.toString() : null;
    Integer memberno = (Integer) session.getAttribute("memberno");

    if (grade == null || memberno == null || "withdrawn".equals(grade)) {
      return "redirect:/member/login_cookie_need?url=/cart/list";
    }
    
    // (2) 현재 회원의 장바구니 전체 삭제
    cartProc.delete_by_memberno(memberno);
    
    // (3) 장바구니 목록 페이지로 이동
    return "redirect:/cart/list";
  }

  /**
   * ===============================================================
   * [POST] 장바구니 선택 상태 변경 (AJAX)
   * ===============================================================
   * URL:
   *   /cart/update_selected
   *
   * 요청 파라미터:
   *   - cartno   (int)    : 선택 상태를 변경할 장바구니 항목 번호
   *   - selected (String) : 선택 여부 ("Y" 또는 "N")
   *
   * 접근 권한:
   *   - 로그인 사용자 (admin, user)
   *   - withdrawn(탈퇴) → 접근 제한
   *
   * 처리 흐름:
   *   1) 세션에서 로그인 상태 및 회원 등급 확인
   *   2) cartno와 selected 값을 Map에 담아 CartProc.update_selected() 호출
   *   3) 처리 결과에 따라 "success" 또는 "fail" 문자열 반환
   *
   * 응답:
   *   - "success" (성공)
   *   - "fail"    (DB 업데이트 실패)
   *   - "unauthorized" (로그인/권한 없음)
   */
  @PostMapping("/update_selected")
  @ResponseBody
  public String update_selected(@RequestParam("cartno") int cartno, @RequestParam("selected") String selected,
      HttpSession session) {
    // (1) 로그인 및 권한 체크
    Object gradeObj = session.getAttribute("grade");
    String grade = (gradeObj != null) ? gradeObj.toString() : null;
    Integer memberno = (Integer) session.getAttribute("memberno");

    if (grade == null || memberno == null || "withdrawn".equals(grade)) {
      return "unauthorized"; // 권한 없음 응답
    }

    // (2) 파라미터 → Map 생성 (MyBatis 전달용)
    Map<String, Object> map = new HashMap<>();
    map.put("cartno", cartno);
    map.put("selected", selected);

    // (3) DB 업데이트 실행
    int updated = cartProc.update_selected(map);

    // (4) 처리 결과 반환
    return (updated == 1) ? "success" : "fail";
  }

  /**
   * ===============================================================
   * [POST] 선택된 장바구니 항목 삭제
   * ===============================================================
   * URL:
   *   /cart/cart/delete_selected
   *
   * 요청 파라미터:
   *   - cartnos (String): 삭제할 장바구니 항목 번호 목록 (쉼표 구분)
   *     예: "12,15,18"
   *
   * 접근 권한:
   *   - 로그인 사용자
   *
   * 처리 흐름:
   *   1) 세션에서 로그인 여부 확인
   *   2) cartnos 문자열을 배열로 변환 후 반복 삭제
   *   3) 장바구니 목록 페이지로 리디렉트
   *
   * View:
   *   - redirect:/cart/list
   */
  @PostMapping("/cart/delete_selected")
  public String deleteSelected(@RequestParam("cartnos") String cartnos, HttpSession session) {
    // (1) 로그인 여부 확인
    int memberno = (int) session.getAttribute("memberno"); // 세션 확인

    // (2) cartnos → 배열 변환 후 반복 삭제
    String[] cartnoArr = cartnos.split(","); // "12,15,18" → ["12","15","18"]
    for (String cartnoStr : cartnoArr) {
      int cartno = Integer.parseInt(cartnoStr);
      cartProc.delete(cartno); // 단건 삭제
    }
    
    // (3) 목록 페이지로 이동
    return "redirect:/cart/list";
  }

  /**
   * ===============================================================
   * [POST] 장바구니 추가 (AJAX)
   * ===============================================================
   * URL:
   *   /cart/add_ajax
   *
   * 요청(JSON):
   *   {
   *     "productsno": 123,  // 상품 번호
   *     "cnt": 2            // 수량
   *   }
   *
   * 응답(JSON):
   *   {
   *     "success": true,                // 처리 성공 여부
   *     "message": "장바구니에 추가되었습니다." // 사용자 메시지
   *   }
   *
   * 접근 권한:
   *   - 로그인 사용자 (admin, user)
   *   - withdrawn, supplier → 제한
   *
   * 처리 흐름:
   *   1) 로그인 및 회원 상태 체크
   *   2) 요청 JSON에서 productsno, cnt 추출
   *   3) CartVO 생성 후 DB 저장
   *   4) 성공/실패 결과 JSON 반환
   */
  @PostMapping("/add_ajax")
  @ResponseBody
  public Map<String, Object> add_ajax(@RequestBody Map<String, Object> payload, HttpSession session) {
    // (1) 응답용 Map 초기화
    Map<String, Object> response = new HashMap<>();

    // (2) 로그인 및 권한 체크
    Integer memberno = (Integer) session.getAttribute("memberno");
    String gradeStr = (String) session.getAttribute("gradeStr"); // ✅ 수정

    if (memberno == null || "withdrawn".equals(gradeStr) || "supplier".equals(gradeStr)) {
      response.put("success", false);
      response.put("message", "로그인이 필요합니다.");
      return response;
    }

    // (3) 요청 데이터 파싱 및 DB 저장
    try {
      int productsno = Integer.parseInt(payload.get("productsno").toString());
      int cnt = Integer.parseInt(payload.get("cnt").toString());

      // CartVO 구성
      CartVO cartVO = new CartVO();
      cartVO.setMemberno(memberno);
      cartVO.setProductsno(productsno);
      cartVO.setCnt(cnt);
      cartVO.setSelected("Y"); // 기본값: 선택

      // DB 등록
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
   * ===============================================================
   * [GET] 로그인한 사용자의 장바구니 아이템 개수 조회 (AJAX)
   * ===============================================================
   * URL:
   *   /cart/count
   *
   * 요청 파라미터:
   *   - (없음) → 세션 정보로 회원 식별
   *
   * 접근 권한:
   *   - 로그인 사용자만 정상 동작
   *   - 비로그인 사용자는 0 반환
   *
   * 처리 흐름:
   *   1) 세션에서 로그인 여부 확인
   *   2) 로그인 상태가 아니면 0 반환
   *   3) 로그인 상태면 DB에서 장바구니 개수 조회
   *   4) 개수를 그대로 반환 (AJAX 응답)
   *
   * 응답:
   *   - int (장바구니 항목 개수)
   *   예:
   *     0 → 비로그인 사용자
   *     3 → 로그인 사용자, 장바구니에 3개 항목 존재
   */
  @GetMapping("/count")
  @ResponseBody
  public int getCartCount(HttpSession session) {
    // (1) 로그인 여부 확인
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return 0; // 비로그인 사용자는 0 반환
    }

    // (2) DB 조회: 해당 회원의 장바구니 개수 반환
    return cartProc.count_by_member(memberno);
  }

}
