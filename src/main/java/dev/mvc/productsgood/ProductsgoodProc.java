package dev.mvc.productsgood;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("dev.mvc.productsgood.ProductsgoodProc")
public class ProductsgoodProc implements ProductsgoodProcInter {
  @Autowired
  ProductsgoodDAOInter productsgoodDAO;
  
  @Override
  public int create(ProductsgoodVO productsgoodVO) {
    int cnt = this.productsgoodDAO.create(productsgoodVO);
    return cnt;
  }

  @Override
  public ArrayList<ProductsgoodVO> list_all() {
    ArrayList<ProductsgoodVO> list = this.productsgoodDAO.list_all();
    return list;
  }

  @Override
  public ProductsgoodVO read(int productsgoodno) {
    ProductsgoodVO productsgoodVO = this.productsgoodDAO.read(productsgoodno);
    return productsgoodVO;
  }
  
  @Override
  public int delete(int productsgoodno) {
    int cnt = this.productsgoodDAO.delete(productsgoodno);
    return cnt;
  }

  @Override
  public int hartCnt(HashMap<String, Object> map) {
    int cnt = this.productsgoodDAO.hartCnt(map);
    return cnt;
  }

  @Override
  public ProductsgoodVO readByProductsnoMemberno(HashMap<String, Object> map) {
    ProductsgoodVO productsgoodVO = this.productsgoodDAO.readByproductsnoMemberno(map);
    return productsgoodVO;
  }

  @Override
  public ArrayList<ProductsproductsgoodMemberVO> list_all_join() {
    ArrayList<ProductsproductsgoodMemberVO> list = this.productsgoodDAO.list_all_join();
    return list;
  }

}



