package dev.mvc.cate;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dev.mvc.products.ProductsProcInter;
import dev.mvc.member.MemberProc;
import dev.mvc.tool.Tool;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Controller
@RequestMapping("/cate")
public class CateCont {
  @Autowired // Spring이 CateProcInter를 구현한 CateProc 클래스의 객체를 생성하여 할당
  @Qualifier("dev.mvc.cate.CateProc")
  private CateProcInter cateProc;

  @Autowired 
  @Qualifier("dev.mvc.member.MemberProc")
  private MemberProc memberProc;
  
  @Autowired
  @Qualifier("dev.mvc.products.ProductsProc")
  private ProductsProcInter productsProc;  

  public CateCont() {
    System.out.println("-> CateCont created.");
  }

  @PostConstruct
  public void init() {
    System.out.println("-> CateCont created.");
    cateProc.updateMidCnt();   // 중분류 cnt 갱신
    cateProc.updateMainCnt();  // 대분류 cnt 갱신
    System.out.println("→ 자동 카테고리 cnt 동기화 완료");
  }

  /** 한 페이지당 표시할 카테고리 수 (페이징에 사용됨) */
  /** 페이지당 출력할 레코드 갯수, nowPage는 1부터 시작 */
  public int record_per_page = 7;

  /** 한 번에 보여줄 페이징 블록 수 */
  /** 블럭당 페이지 수, 하나의 블럭은 10개의 페이지로 구성됨 */
  public int page_per_block = 10;
  
  /** 페이징이 적용된 목록 페이지 URL */
  /** 페이징 목록 주소, @GetMapping(value="/list_search") */
  private String list_url = "/cate/list_search";
  
//  @GetMapping(value="/create")  // http://localhost:9091/cate/create
//  @ResponseBody
//  public String create() {
//    System.out.println("-> http://localhost:9091/cate/create");
//    return "<h2>Create test</h2>";
//  }

  /**
   * 등록폼
   * // http://localhost:9091/cate/create
   * // http://localhost:9091/cate/create/  X
   * @return
   */
  @GetMapping(value="/create")  
  public String create(@ModelAttribute("cateVO") CateVO cateVO) {
    cateVO.setGrp("제철/신선식품/가공식품/즉석조리...");
    cateVO.setName("채소");
    
    return "cate/create"; // /templates/cate/create.html
  }

  /**
   * 등록 처리
   * Model model: controller -> html로 데이터 전송 제공
   * @Valid: @NotEmpty, @Size, @NotNull, @Min, @Max, @Pattern... 규칙 위반 검사 지원
   * CateVO cateVO: FORM 태그의 값 자동 저장, Integer.parseInt(request.getParameter("seqno")) 자동 실행
   * BindingResult bindingResult: @Valid의 결과 저장
   * @param model
   * @return
   */
  @PostMapping(value="/create")
  public String create(Model model, 
                              @Valid CateVO cateVO, 
                              BindingResult bindingResult,
                              @RequestParam(name="word", defaultValue = "") String word,
                              RedirectAttributes ra) {
    // System.out.println("-> create post");
    // 유효성 검사 실패 시 등록폼 다시 보여줌
    if (bindingResult.hasErrors() == true) {
      return "cate/create"; // /templates/cate/create.html 
    }
    
    // System.out.println("-> cateVO.getName(): " + cateVO.getName());
    // System.out.println("-> cateVO.getSeqno(): " + cateVO.getSeqno());
    // DB에 카테고리 등록 요청
    int cnt = this.cateProc.create(cateVO);
    // System.out.println("-> cnt: " + cnt);
    
    if (cnt == 1) {
   // 성공 시 검색어 유지하며 목록 페이지로 이동
//      model.addAttribute("code", Tool.CREATE_SUCCESS);  
//      model.addAttribute("name", cateVO.getName());
      ra.addAttribute("word", word);
      // return "redirect:/cate/list_all"; // @GetMapping(value="/list_all") 호출
      return "redirect:/cate/list_search"; // @GetMapping(value="/list_search") 호출
    } else {
      model.addAttribute("code", Tool.CREATE_FAIL); 
    }
    
    model.addAttribute("cnt", cnt);
    
    return "cate/msg";  // /templates/cate/msg.html
  }
  
