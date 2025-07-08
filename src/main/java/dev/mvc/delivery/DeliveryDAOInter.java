package dev.mvc.delivery;

import java.util.List;

public interface DeliveryDAOInter {

  /**
   * 배송지 저장: 회원이 1명당 1개만 가질 수 있도록 upsert 구조
   * 동일 memberno가 이미 존재하면 update
   */
  public int create(DeliveryVO deliveryVO);     // 최초 insert
  public int update(DeliveryVO deliveryVO);     // update
  public int count_by_memberno(int memberno);   // 기존 배송지 존재 여부
  public DeliveryVO read_by_memberno(int memberno); // 배송지 읽기
  public List<DeliveryVO> list_by_memberno(int memberno);

  //배송지 선택 기능
  public DeliveryVO read(int deliveryno);
  
  /** 배송지 번호로 조회 */
  //public DeliveryVO read_by_deliveryno(int deliveryno);
}
