package dev.mvc.cate;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

//   - 메뉴의 구성
//      제철         신선식품            가공식품                    즉석조리     <- 카테고리 그룹(대분류)
//     └ 채소     └ 과일              └ 견과/건과                ㄴ 반찬       <- 카테고리(중분류)
//     └ 과일     └ 축산/계란       └ 장/소스/드레싱/식초   ㄴ 간편식
//     └ 해산물  └ 수산물/건어물  └ 제과제빵/시리얼
@Getter @Setter
public class CateVOMenu {
  /** 카테고리 그룹(대분류) */
  private String grp;
  
  /** 카테고리(중분류) */
  private ArrayList<CateVO> list_name;
  
}
