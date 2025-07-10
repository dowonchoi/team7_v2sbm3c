package dev.mvc.shipments;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/shipment")
public class ShipmentsCont {

  @Autowired
  @Qualifier("dev.mvc.shipments.ShipmentsProc")
  private ShipmentsProcInter shipmentsProc;

  @GetMapping("/list")
  public String list(Model model) {
    List<ShipmentsVO> list = shipmentsProc.list();
    model.addAttribute("list", list);
    return "shipment/list";
  }

  @GetMapping("/read")
  public String read(@RequestParam("shipmentno") int shipmentno, Model model) {
    ShipmentsVO vo = shipmentsProc.read(shipmentno);
    model.addAttribute("vo", vo);
    return "shipment/read";
  }

  @PostMapping("/updateStatus")
  public String updateStatus(ShipmentsVO shipmentVO) {
    shipmentsProc.updateStatus(shipmentVO);
    return "redirect:/shipment/list";
  }

  @GetMapping("/create")
  public String createForm(@RequestParam("orderno") int orderno, Model model) {
    model.addAttribute("orderno", orderno);  // 주문번호를 등록 폼으로 넘김
    return "shipment/create";  // 배송 등록 폼
  }

  @PostMapping("/create")
  public String create(ShipmentsVO shipmentVO) {
    shipmentsProc.create(shipmentVO);
    return "redirect:/shipment/list";
  }

}
