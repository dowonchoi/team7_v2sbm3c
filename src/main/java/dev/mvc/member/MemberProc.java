package dev.mvc.member;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dev.mvc.tool.Security;
 
@Component("dev.mvc.member.MemberProc")
public class MemberProc implements MemberProcInter {
  @Autowired
  private MemberDAOInter memberDAO;
  
  @Autowired
  private Security security;
  
  public MemberProc(){
    // System.out.println("-> MemberProc created.");
  }

  @Override
  public int checkID(String id) {
    return memberDAO.checkID(id);
  }
  
  @Override
  public int checkEmail(String email) {
      return memberDAO.checkEmail(email);
  }

  @Override
  public int create(MemberVO memberVO) {
      String passwd = memberVO.getPasswd();
      String passwd_encoded = security.aesEncode(passwd); // ✅ 암호화
      memberVO.setPasswd(passwd_encoded);

      return memberDAO.create(memberVO); // DB 저장
  }
 
  @Override
  public ArrayList<MemberVO> list() {
    ArrayList<MemberVO> list = this.memberDAO.list();
    return list;
  }
  
  @Override
  public List<MemberVO> list_search(Map<String, Object> map) {
      return memberDAO.list_search(map);
  }
  
  @Override
  public List<MemberVO> list_search_paging(String word, int nowPage, int recordPerPage) {
      int start_num = ((nowPage - 1) * recordPerPage) + 1;
      int end_num = nowPage * recordPerPage;

      Map<String, Object> map = new HashMap<>();
      map.put("word", word);
      map.put("start_num", start_num);
      map.put("end_num", end_num);

      return memberDAO.list_search_paging(map);
  }

  @Override
  public int list_search_count(String word) {
      return memberDAO.list_search_count(word);
  }

  @Override
  public String pagingBox(int nowPage, String word, String listFile, int search_count, int recordPerPage, int pagePerBlock) {
      int totalPage = (int) Math.ceil((double) search_count / recordPerPage);
      int startPage = ((nowPage - 1) / pagePerBlock) * pagePerBlock + 1;
      int endPage = startPage + pagePerBlock - 1;
      endPage = Math.min(endPage, totalPage);

      StringBuffer str = new StringBuffer();

      str.append("<div class='paging'>");
      if (startPage > 1) {
          str.append("<a href='" + listFile + "?now_page=" + (startPage - 1) + "&word=" + word + "'>Prev</a>");
      }

      for (int i = startPage; i <= endPage; i++) {
          if (i == nowPage) {
              str.append("<span class='current'>" + i + "</span>");
          } else {
              str.append("<a href='" + listFile + "?now_page=" + i + "&word=" + word + "'>" + i + "</a>");
          }
      }

      if (endPage < totalPage) {
          str.append("<a href='" + listFile + "?now_page=" + (endPage + 1) + "&word=" + word + "'>Next</a>");
      }
      str.append("</div>");

      return str.toString();
  }
  
  @Override
  public MemberVO read(int memberno) {
    MemberVO memberVO = this.memberDAO.read(memberno);
    return memberVO;
  }

  @Override
  public MemberVO readById(String id) {
    MemberVO memberVO = this.memberDAO.readById(id);
    return memberVO;
  }
  
  @Override
  public int getGrade(int memberno) {
      return memberDAO.getGrade(memberno);
  }

  @Override
  public boolean isMember(HttpSession session){
    boolean sw = false; // 로그인하지 않은 것으로 초기화
    
    if (session.getAttribute("grade") != null) {
      String grade = (String) session.getAttribute("grade");
      if (grade.equals("admin") || grade.equals("supplier") || grade.equals("user")) {
        sw = true;
      }
    }
   
    return sw;
  }

  @Override
  public boolean isAdmin(HttpSession session){
    boolean sw = false; // 로그인하지 않은 것으로 초기화
    
    if (session.getAttribute("grade") != null) {
      if (((String)session.getAttribute("grade")).equals("admin")) {
        sw = true;
      }
    }
    
    return sw;
  }
  
  @Override
  public int update(MemberVO memberVO) {
    int cnt = this.memberDAO.update(memberVO);
    return cnt;
  }
  
  @Override
  public int delete(int memberno) {
    int cnt = this.memberDAO.delete(memberno);
    return cnt;
  }
  
  @Override
  public int hide(int memberno) {
    return this.memberDAO.hide(memberno);
  }
  
  @Override
  public int deleteByAdmin(int memberno) {
      return this.memberDAO.deleteByAdmin(memberno);
  }
  
  @Override
  public int withdraw(int memberno) {
      return this.memberDAO.withdraw(memberno);
  }
  
  @Override
  public List<MemberVO> selectWithdrawnMembers() {
      return memberDAO.selectWithdrawnMembers();
  }
  
