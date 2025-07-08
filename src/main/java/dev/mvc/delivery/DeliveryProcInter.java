package dev.mvc.delivery;

import java.util.List;

public interface DeliveryProcInter {

  public int create(DeliveryVO deliveryVO);           // 신규 저장
  public int update(DeliveryVO deliveryVO);           // 수정
  public int count_by_memberno(int memberno);         // 존재 여부
  public DeliveryVO read_by_memberno(int memberno);   // 조회

  /**
   * upsert: 배송지 존재하면 update, 없으면 create
   */
  public int upsert(DeliveryVO deliveryVO);
  
  public List<DeliveryVO> list_by_memberno(int memberno);
  
  // 배송지 선택 기능
  public DeliveryVO read(int deliveryno);
  
  /** 배송지 번호로 조회 */
  //public DeliveryVO read_by_deliveryno(int deliveryno);

}
