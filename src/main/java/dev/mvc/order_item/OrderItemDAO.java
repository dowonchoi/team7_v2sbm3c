package dev.mvc.order_item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("dev.mvc.order_item.OrderItemDAO")  // 정확하게 이 이름으로
public class OrderItemDAO implements OrderItemDAOInter {

  @Autowired
  private SqlSessionTemplate sqlSession;

  private static final String NAMESPACE = "dev.mvc.order_item.OrderItemDAOInter";

  @Override
  public int create(OrderItemVO orderItemVO) {
    return sqlSession.insert(NAMESPACE + ".create", orderItemVO);
  }

  @Override
  public List<OrderItemVO> list_by_orderno(int orderno) {
    return sqlSession.selectList(NAMESPACE + ".list_by_orderno", orderno);
  }
  
  // 공급자 전용: 주문번호 + memberno로 해당 공급자의 상품만 조회
  @Override
  public List<OrderItemVO> list_by_orderno_with_memberno(int orderno, int memberno) {
    // 매개변수 2개를 map에 담아서 MyBatis에 전달
    Map<String, Object> map = new HashMap<>();
    map.put("orderno", orderno);
    map.put("memberno", memberno);
    
    return sqlSession.selectList(NAMESPACE + ".list_by_orderno_with_memberno", map);
  }

}
