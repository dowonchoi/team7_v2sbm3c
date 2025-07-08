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
  
  public int clear_default(int memberno);
  public int update_default(DeliveryVO deliveryVO);
  
  /**
   * 배송지 수정 (deliveryno 기준)
   * - 사용자가 수정한 배송지 정보를 DB에 반영한다.
   * @param deliveryVO 수정할 배송지 정보 (deliveryno 포함)
   * @return 처리된 레코드 수 (1이면 성공)
   */
  public int update_by_deliveryno(DeliveryVO deliveryVO);
  
  /**
   * 배송지 삭제
   * @param deliveryno
   * @return
   */
  public int delete_by_deliveryno(int deliveryno);

  


}
