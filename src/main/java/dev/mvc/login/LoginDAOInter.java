package dev.mvc.login;

import java.util.List;
import java.util.Map;

public interface LoginDAOInter {
  
  public int create(LoginVO loginVO);  // 로그인 기록 저장
  
  public List<LoginVO> list();         // 로그인 기록 전체 조회
  
  public List<LoginVO> search(Map<String, Object> map); // 검색 조회
  
  public int searchCount(String keyword);              // 검색 결과 수
  
  public List<LoginVO> mylist(String id); // 내 로그인 내역
  
  public LoginVO read(int loginno); // 개별 로그 조회
  
  public int delete(int loginno); // 삭제
}
