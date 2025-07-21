package dev.mvc.cart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("dev.mvc.cart.CartProc")
public class CartProc implements CartProcInter {

  @Autowired
  private CartDAOInter cartDAO;
  
  @Autowired
  private CartDAOInter cartDAOInter;  // ✅ 이 필드가 있어야 함
  
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
  
  @Override
  public int sum_total_point(int memberno) {
      int totalPoint = 0;
      
      List<CartVO> cartList = this.cartDAO.list_by_memberno(memberno);

      for (CartVO cartVO : cartList) {
          // 선택된 항목만 계산 ('Y'만 적립 대상)
          if ("Y".equals(cartVO.getSelected())) {
              int point = cartVO.getProductsVO().getPoint();
              int cnt = cartVO.getCnt();
              totalPoint += (point * cnt);
          }
      }

      return totalPoint;
  }
  
  /** 총 상품 정가 계산 */
  @Override
  public int sum_total_price_origin(int memberno){
    return cartDAO.sum_total_price_origin(memberno);
  }

  /** 총 할인액 계산 */
  @Override
  public int sum_total_discount(int memberno) {
    return cartDAO.sum_total_discount(memberno);
  }

  @Override
  public List<CartVO> list_selected_by_memberno(int memberno) {
    // 선택된(selected='Y') 장바구니 항목만 조회하여 리스트 반환
    return cartDAO.list_selected_by_memberno(memberno);
  }

  @Override
  public int total_selected_by_memberno(int memberno) {
    // 선택된 항목들의 할인가 * 수량을 합산한 총액 반환
    return cartDAO.total_selected_by_memberno(memberno);
  }
  
  @Override
  public int delete_selected_by_memberno(int memberno) {
    // 선택된 항목들의 삭제
    return cartDAO.delete_selected_by_memberno(memberno);
  }

  @Override
  public int countItems(int memberno) {
      return cartDAO.countItems(memberno);
  }

  @Override
  public int count_by_member(int memberno) {
      return this.cartDAOInter.count_by_member(memberno);
  }

}