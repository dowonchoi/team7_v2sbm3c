package dev.mvc.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;  // 구현 클래스를 교체하기 쉬운 구조 지원

// import javax.servlet.http.HttpSession; // Spring Boot ~ 2.9
import jakarta.servlet.http.HttpSession; //  Spring Boot 3.0~

public interface MemberProcInter {
  /**
   * 중복 아이디 검사
   * @param id
   * @return 중복 아이디 갯수
   */
  public int checkID(String id);
  
  /**
   * 회원 가입
   * @param memberVO
   * @return
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
  public int passwd_update(HashMap<String, Object> map);
  
  /**
   * 로그인 처리
   */
  public int login(HashMap<String, Object> map);
  
  /**
   * 회원/관리자인지 검사
   * @param session
   * @return
   */
  public boolean isMember(HttpSession session);
  
  /**
   * 관리자인지 검사
   * @param session
   * @return
   */
  public boolean isAdmin(HttpSession session);
  
  /**
   * 공급자 회원 가입 (사업자 인증 포함)
   * @param memberVO
   * @return 등록된 회원 수
   */
  public int insertMember(MemberVO memberVO);
  
  /**
   * 공급자 등급 관리
   * @param paramMap
   * @return
   */
  public List<Integer> getUsedGradesInRange(Map<String, Object> paramMap);

  /**
   * 공급자 승인 처리 (관리자 승인)
   * @param paramMap
   * @return 업데이트된 건수
   */
  public int updateSupplierApproved(Map<String, Object> paramMap);

  /**
   * 승인 대기 중인 공급자 목록 조회
   * @return 공급자 목록
   */
  public List<MemberVO> selectPendingSuppliers();

  /** ✅ 공급자 승인 거절 처리 */
  public int updateSupplierRejected(int memberno);

  /** ✅ 공급자 승인 취소 처리 (대기 상태로 복원) */
  public int updateSupplierApprovalToPending(int memberno);

  public List<Integer> getUsedGradesInRange(int gradeStart, int gradeEnd);
  
}



