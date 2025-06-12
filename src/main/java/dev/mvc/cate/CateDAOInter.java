package dev.mvc.cate;

import java.util.ArrayList;
import java.util.Map;

// MyBATIS 기준으로 추상 메소드를 만들면 Spring Boot가 자동으로 class로 구현함.
public interface CateDAOInter {
  /**
   * <pre>
   * MyBATIS: insert id="create" parameterType="dev.mvc.cate.CateVO"
   * insert: INSERT SQL, 처리후 등록된 레코드 갯수를 리턴
   * id: 자바 메소드명
   * parameterType: MyBATS가 전달받는 VO 객체 타입
   * </pre>
   * @param cateVO
   * @return 등록된 레코드 갯수
   */
  public int create(CateVO cateVO);
  
  /**
   * 전체 목록
   * @return
   */
  public ArrayList<CateVO> list_all();

  /**
   * 검색, 전체 목록
   * @return
   */
  public ArrayList<CateVO> list_search(String word);

  /**
   * 검색, 전체 레코드 갯수, 페이징 버튼 생성시 필요 ★★★★★
   * @return
   */
  public int list_search_count(String word);
  
  /**
   * 조회
   * @param cateno
   * @return
   */
  public CateVO read(int cateno); 
  
  /**
   * 수정
   * @param cateVO
   * @return
   */
  public int update(CateVO cateVO);

  /**
   * 삭제 처리
   * delete id="delete" parameterType="Integer"
   * @param int
   * @return 삭제된 레코드 갯수
   */
  public int delete(int cateno);
  
  /**
   * 우선 순위 높임, 10 등 -> 1 등
   * @param int
   * @return
   */
  public int update_seqno_forward(int cateno);

  /**
   * 우선 순위 낮춤, 1 등 -> 10 등
   * @param int
   * @return
   */
  public int update_seqno_backward(int cateno);
  
  /**
   * 카테고리 공개 설정
   * @param int
   * @return
   */
  public int update_visible_y(int cateno);

  /**
   * 카테고리 비공개 설정
   * @param int
   * @return
   */
  public int update_visible_n(int cateno);

  /**
   * 공개된 대분류만 출력
   * @return
   */
  public ArrayList<CateVO> list_all_grp_y();
  
  /**
   * 특정 그룹의 중분류 출력
   * @return
   */
  public ArrayList<CateVO> list_all_name_y(String grp);

  /**
   * 카테고리 그룹 목록
   * @return
   */
  public ArrayList<String> grpset();

  /**
   * 검색, 전체 목록
   * @return
   */
  public ArrayList<CateVO> list_search_paging(Map map);

  /**
   * 갯수 전달받아 대분류 감소
   * @return
   */
  public int update_cnt_by_cateno(Map<String, Object> map);
  
}






