package dev.mvc.delivery;

import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/delivery")
public class DeliveryCont {

  @Autowired
  @Qualifier("dev.mvc.delivery.DeliveryProc")  // 정확한 Bean 명시
  private DeliveryProcInter deliveryProc;

  /**
   * 기본 배송지 저장 요청 처리
   * - JSON POST 형식으로 들어온 배송지 데이터를 DB에 저장 (upsert)
   * - 비로그인 상태면 "not_logged_in" 반환
   */
  @PostMapping("/save_default")
  @ResponseBody
  public String saveDefaultDelivery(@RequestBody DeliveryVO deliveryVO, HttpSession session) {
    Integer memberno = (Integer) session.getAttribute("memberno");

    if (memberno == null) {
      return "not_logged_in";
    }

    deliveryVO.setMemberno(memberno);

    // 1. 먼저 기존 기본 배송지를 모두 해제
    deliveryProc.clear_default(memberno);

    // 2. 현재 배송지를 기본으로 설정 (is_default = 'Y'로 업데이트)
    deliveryVO.setIs_default("Y");
    int result = deliveryProc.update_default(deliveryVO);

    return result == 1 ? "success" : "fail";
  }

  
  @GetMapping(value = "/list", produces = "application/json;charset=UTF-8")
  @ResponseBody
  public Object list(HttpSession session) {
    Integer memberno = (Integer) session.getAttribute("memberno");

    if (memberno == null) {
      Map<String, Object> map = new HashMap<>();
      map.put("status", "not_logged_in");
      return map;
    }

    return deliveryProc.list_by_memberno(memberno);
  }
  
   // 배송지 선택
  @PostMapping("/create")
  @ResponseBody
  public String create(@RequestBody DeliveryVO deliveryVO, HttpSession session) {
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) return "not_logged_in";

    deliveryVO.setMemberno(memberno);
    int cnt = deliveryProc.create(deliveryVO);
    return cnt == 1 ? "success" : "fail";
  }
    
  //배송지 선택 기능
  @GetMapping("/read/{deliveryno}")
  @ResponseBody
  public Object read(@PathVariable("deliveryno") int deliveryno, HttpSession session) {
   Integer memberno = (Integer) session.getAttribute("memberno");
   if (memberno == null) return "not_logged_in";
  
   DeliveryVO vo = this.deliveryProc.read(deliveryno);
  
   // 본인의 배송지가 아닌 경우 차단
   if (vo == null || vo.getMemberno() != memberno) {
     return "unauthorized";
   }
  
   return vo;
  }

  
  /**
   * 배송지 수정 요청 처리
   * - deliveryno를 기준으로 해당 배송지 정보 수정
   * - 로그인 상태 확인
   * - 수정 성공 시 "success", 실패 시 "fail"
   */
  @PostMapping("/update")
  @ResponseBody
  public String update(@RequestBody DeliveryVO deliveryVO, HttpSession session) {
    // 세션에서 로그인된 사용자 번호 가져오기
    Integer memberno = (Integer) session.getAttribute("memberno");

    // 로그인되지 않은 경우
    if (memberno == null) {
      return "not_logged_in";
    }

    // 해당 배송지가 본인의 것인지 확인하기 위해 memberno 세팅
    deliveryVO.setMemberno(memberno);

    // 실제 수정 로직 실행 (deliveryno를 기준으로)
    int result = deliveryProc.update_by_deliveryno(deliveryVO);

    // 결과 반환
    return result == 1 ? "success" : "fail";
  }

  /**
   * 배송지 삭제 요청
   * - JSON { deliveryno } 전달받아 해당 배송지를 삭제
   * - 본인 확인은 생략하거나 확장 가능
   */
  @PostMapping("/delete")
  @ResponseBody
  public String delete(@RequestBody DeliveryVO deliveryVO, HttpSession session) {
    Integer memberno = (Integer) session.getAttribute("memberno");

    if (memberno == null) return "not_logged_in";

    deliveryVO.setMemberno(memberno); // 확장 대비

    int result = deliveryProc.delete_by_deliveryno(deliveryVO.getDeliveryno());
    return result == 1 ? "success" : "fail";
  }



  

  
  

  


}
