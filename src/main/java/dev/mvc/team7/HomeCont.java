package dev.mvc.team7;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeCont {
  public HomeCont() {
    System.out.println("-> HomeCnont created.");
  }
  
  // http://localhost:9093
  // http://localhost:9093/index.do
  @GetMapping(value={"/", "/index.do"}) 
  public String home() {
    return "index";  // /templates/index.html
  }
  
}
