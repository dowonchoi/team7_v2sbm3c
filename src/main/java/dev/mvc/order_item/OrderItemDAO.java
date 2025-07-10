package dev.mvc.order_item;

import java.util.List;

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
}