  /**
   * 전체 목록
   * http://localhost:9091/cate/list_all
   * @param model
   * @return
   */
  @GetMapping(value="/list_all")
  public String list_all(Model model, @ModelAttribute("cateVO") CateVO cateVO) {
    // 빈 검색 조건 세팅
    cateVO.setGrp("");
    cateVO.setName("");
    // 모든 카테고리 목록 가져오기
    ArrayList<CateVO> list = this.cateProc.list_all();
    model.addAttribute("list", list);
    
    // 2단 메뉴: 대분류에 해당하는 중분류 목록 구조
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);
    
    // 카테고리 그룹 목록
    // 등록폼 내에 보여줄 그룹 목록 생성
    ArrayList<String> grpset = this.cateProc.grpset();
    cateVO.setGrp(String.join("/",  grpset)); // 슬래시로 구분된 문자열로 만들어 보여줌
    // System.out.println("-> cateVO.getGrp(): " + cateVO.getGrp());
    
    return "cate/list_all"; // /templates/cate/list_all.html
  }

//  /**
//   * 전체 목록
//   * http://localhost:9091/cate/list_search
//   * @param model
//   * @return
//   */
//  @GetMapping(value="/list_search")
//  public String list_search(Model model, 
//                                    @ModelAttribute("cateVO") CateVO cateVO,
//                                    @RequestParam(name="word", defaultValue = "") String word) {
//    cateVO.setGrp("");
//    cateVO.setName("");
//    
//    ArrayList<CateVO> list = this.cateProc.list_search(word);
//    model.addAttribute("list", list);
//    
//    // 2단 메뉴
//    ArrayList<CateVOMenu> menu = this.cateProc.menu();
//    model.addAttribute("menu", menu);
//    
//    // 카테고리 그룹 목록
//    ArrayList<String> grpset = this.cateProc.grpset();
//    cateVO.setGrp(String.join("/",  grpset));
//    System.out.println("-> cateVO.getGrp(): " + cateVO.getGrp());
//    
//    model.addAttribute("word", word);
//    
//    int list_search_count = list.size(); // 검색된 레코드 갯수
//    model.addAttribute("list_search_count", list_search_count);
//    
//    return "cate/list_search"; // /templates/cate/list_search.html
//  }
  
  /**
   * 등록 폼 및 검색 목록 + 페이징
   * http://localhost:9091/cate/list_search
   * http://localhost:9091/cate/list_search?word=&now_page=
   * http://localhost:9091/cate/list_search?word=까페&now_page=1
   * @param model
   * @return
   */
  @GetMapping(value="/list_search") 
  public String list_search_paging(HttpSession session, Model model, 
                                   @RequestParam(name="word", defaultValue = "") String word,
                                   @RequestParam(name="now_page", defaultValue="1") int now_page) {
    // 관리자만 접근 가능하도록 제한
    if (this.memberProc.isAdmin(session)) {
      // 1. 등록 폼에서 사용할 기본값 준비
      CateVO cateVO = new CateVO(); // form 초기값 전달
      // cateVO.setGenre("분류");
      // cateVO.setName("카테고리 이름을 입력하세요."); // Form으로 초기값을 전달
      
      // 카테고리 그룹 목록
      // 2. 등록 폼에 보여줄 그룹 목록 생성 → "과일/채소/음료" 형태로 표시
      ArrayList<String> list_grp = this.cateProc.grpset(); // 중복 없이 그룹 목록만 가져오기
      cateVO.setGrp(String.join("/", list_grp)); // 슬래시로 구분된 문자열로 변환
      model.addAttribute("cateVO", cateVO); // 등록폼 카테고리 그룹 초기값
      
      // 3. 검색어 null 방지 처리
      word = Tool.checkNull(word);             // null -> ""
      
      // 4. 해당 페이지의 검색 결과 목록 가져오기
      ArrayList<CateVO> list = this.cateProc.list_search_paging(word, now_page, this.record_per_page);
      model.addAttribute("list", list); // 템플릿에서 반복 출력용
      
//      ArrayList<CateVO> menu = this.cateProc.list_all_categrp_y();
//      model.addAttribute("menu", menu);

      // 5. 메뉴 구성 (2단 메뉴용, 대분류 + 중분류 형태)
      ArrayList<CateVOMenu> menu = this.cateProc.menu();
      model.addAttribute("menu", menu);
      
      // 6. 검색 결과 개수 표시
      int search_cnt = list.size(); // 현재 페이지에 검색된 카테고리 수
      model.addAttribute("search_cnt", search_cnt);    
      
      // 7. 검색어 유지 (뷰에서 다시 표시하기 위해)
      model.addAttribute("word", word); // 검색어
      
      // --------------------------------------------------------------------------------------
      // 8. 페이지 번호 목록 생성
      // --------------------------------------------------------------------------------------
      int search_count = this.cateProc.list_search_count(word); // 전체 검색 결과 수
      // 페이징 HTML 코드 생성 ( 1 2 3 )
      model.addAttribute("list_search_count", search_count);
      String paging = this.cateProc.pagingBox(now_page, word, this.list_url, search_count, this.record_per_page, this.page_per_block);
      model.addAttribute("paging", paging); // 템플릿에 전달
      model.addAttribute("now_page", now_page); // 현재 페이지 번호 유지
      
      // 일련 변호 생성: 레코드 갯수 - ((현재 페이지수 -1) * 페이지당 레코드 수)
      int no = search_count - ((now_page - 1) * this.record_per_page);
      model.addAttribute("no", no); // 예: 총 35건 중 1페이지 시작이면 35 → 29 → 28 ...
      // System.out.println("-> no: " + no);
      // --------------------------------------------------------------------------------------    
      
      return "cate/list_search";  // /templates/cate/list_search.html
    } else {
      // 비로그인 상태 혹은 관리자 권한이 없으면 로그인 페이지로 이동
      return "redirect:/member/login_cookie_need?url=/cate/list_search"; // redirect
    }
    

  }  
  
  /**
   * 조회
   * 특정 카테고리(cateno)의 상세 정보 조회 + 검색 결과 목록 + 페이징 출력
   * http://localhost:9091/cate/read/1
   * @param model
   * @return
   */
  @GetMapping(value="/read/{cateno}")
  public String read (Model model, @PathVariable("cateno") Integer cateno,
                             @RequestParam(name="word", defaultValue = "") String word,
                             @RequestParam(name="now_page", defaultValue="1") int now_page) {
    // System.out.println("-> read cateno: " + cateno);
    // 1. cateno에 해당하는 카테고리 정보 1건 조회
    CateVO cateVO = this.cateProc.read(cateno);
    model.addAttribute("cateVO", cateVO); // 뷰에 전달 (상세정보 출력용)
    
//    ArrayList<CateVO> list = this.cateProc.list_all();
//    model.addAttribute("list", list);
    
    // 2. 2단 메뉴 구성 (대분류-중분류 구조)
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);
    
    // 카테고리 그룹 목록
    ArrayList<String> grpset = this.cateProc.grpset();
    cateVO.setGrp(String.join("/",  grpset));
