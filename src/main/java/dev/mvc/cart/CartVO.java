package dev.mvc.cart;

import java.util.Date;

import dev.mvc.products.ProductsVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CartVO {
  private int cartno;            // 장바구니 번호 (PK)
  private int memberno;          // 회원 번호 (FK)
  private int productsno;        // 상품 번호 (FK)
  private int cnt;               // 수량
  private Date rdate;            // 등록일

  // 출력용 상품 정보 (thumb1, title, saleprice 등)
  private ProductsVO productsVO;
  
  // 'Y'는 구매 예정, 'N'은 장바구니에만 보관
  private String selected = "Y";
  
  //1개 상품에 대한 적립 포인트
  private int point;

}
