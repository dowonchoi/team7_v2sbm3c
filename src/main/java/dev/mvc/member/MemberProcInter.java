package dev.mvc.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

public interface MemberProcInter {
  
  /** 아이디 중복 체크 */
  public int checkID(String id);
  
  /** 이메일 중복 확인 */
  public int checkEmail(String email);

  /** 회원 가입 */
  public int create(MemberVO memberVO);

  /** 회원 전체 목록 */
  public ArrayList<MemberVO> list();
  
  /** 회원 목록 검색 */
  public List<MemberVO> list_search(Map<String, Object> map);
  
  //검색 + 페이징 목록
  public List<MemberVO> list_search_paging(String word, int nowPage, int recordPerPage);
  
  //검색 레코드 수
  public int list_search_count(String word);
  
  //페이징 박스 생성
  public String pagingBox(int nowPage, String word, String listFile, int search_count, int recordPerPage, int pagePerBlock);

  /** 회원 조회 (memberno 기준) */
  public MemberVO read(int memberno);

  /** 회원 조회 (id 기준) */
  public MemberVO readById(String id);
  
  /** 등급 조회 */
  public int getGrade(int memberno);

  /** 회원 존재 여부 (member인지) */
  public boolean isMember(HttpSession session);

  /** 관리자 여부 체크 */
  public boolean isAdmin(HttpSession session);

  /** 회원 정보 수정 */
  public int update(MemberVO memberVO);

  /** 회원 삭제 (완전 삭제 - 관리자 전용) */
  public int delete(int memberno);

  /** 회원 숨기기 (사용 안함) */
  public int hide(int memberno);

  /** 회원 완전 삭제 (관리자 전용) */
  public int deleteByAdmin(int memberno);

  /** 회원 탈퇴 (등급 변경 방식) */
  public int withdraw(int memberno);
  
  /** 회원 복구 */
  public int restoreMember(int memberno, String userType);

  /** 탈퇴 회원 목록 조회 */
  public List<MemberVO> selectWithdrawnMembers();

  /** 패스워드 확인 */
  public int passwd_check(HashMap<String, Object> map);

  /** 패스워드 수정 */
  public int passwd_update(Map<String, Object> map);

  /** 로그인 처리 */
  public MemberVO login(HashMap<String, Object> map);

  /** 등급 변경 */
  public int updateGrade(int memberno, int grade);

  /** 등급 중 사용 중인 등급 조회 */
  public List<Integer> getUsedGradesInRange(int start, int end);

  /** 등급 중 사용 중인 등급 조회 (Map 방식) */
  public List<Integer> getUsedGradesInRange(Map<String, Object> paramMap);

  /** 공급자 승인 상태 변경 */
  public int updateSupplierApproved(Map<String, Object> paramMap);

  /** 승인 대기 공급자 목록 */
  public List<MemberVO> selectPendingSuppliers();

  /** 공급자 승인 거절 처리 */
  public int updateSupplierRejected(int memberno);

  /** 공급자 승인 대기 상태로 변경 */
  public int updateSupplierApprovalToPending(int memberno);

  /** 아이디 찾기 (이름 + 전화번호) */
  public MemberVO findIdByNameAndTel(String mname, String tel);

  /** 비밀번호 찾기 (아이디 + 전화번호) */
  public MemberVO findPasswdByIdAndTel(String id, String tel);

  /** 비밀번호 변경 (아이디 기준) */
  public int updatePasswdById(String id, String passwd);

  /** 아이디 + 전화번호로 찾기 */
  public MemberVO findByIdAndTel(String id, String tel);

  /** 이메일로 회원 조회 */
  public MemberVO findByEmail(String email);
}