  @Override
  public int restoreMember(int memberno, String userType) {
      int gradeStart, gradeEnd;
      if ("supplier".equals(userType)) {
          gradeStart = 5;
          gradeEnd = 15;
      } else {
          gradeStart = 16;
          gradeEnd = 39;
      }

      // 현재 사용 중인 등급 조회
      HashMap<String, Object> gradeMap = new HashMap<>();
      gradeMap.put("gradeStart", gradeStart);
      gradeMap.put("gradeEnd", gradeEnd);
      List<Integer> usedGrades = memberDAO.getUsedGradesInRange(gradeMap);

      // 사용 가능한 가장 작은 등급 찾기
      int assignedGrade = -1;
      for (int i = gradeStart; i <= gradeEnd; i++) {
          if (!usedGrades.contains(i)) {
              assignedGrade = i;
              break;
          }
      }

      // 만약 사용 가능한 등급이 없으면 최대값으로 배정
      if (assignedGrade == -1) {
          assignedGrade = gradeEnd;
      }

      // 복구 쿼리 실행
      HashMap<String, Object> paramMap = new HashMap<>();
      paramMap.put("grade", assignedGrade);
      paramMap.put("memberno", memberno);

      return memberDAO.restoreMember(paramMap);
  }
  
  @Override
  public int passwd_check(HashMap<String, Object> map) {
    String passwd = (String)map.get("passwd");
    // map.put("passwd", new Security().aesEncode(passwd));
    map.put("passwd", this.security.aesEncode(passwd));
    int cnt = this.memberDAO.passwd_check(map);
    return cnt;
  }

  @Override
  public int passwd_update(Map<String, Object> map) {
    String passwd = (String)map.get("passwd");
    // map.put("passwd", new Security().aesEncode(passwd));
    map.put("passwd", this.security.aesEncode(passwd));
    int cnt = this.memberDAO.passwd_update(map);
    return cnt;
  }
  
  @Override
  public MemberVO login(HashMap<String, Object> map) {
      return memberDAO.login(map); // selectOne으로 MemberVO 반환
  }
  
//  @Override
//  public int insertMember(MemberVO memberVO) {
//      // 회원 가입 처리, 사업자 인증 포함
//      String passwd = memberVO.getPasswd();
//      String passwd_encoded = this.security.aesEncode(passwd);
//      memberVO.setPasswd(passwd_encoded);
//      
//      int cnt = this.memberDAO.create(memberVO);
//      return cnt;
//  }
  
  @Override
  public int updateGrade(int memberno, int grade) {
      return memberDAO.updateGrade(memberno, grade);
  }
  
  // 공급자 등급 관리
  @Override
  public List<Integer> getUsedGradesInRange(int start, int end) {
      Map<String, Object> map = new HashMap<>();
      map.put("gradeStart", start);
      map.put("gradeEnd", end);
      return memberDAO.getUsedGradesInRange(map);
  }
  
  @Override
  public List<Integer> getUsedGradesInRange(Map<String, Object> paramMap) {
      return memberDAO.getUsedGradesInRange(paramMap);
  }

  @Override
  public int updateSupplierApproved(Map<String, Object> paramMap) {
      int cnt = this.memberDAO.updateSupplierApproved(paramMap);
      return cnt;
  }

  @Override
  public List<MemberVO> selectPendingSuppliers() {
      List<MemberVO> list = this.memberDAO.selectPendingSuppliers();
      return list;
  }
  
  @Override
  public int updateSupplierRejected(int memberno) {
      // 등급을 소비자(예: 16)로 변경하고 승인 상태를 'N'으로 유지
      int cnt1 = this.memberDAO.updateGrade(memberno, 16);
      int cnt2 = 1;  // supplier_approved = 'N'은 기본값이므로 별도 업데이트 생략 가능
      return (cnt1 + cnt2) / 2;
  }

  @Override
  public int updateSupplierApprovalToPending(int memberno) {
      int cnt1 = this.memberDAO.updateGrade(memberno, 5);  // 대기용 공급자 등급 예: 5
      int cnt2 = this.memberDAO.updateSupplierApprovalToPending(memberno); // 승인 상태 N
      return (cnt1 + cnt2) / 2;
  }
  
  @Override
  public MemberVO findIdByNameAndTel(String mname, String tel) {
      Map<String, Object> map = new HashMap<>();
      map.put("mname", mname);
      map.put("tel", tel);
      return memberDAO.findIdByNameAndTel(map);
  }

  @Override
  public MemberVO findPasswdByIdAndTel(String id, String tel) {
      Map<String, Object> map = new HashMap<>();
      map.put("id", id);
      map.put("tel", tel);
      return memberDAO.findPasswdByIdAndTel(map);
  }

  @Override
  public int updatePasswdById(String id, String passwd) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", id);
    map.put("passwd", passwd);
    return memberDAO.updatePasswdById(map); // 0이 아닌지 확인
  }

  @Override
  public MemberVO findByIdAndTel(String id, String tel) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", id);
    map.put("tel", tel);
    return memberDAO.findPasswdByIdAndTel(map);
  }

  @Override
  public MemberVO findByEmail(String email) {
      return this.memberDAO.findByEmail(email);
  }
  
  //회원의 포인트를 누적 추가
  @Override
  public int addPoint(int memberno, int point) {
      return memberDAO.addPoint(memberno, point);
  }

  //포인트 합계 조회
  @Override
  public int sum_total_point(int memberno) {
      return this.memberDAO.sum_total_point(memberno);
  }

}


