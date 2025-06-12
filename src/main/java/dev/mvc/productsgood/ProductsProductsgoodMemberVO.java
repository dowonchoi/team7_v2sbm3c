package dev.mvc.productsgood;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

//-- 테이블 3개 join
//SELECT r.productsgoodno, r.rdate, r.productsno, c.title as c_title, r.memberno, m.id, m.mname
//FROM products c, productsgood r, member m
//WHERE c.productsno = r.productsno AND r.memberno = m.memberno
//ORDER BY productsgoodno DESC;

@Getter @Setter @ToString
<<<<<<< HEAD:src/main/java/dev/mvc/productsgood/productsproductsgoodMemberVO.java
public class ProductsproductsgoodMemberVO {
=======
public class ProductsProductsgoodMemberVO {
>>>>>>> a472891820da2bc4e8bac7038ec34aacee820cd5:src/main/java/dev/mvc/productsgood/ProductsProductsgoodMemberVO.java
  /** 컨텐츠 추천 번호 */
  private int productsgoodno;
  
  /** 등록일 */
  private String rdate;
  
  /** 컨텐츠 번호 */
  private int productsno;
  
  /** 제목 */
  private String c_title = "";
  
  /** 회원 번호 */
  private int memberno;
  
  /** 아이디(이메일) */
  private String id = "";
  
  /** 회원 성명 */
  private String mname = "";
  
}



