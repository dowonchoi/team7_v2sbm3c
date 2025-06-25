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
  public int create(MemberVO memberVO) {
    String passwd = memberVO.getPasswd();
//    Security security = new Security(); // dev.mvc.tool
//    String passwd_encoded = security.aesEncode(passwd); // 암호화
    // map.put("passwd", new Security().aesEncode(passwd));
    String passwd_encoded = this.security.aesEncode(passwd);
    memberVO.setPasswd(passwd_encoded); // 패스워드 저장
    
    // memberVO.setPasswd(new Security().aesEncode(memberVO.getPasswd())); // 단축형
    
    int cnt = this.memberDAO.create(memberVO);  // 가입
    return cnt;
  }
 
  @Override
  public ArrayList<MemberVO> list() {
    ArrayList<MemberVO> list = this.memberDAO.list();
    return list;
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
  public boolean isMember(HttpSession session){
    boolean sw = false; // 로그인하지 않은 것으로 초기화
    
    if (session.getAttribute("grade") != null) {
      if (((String)session.getAttribute("grade")).equals("admin") ||
          ((String)session.getAttribute("grade")).equals("member")) {
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
  
  @Override
  public int insertMember(MemberVO memberVO) {
      // 회원 가입 처리, 사업자 인증 포함
      String passwd = memberVO.getPasswd();
      String passwd_encoded = this.security.aesEncode(passwd);
      memberVO.setPasswd(passwd_encoded);
      
      int cnt = this.memberDAO.create(memberVO);
      return cnt;
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
  
}


