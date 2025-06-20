package dev.mvc.member;

import java.util.ArrayList;
import java.util.HashMap;  // class
import java.util.List;
// interface, 인터페이스를 사용하는 이유는 다른 형태의 구현 클래스로 변경시 소스 변경이 거의 발생 안됨
// 예) 2022년 세금 계산 방법 구현 class, 2023년 세금 계산 방법 구현 class
// 인터페이스 = 구현 클래스
// Payend pay = new Payend2022();
// Payend pay = new Payend2023();
// Payend pay = new Payend2024();
// pay.calc();
import java.util.Map;         

public interface MemberDAOInter {
  /**
   * 중복 아이디 검사
   * @param id
   * @return 중복 아이디 갯수
   */
  public int checkID(String id);
  
  /**
   * 회원 가입
   * @param memberVO
   * @return 추가한 레코드 갯수
   */
  public int create(MemberVO memberVO);

  /**
   * 회원 전체 목록
   * @return
   */
  public ArrayList<MemberVO> list();

  /**
   * memberno로 회원 정보 조회
   * @param memberno
   * @return
   */
  public MemberVO read(int memberno);
  
  /**
   * id로 회원 정보 조회
   * @param id
   * @return
   */
  public MemberVO readById(String id);

  /**
   * 수정 처리
   * @param memberVO
   * @return
   */
  public int update(MemberVO memberVO);
 
  /**
   * 회원 삭제 처리
   * @param memberno
   * @return
   */
  public int delete(int memberno);

  public int hide(int memberno);

  /**
   * 현재 패스워드 검사
   * @param map
   * @return 0: 일치하지 않음, 1: 일치함
   */
  public int passwd_check(HashMap<String, Object> map);
  
  /**
   * 패스워드 변경
   * @param map
   * @return 변경된 패스워드 갯수
   */
  public int passwd_update(Map<String, Object> map);
  
  /**
   * 로그인 처리
   */
  public MemberVO login(HashMap<String, Object> map);
  
  /**
   * 공급자 회원 가입 (사업자 인증 파일 포함)
   * @param memberVO
   * @return 등록된 레코드 수
   */
  public int insertMember(MemberVO memberVO);
  
  /**
   * 공급자 등급
   * @param paramMap
   * @return
   */
  public List<Integer> getUsedGradesInRange(Map<String, Object> paramMap);

  /**
   * 공급자 승인 처리 (관리자 승인)
   * @param paramMap
   * @return 업데이트된 레코드 수
   */
  public int updateSupplierApproved(Map<String, Object> paramMap);

  /**
   * 승인 대기 중인 공급자 목록 조회
   * @return 승인 대기 공급자 리스트
   */
  public List<MemberVO> selectPendingSuppliers();
  
  // 등급 변경
  public int updateGrade(int memberno, int grade);
  
  /** ✅ 공급자 승인 거절 처리 */
  public int updateSupplierRejected(int memberno);

  /** ✅ 공급자 승인 취소 처리 (대기 상태로 복원) */
  public int updateSupplierApprovalToPending(int memberno);
  
//  /** 모든 공급자 조회 (승인 여부 무관) */
//  public List<MemberVO> selectAllSuppliers();
  
  /**
   * 이름 + 전화번호로 아이디 찾기
   * @param map
   * @return 해당 조건에 맞는 회원정보
   */
  public MemberVO findIdByNameAndTel(Map<String, Object> map);

  /**
   * 아이디 + 전화번호로 비밀번호 찾기
   * @param map
   * @return 해당 조건에 맞는 회원정보
   */
  public MemberVO findPasswdByIdAndTel(Map<String, Object> map);
  
  /**
   * 이메일 기준으로 비밀번호 변경
   * @param map (id, passwd)
   * @return 변경된 레코드 수
   */
  public int updatePasswdById(Map<String, Object> map);
  
  /**
   * 아이디 + 전화번호로 회원 조회
   * @param map (id, tel)
   * @return 일치하는 회원 정보
   */
  public MemberVO findByIdAndTel(Map<String, Object> map);
  
  public MemberVO findByEmail(String email);

}