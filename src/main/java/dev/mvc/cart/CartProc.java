package dev.mvc.cart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("dev.mvc.cart.CartProc")
public class CartProc implements CartProcInter {

  @Autowired
  private CartDAOInter cartDAO;

  @Override
  public int create(CartVO cartVO) {
    // 1. 중복 여부 확인
    Map<String, Object> map = new HashMap<>();
    map.put("memberno", cartVO.getMemberno());
    map.put("productsno", cartVO.getProductsno());

    CartVO existing = cartDAO.read_by_memberno_productsno(map);

    if (existing != null) {
      // 2. 이미 있으면 수량 증가
      int newCnt = existing.getCnt() + cartVO.getCnt(); // 기본적으로 1 증가
      map.put("cnt", newCnt);
      map.put("cartno", existing.getCartno());
      return cartDAO.update_cnt(map);
    } else {
      // 3. 없으면 신규 추가
      return cartDAO.create(cartVO);
    }
  }

  @Override
  public ArrayList<CartVO> list_by_memberno(int memberno) {
    return cartDAO.list_by_memberno(memberno);
  }

  @Override
  public CartVO read_by_memberno_productsno(Map<String, Object> map) {
    return cartDAO.read_by_memberno_productsno(map);
  }

  @Override
  public int update_cnt(Map<String, Object> map) {
    return cartDAO.update_cnt(map);
  }

  @Override
  public int delete(int cartno) {
    return cartDAO.delete(cartno);
  }

  @Override
  public int delete_by_memberno(int memberno) {
    return cartDAO.delete_by_memberno(memberno);
  }

  @Override
  public int sum_total_price(int memberno) {
    return cartDAO.sum_total_price(memberno);
  }
  
  @Override
  public int update_selected(Map<String, Object> map) {
    return cartDAO.update_selected(map);
  }

}
