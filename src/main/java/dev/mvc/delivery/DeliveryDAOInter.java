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
  
  //1. DeliveryDAO 인터페이스에 메서드 선언 필요
  
  public int clear_default(int memberno);   // 기존 기본 배송지 해제
  public int update_default(DeliveryVO vo); // 기본 배송지 설정

  /**
   * 배송지 수정 (deliveryno 기준)
   * @param deliveryVO 수정할 배송지 정보
   * @return 처리된 레코드 수
   */
  public int update_by_deliveryno(DeliveryVO deliveryVO);
  
  /*
   * 배송지 삭제
   */
  public int delete_by_deliveryno(int deliveryno);

  
}
