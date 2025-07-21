package dev.mvc.productsgood;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dev.mvc.products.ProductsDAOInter;
import dev.mvc.products.ProductsVO;

@Component("dev.mvc.productsgood.ProductsgoodProc")
public class ProductsgoodProc implements ProductsgoodProcInter {
  @Autowired
  ProductsgoodDAOInter productsgoodDAO;
  
  @Autowired
  private ProductsDAOInter productsDAO;

  
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



  @Override
  public ProductsgoodVO readByProductsnoMemberno(int productsno, int memberno) {
    // 파라미터를 Map으로 만들어 DAO 호출
    Map<String, Object> map = new HashMap<>();
    map.put("productsno", productsno);
    map.put("memberno", memberno);
    
    return this.productsgoodDAO.readByProductsnoMemberno(map);
  }

  @Override
  public int deleteByProductsnoMemberno(int productsno, int memberno) {
    // 파라미터를 Map으로 만들어 DAO 호출
    Map<String, Object> map = new HashMap<>();
    map.put("productsno", productsno);
    map.put("memberno", memberno);
    
    return this.productsgoodDAO.deleteByProductsnoMemberno(map);
  }

  @Override
  public ArrayList<ProductsVO> list_user_liked_products(int memberno) {
    return this.productsgoodDAO.list_user_liked_products(memberno);
  }

  @Override
  public ArrayList<ProductsVO> list_supplier_products_liked(int memberno) {
    return this.productsgoodDAO.list_supplier_products_liked(memberno);
  }
  
  @Override
  public List<ProductsVO> getProductsgoodByMember(int memberno) {
      return productsgoodDAO.getProductsgoodByMember(memberno);
  }

  @Override
  public List<ProductsVO> search(String word) {
      return productsDAO.search(word);
  }
}


