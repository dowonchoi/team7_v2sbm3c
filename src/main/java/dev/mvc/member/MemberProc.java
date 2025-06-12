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
    int cnt = this.memberDAO.checkID(id);
    return cnt;
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
  public int passwd_check(HashMap<String, Object> map) {
    String passwd = (String)map.get("passwd");
    // map.put("passwd", new Security().aesEncode(passwd));
    map.put("passwd", this.security.aesEncode(passwd));
    int cnt = this.memberDAO.passwd_check(map);
    return cnt;
  }

  @Override
  public int passwd_update(HashMap<String, Object> map) {
    String passwd = (String)map.get("passwd");
    // map.put("passwd", new Security().aesEncode(passwd));
    map.put("passwd", this.security.aesEncode(passwd));
    int cnt = this.memberDAO.passwd_update(map);
    return cnt;
  }
  
  @Override
  public int login(HashMap<String, Object> map) {
    String passwd = (String)map.get("passwd");
    // map.put("passwd", new Security().aesEncode(passwd));
    map.put("passwd", this.security.aesEncode(passwd));
    int cnt = this.memberDAO.login(map);
    
    return cnt;
  }
  
}



