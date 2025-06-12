package dev.mvc.productsgood;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

//CREATE TABLE productsgood (
//    productsgoodno    NUMBER(10)        NOT NULL,
//    rdate            DATE        NOT NULL,
//    productsno        NUMBER(10)        NOT NULL,
//    memberno        NUMBER(10)        NOT NULL,
//    PRIMARY KEY (productsgoodno),
//    FOREIGN KEY (productsno) REFERENCES products(productsno),
//    FOREIGN KEY (memberno) REFERENCES member(memberno)
//);

@Getter @Setter @ToString
public class ProductsgoodVO {
  /** 컨텐츠 추천 번호 */
  private int productsno;
  
  /** 등록일 */
  private String rdate;
  
  /** 컨텐츠 번호 */
  private int productsgoodno;
  
  /** 회원 번호 */
  private int memberno;
  
}



