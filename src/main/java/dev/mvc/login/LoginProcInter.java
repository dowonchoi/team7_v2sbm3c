package dev.mvc.login;

import java.util.List;

public interface LoginProcInter {
  
  public int create(LoginVO loginVO);
  
  public List<LoginVO> list();
  
  public List<LoginVO> search(String keyword);
  
  public int searchCount(String keyword);
  
  public List<LoginVO> mylist(String id); // 내 로그인 내역
  
public LoginVO read(int loginno);
  
  public int delete(int loginno);
}
