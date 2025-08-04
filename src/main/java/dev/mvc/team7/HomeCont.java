package dev.mvc.team7;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import dev.mvc.products.ProductsProcInter;
import dev.mvc.products.ProductsVO;
import dev.mvc.cate.CateProcInter;
import dev.mvc.cate.CateVOMenu;
import dev.mvc.tool.Security;


@Controller
public class HomeCont {
  
  @Autowired
  @Qualifier("dev.mvc.products.ProductsProc")
  private ProductsProcInter productsProc;
  
  @Autowired // Spring이 CateProcInter를 구현한 CateProc 클래스의 객체를 생성하여 할당
  @Qualifier("dev.mvc.cate.CateProc")
  private CateProcInter cateProc;

  @Autowired
  private Security security;
  
  public HomeCont() {
    System.out.println("-> HomeCont created.");
  }
  
  @GetMapping(value="/") // http://localhost:9091
  public String home(Model model) { // 파일명 return
//    if (this.security != null) {
//      System.out.println("-> 객체 고유 코드: " + security.hashCode());
//      System.out.println(security.aesEncode("1234"));
//    }
    
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);
    
    model.addAttribute("word", ""); // 시작페이지는 검색을 하지 않음.
    
    // ✅ 카테고리별 상품 리스트 추가
    List<ProductsVO> vegetableList = productsProc.list_by_cateno_limit(2, 8); // 예: 채소
    List<ProductsVO> fruitList = productsProc.list_by_cateno_limit(3, 8);     // 예: 과일
    List<ProductsVO> meatList = productsProc.list_by_cateno_limit(4, 8);      // 예: 육류
    
    model.addAttribute("vegetableList", vegetableList);
    model.addAttribute("fruitList", fruitList);
    model.addAttribute("meatList", meatList);
    
    return "index"; // /templates/index.html  
  }
  

}