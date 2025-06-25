package dev.mvc.login;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("dev.mvc.login.LoginProc")
public class LoginProc implements LoginProcInter {
  
  @Autowired
  private LoginDAOInter loginDAO;

  @Override
  public int create(LoginVO loginVO) {
      return loginDAO.create(loginVO);
  }

  @Override
  public List<LoginVO> list() {
      return loginDAO.list();
  }

  @Override
  public List<LoginVO> search(String keyword) {
      Map<String, Object> map = new HashMap<>();
      map.put("keyword", keyword);
      return loginDAO.search(map);
  }

  @Override
  public int searchCount(String keyword) {
      return loginDAO.searchCount(keyword);
  }
  
    //🔥 내 로그인 내역 조회
   @Override
   public List<LoginVO> mylist(String id) {
       return loginDAO.mylist(id);
   }
   
   public LoginVO read(int loginno) {
     return loginDAO.read(loginno);
  }
  
  public int delete(int loginno) {
     return loginDAO.delete(loginno);
  }

}
