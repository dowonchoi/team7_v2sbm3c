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
  
  @Override
  public int clear_default(int memberno) {
    return deliveryDAO.clear_default(memberno);
  }

  @Override
  public int update_default(DeliveryVO deliveryVO) {
    return deliveryDAO.update_default(deliveryVO);
  }

  /**
   * 배송지 수정 (deliveryno 기준)
   * - deliveryno로 해당 배송지를 찾아 수정한다.
   */
  @Override
  public int update_by_deliveryno(DeliveryVO deliveryVO) {
    return deliveryDAO.update_by_deliveryno(deliveryVO);
  }
  
  /*
   * 배송지 삭제
   */
  @Override
  public int delete_by_deliveryno(int deliveryno) {
    return deliveryDAO.delete_by_deliveryno(deliveryno);
  }


  
  

}
