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

    int result = deliveryProc.upsert(deliveryVO);

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
  
  // 배송지 선택 기능
  @GetMapping("/delivery/read/{deliveryno}")
  @ResponseBody // JSON 반환
  public DeliveryVO read(@PathVariable("deliveryno") int deliveryno) {
   return this.deliveryProc.read(deliveryno);  // deliveryno로 1건 조회
  }
  
//  @GetMapping("/delivery/read/{deliveryno}")
//  @ResponseBody
//  public DeliveryVO read_by_deliveryno(@PathVariable("deliveryno") int deliveryno) {
//    return this.deliveryProc.read_by_deliveryno(deliveryno);
//  }


  

  
  

  


}
