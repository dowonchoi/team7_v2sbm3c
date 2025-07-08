package dev.mvc.delivery;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("dev.mvc.delivery.DeliveryProc")
public class DeliveryProc implements DeliveryProcInter {

  @Autowired
  private DeliveryDAOInter deliveryDAO;

  @Override
  public int create(DeliveryVO deliveryVO) {
    return deliveryDAO.create(deliveryVO);
  }

  @Override
  public int update(DeliveryVO deliveryVO) {
    return deliveryDAO.update(deliveryVO);
  }

  @Override
  public int count_by_memberno(int memberno) {
    return deliveryDAO.count_by_memberno(memberno);
  }

  @Override
  public DeliveryVO read_by_memberno(int memberno) {
    return deliveryDAO.read_by_memberno(memberno);
  }

  @Override
  public int upsert(DeliveryVO deliveryVO) {
    int count = deliveryDAO.count_by_memberno(deliveryVO.getMemberno());
    if (count == 0) {
      return deliveryDAO.create(deliveryVO);
    } else {
      return deliveryDAO.update(deliveryVO);
    }
  }
  
  @Override
  public List<DeliveryVO> list_by_memberno(int memberno) {
    return deliveryDAO.list_by_memberno(memberno);
  }
  
  //배송지 선택 기능
  @Override
  public DeliveryVO read(int deliveryno) {
   return this.deliveryDAO.read(deliveryno);
  }
  
//  @Override
//  public DeliveryVO read_by_deliveryno(int deliveryno) {
//    return deliveryDAO.read_by_deliveryno(deliveryno);
//  }

  
  

}