//    System.out.println("-> cateVO.getGrp(): " + cateVO.getGrp());
    
    // 3. 검색어 유지 처리 (null 방지)
    model.addAttribute("word", word);
    // System.out.println("-> word null 체크: " + word);

    // 4. 현재 검색어로 페이징된 카테고리 목록 가져오기
    // ArrayList<CateVO> list = this.cateProc.list_search(word);  // 검색 목록
    ArrayList<CateVO> list = this.cateProc.list_search_paging(word, now_page, this.record_per_page); // 검색 목록 + 페이징
    model.addAttribute("list", list);
    
    // 5. 검색된 카테고리 개수 (총 몇 건 검색되었는지)
    int list_search_count = this.cateProc.list_search_count(word); // 검색된 레코드 갯수
    model.addAttribute("list_search_count", list_search_count); // 템플릿에서 "총 n건" 표시용
    
    // --------------------------------------------------------------------------------------
    // 6. 페이지 번호 목록 생성
    // --------------------------------------------------------------------------------------
    int search_count = this.cateProc.list_search_count(word); // 위와 동일한 값
    String paging = this.cateProc.pagingBox(now_page, word, this.list_url, search_count, this.record_per_page, this.page_per_block);
    model.addAttribute("paging", paging); // 페이지 번호 출력용 HTML
    model.addAttribute("now_page", now_page); // 현재 페이지 번호 전달
    
    // 7. 현재 페이지에서 시작하는 번호 계산
    // 일련 변호 생성: 레코드 갯수 - ((현재 페이지수 -1) * 페이지당 레코드 수)
    int no = search_count - ((now_page - 1) * this.record_per_page); 
    model.addAttribute("no", no); // 템플릿에서 일련번호로 사용
    // System.out.println("-> no: " + no);    
    // --------------------------------------------------------------------------------------    
    
    // 8. 최종적으로 read.html 템플릿을 열어 모든 데이터 전달
    return "cate/read"; // /templates/cate/read.html
  }
  
  /**
   * 수정폼
   * 지정된 cateno의 데이터를 불러와서 입력 폼에 표시
   * 관리자만 접근 가능 / 공급자도 수정가능하게 해야 함
   * 검색 목록 + 페이징 정보도 같이 전달
   * http://localhost:9091/cate/update/1
   * @param model
   * @return
   */
  @GetMapping(value="/update/{cateno}")
  public String update(Model model, HttpSession session,
                               @PathVariable("cateno") Integer cateno,
                               @RequestParam(name="word", defaultValue = "") String word,
                               @RequestParam(name="now_page", defaultValue="1") int now_page) {
    // System.out.println("-> read cateno: " + cateno);

    if (this.memberProc.isAdmin(session)) {
      // 1. 수정 대상 카테고리 정보 불러오기
      CateVO cateVO = this.cateProc.read(cateno);
      model.addAttribute("cateVO", cateVO);
      
      // 2단 메뉴 대분류-중분류 메뉴 구조 불러오기
      ArrayList<CateVOMenu> menu = this.cateProc.menu();
      model.addAttribute("menu", menu);
      
      // 3. 검색어 및 현재 페이지 유지
      model.addAttribute("word", word);
      // System.out.println("-> word null 체크: " + word);

      // ArrayList<CateVO> list = this.cateProc.list_search(word);  // 검색 목록
      // 4. 검색 결과 목록 불러오기 (페이징 포함)
      ArrayList<CateVO> list = this.cateProc.list_search_paging(word, now_page, this.record_per_page); // 검색 목록 + 페이징
      model.addAttribute("list", list);
      
      // 5. 검색 결과 개수
      int list_search_count = this.cateProc.list_search_count(word); // 검색된 레코드 갯수
      model.addAttribute("list_search_count", list_search_count);
      
      // --------------------------------------------------------------------------------------
      // 6. 페이지 번호 목록 생성
      // --------------------------------------------------------------------------------------
      int search_count = this.cateProc.list_search_count(word);
      String paging = this.cateProc.pagingBox(now_page, word, this.list_url, search_count, this.record_per_page, this.page_per_block);
      model.addAttribute("paging", paging);
      model.addAttribute("now_page", now_page);
      
      // 7. 목록 번호 계산
      // 일련 변호 생성: 레코드 갯수 - ((현재 페이지수 -1) * 페이지당 레코드 수)
      int no = search_count - ((now_page - 1) * this.record_per_page);
      model.addAttribute("no", no);
      // System.out.println("-> no: " + no);    
      // --------------------------------------------------------------------------------------  
      
      // 8. 수정 페이지 이동       
      return "cate/update"; // /templates/cate/update.html
    } else {
      // 비관리자 접근 시 로그인 필요 페이지로 이동
      return "redirect:/member/login_cookie_need"; // redirect
    }
    

  }
  
  /**
   * 수정 처리
   * Model model: controller -> html로 데이터 전송 제공
   * @Valid: @NotEmpty, @Size, @NotNull, @Min, @Max, @Pattern... 규칙 위반 검사 지원
   * CateVO cateVO: FORM 태그의 값 자동 저장, Integer.parseInt(request.getParameter("seqno")) 자동 실행
   * BindingResult bindingResult: @Valid의 결과 저장
   * @param model
   * @return
   */
  @PostMapping(value="/update")
  public String update_process(Model model, 
                               @Valid CateVO cateVO, 
                               BindingResult bindingResult,
                               @RequestParam(name="word", defaultValue = "") String word,
                               @RequestParam(name="now_page", defaultValue="1") int now_page,
                               RedirectAttributes ra) {
    // System.out.println("-> update_process");
    // 1. 유효성 검사 실패 시 다시 수정 폼으로 돌아감
    if (bindingResult.hasErrors() == true) {
      return "cate/update"; // /templates/cate/update.html 
    }
    
    // System.out.println("-> cateVO.getName(): " + cateVO.getName());
    // System.out.println("-> cateVO.getSeqno(): " + cateVO.getSeqno());
    // 2. 실제 수정 로직 수행 (DB update)
    int cnt = this.cateProc.update(cateVO);
    // System.out.println("-> cnt: " + cnt); // 성공 시 1, 실패 시 0
    
    if (cnt == 1) {
      // 3. 성공 → 검색어 유지하며 수정폼으로 다시 이동 (POST-REDIRECT-GET 패턴)
//      model.addAttribute("code", Tool.UPDATE_SUCCESS);  
//      model.addAttribute("name", cateVO.getName());
      // System.out.println("-> redirect:/cate/update/" + cateVO.getCateno() + "?word=" + word);
      // http://localhost:9091/cate/update/33?word=영화
      // return "redirect:/cate/update/" + cateVO.getCateno() + "?word=" + word; // @GetMapping(value="/update") // 한글 깨짐, X
      ra.addAttribute("word", word); // redirect로 데이터 전송, 한글 깨짐 방지, redirect 시 검색어 유지 (URL에 자동 포함됨)
      return "redirect:/cate/update/" + cateVO.getCateno(); // 다시 수정폼으로 이동
    } else {
      // 4. 실패 → 메시지 페이지로 이동
      model.addAttribute("code", Tool.UPDATE_FAIL); 
    }
    
    model.addAttribute("cnt", cnt);
    
    return "cate/msg";  // /templates/cate/msg.html
  }

  /**
   * 삭제폼
   * 삭제 전 확인을 위한 화면
   * 현재 선택된 카테고리 정보, 관련 상품 수, 검색 결과 및 페이징 정보도 함께 표시
   * http://localhost:9091/cate/delete/1
   * @param model
   * @return
   */
  @GetMapping(value="/delete/{cateno}")
  public String delete(Model model, 
                              @PathVariable("cateno") Integer cateno,
                              @RequestParam(name="word", defaultValue = "") String word,
                              @RequestParam(name="now_page", defaultValue="1") int now_page) {
    // System.out.println("-> read cateno: " + cateno);
    // 1. 삭제할 카테고리 정보 조회 (삭제 확인용)
    CateVO cateVO = this.cateProc.read(cateno);
    model.addAttribute("cateVO", cateVO);
    
    // 2. 검색된 목록 출력 (삭제폼에서도 목록 보여줌)
    // ArrayList<CateVO> list = this.cateProc.list_all();
    // ArrayList<CateVO> list = this.cateProc.list_search(word);
    ArrayList<CateVO> list = this.cateProc.list_search_paging(word, now_page, this.record_per_page);
    // System.out.println("-> delete form list.size(): " + list.size());
    model.addAttribute("list", list);
    
    // 3.  2단 메뉴 (대분류/중분류)
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);
    
    // 4. 검색어 및 페이징 정보 전달
    model.addAttribute("word", word);
    
    // --------------------------------------------------------------------------------------
    // 5. 페이지 번호 목록 생성
    // --------------------------------------------------------------------------------------
    int search_count = this.cateProc.list_search_count(word);
    String paging = this.cateProc.pagingBox(now_page, word, this.list_url, search_count, this.record_per_page, this.page_per_block);
    model.addAttribute("paging", paging);
    model.addAttribute("now_page", now_page);
    
    // 6. 목록 번호 계산
    // 일련 변호 생성: 레코드 갯수 - ((현재 페이지수 -1) * 페이지당 레코드 수)
    int no = search_count - ((now_page - 1) * this.record_per_page);
    model.addAttribute("no", no);
    // System.out.println("-> no: " + no);        
    // --------------------------------------------------------------------------------------    
    
    // 7. 특정 cateno에 해당하는 products 레코드 수
    int count_by_cateno = this.productsProc.count_by_cateno(cateno);
    model.addAttribute("count_by_cateno", count_by_cateno);
    // System.out.println("-> count_by_cateno: " + count_by_cateno);
    
    return "cate/delete"; // /templates/cate/delete.html
  }
  
  /**
   * 삭제 처리
   * 실제 DB에서 해당 카테고리 삭제 수행
   * 삭제 후 남은 목록 수에 따라 페이지 번호 조정
   * 성공 시 목록으로 리다이렉트, 실패 시 메시지 출력
   * @param model
   * @return
   */
  @PostMapping(value="/delete/{cateno}")
  public String delete_process(Model model, 
                                         @PathVariable("cateno") Integer cateno,
                                         @RequestParam(name="word", defaultValue = "") String word,
                                         RedirectAttributes ra,
                                         @RequestParam(name="now_page", defaultValue="1") int now_page) {    
    // 1. 삭제 전 정보를 미리 조회 (실패 시 메시지 등에 활용)
    CateVO cateVO = this.cateProc.read(cateno); // 삭제 정보 출력용으로 사전에 읽음
    model.addAttribute("cateVO", cateVO);
    
    // 2. 삭제 실행
    int cnt = this.cateProc.delete(cateno); // 삭제 처리
    // System.out.println("-> cnt: " + cnt);
    
    if (cnt == 1) {
      //  삭제 성공 처리
      // ----------------------------------------------------------------------------------------------------------
      // 마지막 페이지에서 모든 레코드가 삭제되면 페이지수를 1 감소 시켜야함.
      int search_cnt = this.cateProc.list_search_count(word);
      if (search_cnt % this.record_per_page == 0) {
        now_page = now_page - 1;
        if (now_page < 1) {
          now_page = 1; // 최소 시작 페이지
        }
      }
      // ----------------------------------------------------------------------------------------------------------
      
      // model.addAttribute("code", Tool.DELETE_SUCCESS);
      // redirect 시 검색어와 페이지 번호를 다시 전달
      ra.addAttribute("word", word);
      ra.addAttribute("now_page", now_page);      

      return "redirect:/cate/list_search"; // @GetMapping(value="/list_search")
      // 다시 목록으로
    } else {
      // 삭제 실패 처리
      model.addAttribute("code", Tool.DELETE_FAIL); 
    }
    // 삭제 실패 시 정보 유지
    model.addAttribute("grp", cateVO.getGrp());
    model.addAttribute("name", cateVO.getName());
    model.addAttribute("cnt", cnt);

    return "cate/msg";  // /templates/cate/msg.html
  }  

  /**
   * 특정 cateno에 해당하는 products 삭제 후 cate 삭제 처리
   * 해당 카테고리(cate) 자체도 삭제하는 메서드
   * @param model
   * @return
   */
  @PostMapping(value="/delete_all_by_cateno/{cateno}")
  public String delete_all_by_cateno(Model model, 
                                         @PathVariable("cateno") Integer cateno,
                                         @RequestParam(name="word", defaultValue = "") String word,
                                         RedirectAttributes ra,
                                         @RequestParam(name="now_page", defaultValue="1") int now_page) {    
    // 1. 삭제 전 카테고리 정보 읽어오기 (뷰에 정보 전달용)
    CateVO cateVO = this.cateProc.read(cateno); // 삭제 정보 출력용으로 사전에 읽음
    model.addAttribute("cateVO", cateVO);
    
    // int count_by_cateno = this.productsProc.count_by_cateno(now_page);
    
    // 자식 테이블 삭제
    // 2. 해당 카테고리에 속한 상품들을 모두 삭제 (자식 테이블 먼저)
    int count_by_cateno = this.productsProc.delete_by_cateno(cateno);
    System.out.println("-> count_by_cateno 삭제된 레코드 수: " + count_by_cateno);
    
    // 카테고리 그룹에 등록된 글수 변경
    // .....
    // 3. 자식 삭제 후, 부모 카테고리 삭제
    int cnt = this.cateProc.delete(cateno); // 삭제 처리
    // System.out.println("-> cnt: " + cnt);
    
    if (cnt == 1) {
      // 삭제 성공 시
      // ----------------------------------------------------------------------------------------------------------
      // 마지막 페이지에서 모든 레코드가 삭제되면 페이지수를 1 감소 시켜야함.
      // 현재 페이지에서 모든 항목이 삭제된 경우 → 이전 페이지로 자동 이동
      int search_cnt = this.cateProc.list_search_count(word);
      if (search_cnt % this.record_per_page == 0) {
        now_page = now_page - 1;
        if (now_page < 1) {
          now_page = 1; // 최소 시작 페이지
        }
      }
      // ----------------------------------------------------------------------------------------------------------
      
      // model.addAttribute("code", Tool.DELETE_SUCCESS);
      // redirect 시 검색어 및 페이지 정보 전달
      ra.addAttribute("word", word);
      ra.addAttribute("now_page", now_page);      

      // 다시 카테고리 목록으로 리다이렉트
      return "redirect:/cate/list_search"; // @GetMapping(value="/list_search")
    } else {
      // 삭제 실패 시 메시지 처리
      model.addAttribute("code", Tool.DELETE_FAIL); 
    }

    // 실패 시 정보 전달 (템플릿 출력용)
    model.addAttribute("grp", cateVO.getGrp());
    model.addAttribute("name", cateVO.getName());
    model.addAttribute("cnt", cnt);

    return "cate/msg";  // /templates/cate/msg.html
  }  
  
  /**
   * 우선 순위 높임, 10 등 -> 1 등
   * 내부적으로 seqno 값을 줄임
   * http://localhost:9091/cate/update_seqno_forward/1
   */
  @GetMapping(value="/update_seqno_forward/{cateno}")
  public String update_seqno_forward(Model model, @PathVariable("cateno") Integer cateno,
                                                  @RequestParam(name="word", defaultValue = "") String word,
                                                  @RequestParam(name="now_page", defaultValue = "1") int now_page,
                                                  RedirectAttributes ra) {
    
    this.cateProc.update_seqno_forward(cateno); // 서비스 로직에서 seqno 감소
    
    ra.addAttribute("word", word);
    ra.addAttribute("now_page", now_page);
    
    return "redirect:/cate/list_search";  // @GetMapping
  }

  /**
   * 우선 순위 낮춤, 1 등 -> 10 등
   * 내부적으로 seqno 값을 증가시킴
   * http://localhost:9091/cate/update_seqno_forward/1
   */
  @GetMapping(value="/update_seqno_backward/{cateno}")
  public String update_seqno_backward(Model model, @PathVariable("cateno") Integer cateno,
                                                    @RequestParam(name="word", defaultValue = "") String word,
                                                    @RequestParam(name="now_page", defaultValue = "1") int now_page,
                                                    RedirectAttributes ra) {
    
    this.cateProc.update_seqno_backward(cateno); // 서비스 로직에서 seqno 증가
    
    ra.addAttribute("word", word);
    ra.addAttribute("now_page", now_page);
    
    return "redirect:/cate/list_search";  // @GetMapping
  }
  
  /**
   * 카테고리 공개 설정  (visible = 'Y')
   * 일반 사용자에게도 보여지도록 설정
   * 검색어 및 현재 페이지 정보 유지하며 목록으로 리다이렉트
   * http://localhost:9091/cate/update_visible_y/1
   */
  @GetMapping(value="/update_visible_y/{cateno}")
  public String update_visible_y(Model model, @PathVariable("cateno") Integer cateno,
      @RequestParam(name="word", defaultValue = "") String word,
      @RequestParam(name="now_page", defaultValue="1") int now_page,
      RedirectAttributes ra) {
    
    // System.out.println("-> update_visible_y()");
    
    // 검색어, 페이지 유지
    this.cateProc.update_visible_y(cateno); // visible 컬럼을 'Y'로 변경
    
    ra.addAttribute("word", word); // redirect로 데이터 전송, 한글 깨짐 방지
    ra.addAttribute("now_page", now_page); // redirect로 데이터 전송, 한글 깨짐 방지
    
    return "redirect:/cate/list_search";  // @GetMapping(value="/list_search")
    // 목록 페이지 새로고침
  }
  
  /**
   * 카테고리 비공개 설정 (visible = 'N')
   * 관리자에게만 보이고 일반 사용자에겐 숨김
   * 검색어 및 현재 페이지 정보 유지하며 목록으로 리다이렉트
   * http://localhost:9091/cate/update_visible_n/1
   */
  @GetMapping(value="/update_visible_n/{cateno}")
  public String update_visible_n(Model model, @PathVariable("cateno") Integer cateno,
                                            @RequestParam(name="word", defaultValue = "") String word,
                                            @RequestParam(name="now_page", defaultValue="1") int now_page,
                                            RedirectAttributes ra) {
    
    // System.out.println("-> update_visible_n()");
    
    this.cateProc.update_visible_n(cateno); // visible 컬럼을 'N'으로 변경
    
    // 검색어, 페이지 유지
    ra.addAttribute("word", word); // redirect로 데이터 전송, 한글 깨짐 방지
    ra.addAttribute("now_page", now_page); // redirect로 데이터 전송, 한글 깨짐 방지
    
    return "redirect:/cate/list_search";  // @GetMapping(value="/list_search")
    // 목록 페이지 새로고침
  }
  
  /*
   * 20250619 추가 
   */
  @GetMapping("/update_cnt")
  @ResponseBody
  public String updateCnt() {
    cateProc.updateMidCnt();
    cateProc.updateMainCnt();
    return "카테고리 cnt 동기화 완료"; //수정해야함
  }
  
}


