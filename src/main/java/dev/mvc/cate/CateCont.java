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

import dev.mvc.contents.ContentsProcInter;
import dev.mvc.member.MemberProc;
import dev.mvc.tool.Tool;
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
  @Qualifier("dev.mvc.contents.ContentsProc") // @Component("dev.mvc.contents.ContentsProc")
  private ContentsProcInter contentsProc;  
  
  /** 페이지당 출력할 레코드 갯수, nowPage는 1부터 시작 */
  public int record_per_page = 7;

  /** 블럭당 페이지 수, 하나의 블럭은 10개의 페이지로 구성됨 */
  public int page_per_block = 10;
  
  /** 페이징 목록 주소, @GetMapping(value="/list_search") */
  private String list_url = "/cate/list_search";
  
  public CateCont( ) {
    System.out.println("-> CateCont created.");
  }

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
    if (bindingResult.hasErrors() == true) {
      return "cate/create"; // /templates/cate/create.html 
    }
    
    // System.out.println("-> cateVO.getName(): " + cateVO.getName());
    // System.out.println("-> cateVO.getSeqno(): " + cateVO.getSeqno());
    
    int cnt = this.cateProc.create(cateVO);
    // System.out.println("-> cnt: " + cnt);
    
    if (cnt == 1) {
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
    cateVO.setGrp("");
    cateVO.setName("");
    
    ArrayList<CateVO> list = this.cateProc.list_all();
    model.addAttribute("list", list);
    
    // 2단 메뉴
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);
    
    // 카테고리 그룹 목록
    ArrayList<String> grpset = this.cateProc.grpset();
    cateVO.setGrp(String.join("/",  grpset));
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
    
    if (this.memberProc.isAdmin(session)) {
      CateVO cateVO = new CateVO(); // form 초기값 전달
      // cateVO.setGenre("분류");
      // cateVO.setName("카테고리 이름을 입력하세요."); // Form으로 초기값을 전달
      
      // 카테고리 그룹 목록
      ArrayList<String> list_grp = this.cateProc.grpset();
      cateVO.setGrp(String.join("/", list_grp)); // 기존에 등록된 그룹명 제시
      
      model.addAttribute("cateVO", cateVO); // 등록폼 카테고리 그룹 초기값
      
      word = Tool.checkNull(word);             // null -> ""
      
      ArrayList<CateVO> list = this.cateProc.list_search_paging(word, now_page, this.record_per_page);
      model.addAttribute("list", list);
      
//      ArrayList<CateVO> menu = this.cateProc.list_all_categrp_y();
//      model.addAttribute("menu", menu);

      ArrayList<CateVOMenu> menu = this.cateProc.menu();
      model.addAttribute("menu", menu);
      
      int search_cnt = list.size();
      model.addAttribute("search_cnt", search_cnt);    
      
      model.addAttribute("word", word); // 검색어
      
      // --------------------------------------------------------------------------------------
      // 페이지 번호 목록 생성
      // --------------------------------------------------------------------------------------
      int search_count = this.cateProc.list_search_count(word);
      String paging = this.cateProc.pagingBox(now_page, word, this.list_url, search_count, this.record_per_page, this.page_per_block);
      model.addAttribute("paging", paging);
      model.addAttribute("now_page", now_page);
      
      // 일련 변호 생성: 레코드 갯수 - ((현재 페이지수 -1) * 페이지당 레코드 수)
      int no = search_count - ((now_page - 1) * this.record_per_page);
      model.addAttribute("no", no);
      // System.out.println("-> no: " + no);
      // --------------------------------------------------------------------------------------    
      
      return "cate/list_search";  // /templates/cate/list_search.html
    } else {
      return "redirect:/member/login_cookie_need?url=/cate/list_search"; // redirect
    }
    

  }  
  
  /**
   * 조회
   * http://localhost:9091/cate/read/1
   * @param model
   * @return
   */
  @GetMapping(value="/read/{cateno}")
  public String read (Model model, @PathVariable("cateno") Integer cateno,
                             @RequestParam(name="word", defaultValue = "") String word,
                             @RequestParam(name="now_page", defaultValue="1") int now_page) {
    // System.out.println("-> read cateno: " + cateno);
    
    CateVO cateVO = this.cateProc.read(cateno);
    model.addAttribute("cateVO", cateVO);
    
//    ArrayList<CateVO> list = this.cateProc.list_all();
//    model.addAttribute("list", list);
    
    // 2단 메뉴
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);
    
    // 카테고리 그룹 목록
//    ArrayList<String> grpset = this.cateProc.grpset();
//    cateVO.setGrp(String.join("/",  grpset));
//    System.out.println("-> cateVO.getGrp(): " + cateVO.getGrp());
    
    model.addAttribute("word", word);
    // System.out.println("-> word null 체크: " + word);

    // ArrayList<CateVO> list = this.cateProc.list_search(word);  // 검색 목록
    ArrayList<CateVO> list = this.cateProc.list_search_paging(word, now_page, this.record_per_page); // 검색 목록 + 페이징
    model.addAttribute("list", list);
    
    int list_search_count = this.cateProc.list_search_count(word); // 검색된 레코드 갯수
    model.addAttribute("list_search_count", list_search_count);
    
    // --------------------------------------------------------------------------------------
    // 페이지 번호 목록 생성
    // --------------------------------------------------------------------------------------
    int search_count = this.cateProc.list_search_count(word);
    String paging = this.cateProc.pagingBox(now_page, word, this.list_url, search_count, this.record_per_page, this.page_per_block);
    model.addAttribute("paging", paging);
    model.addAttribute("now_page", now_page);
    
    // 일련 변호 생성: 레코드 갯수 - ((현재 페이지수 -1) * 페이지당 레코드 수)
    int no = search_count - ((now_page - 1) * this.record_per_page);
    model.addAttribute("no", no);
    // System.out.println("-> no: " + no);    
    // --------------------------------------------------------------------------------------    
    
    return "cate/read"; // /templates/cate/read.html
  }
  
  /**
   * 수정폼
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
      CateVO cateVO = this.cateProc.read(cateno);
      model.addAttribute("cateVO", cateVO);
      
      // 2단 메뉴
      ArrayList<CateVOMenu> menu = this.cateProc.menu();
      model.addAttribute("menu", menu);
      
      model.addAttribute("word", word);
      // System.out.println("-> word null 체크: " + word);

      // ArrayList<CateVO> list = this.cateProc.list_search(word);  // 검색 목록
      ArrayList<CateVO> list = this.cateProc.list_search_paging(word, now_page, this.record_per_page); // 검색 목록 + 페이징
      model.addAttribute("list", list);
      
      int list_search_count = this.cateProc.list_search_count(word); // 검색된 레코드 갯수
      model.addAttribute("list_search_count", list_search_count);
      
      // --------------------------------------------------------------------------------------
      // 페이지 번호 목록 생성
      // --------------------------------------------------------------------------------------
      int search_count = this.cateProc.list_search_count(word);
      String paging = this.cateProc.pagingBox(now_page, word, this.list_url, search_count, this.record_per_page, this.page_per_block);
      model.addAttribute("paging", paging);
      model.addAttribute("now_page", now_page);
      
      // 일련 변호 생성: 레코드 갯수 - ((현재 페이지수 -1) * 페이지당 레코드 수)
      int no = search_count - ((now_page - 1) * this.record_per_page);
      model.addAttribute("no", no);
      // System.out.println("-> no: " + no);    
      // --------------------------------------------------------------------------------------  
             
      return "cate/update"; // /templates/cate/update.html
    } else {
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
    
    if (bindingResult.hasErrors() == true) {
      return "cate/update"; // /templates/cate/update.html 
    }
    
    // System.out.println("-> cateVO.getName(): " + cateVO.getName());
    // System.out.println("-> cateVO.getSeqno(): " + cateVO.getSeqno());
    
    int cnt = this.cateProc.update(cateVO);
    // System.out.println("-> cnt: " + cnt);
    
    if (cnt == 1) {
//      model.addAttribute("code", Tool.UPDATE_SUCCESS);  
//      model.addAttribute("name", cateVO.getName());
      // System.out.println("-> redirect:/cate/update/" + cateVO.getCateno() + "?word=" + word);
      // http://localhost:9091/cate/update/33?word=영화
      // return "redirect:/cate/update/" + cateVO.getCateno() + "?word=" + word; // @GetMapping(value="/update") // 한글 깨짐, X
      ra.addAttribute("word", word); // redirect로 데이터 전송, 한글 깨짐 방지
      return "redirect:/cate/update/" + cateVO.getCateno();
    } else {
      model.addAttribute("code", Tool.UPDATE_FAIL); 
    }
    
    model.addAttribute("cnt", cnt);
    
    return "cate/msg";  // /templates/cate/msg.html
  }

  /**
   * 삭제폼
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
    
    CateVO cateVO = this.cateProc.read(cateno);
    model.addAttribute("cateVO", cateVO);
    
    // ArrayList<CateVO> list = this.cateProc.list_all();
    // ArrayList<CateVO> list = this.cateProc.list_search(word);
    ArrayList<CateVO> list = this.cateProc.list_search_paging(word, now_page, this.record_per_page);
    // System.out.println("-> delete form list.size(): " + list.size());
    model.addAttribute("list", list);
    
    // 2단 메뉴
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);
    
    model.addAttribute("word", word);
    
    // --------------------------------------------------------------------------------------
    // 페이지 번호 목록 생성
    // --------------------------------------------------------------------------------------
    int search_count = this.cateProc.list_search_count(word);
    String paging = this.cateProc.pagingBox(now_page, word, this.list_url, search_count, this.record_per_page, this.page_per_block);
    model.addAttribute("paging", paging);
    model.addAttribute("now_page", now_page);
    
    // 일련 변호 생성: 레코드 갯수 - ((현재 페이지수 -1) * 페이지당 레코드 수)
    int no = search_count - ((now_page - 1) * this.record_per_page);
    model.addAttribute("no", no);
    // System.out.println("-> no: " + no);        
    // --------------------------------------------------------------------------------------    
    
    // 특정 cateno에 해당하는 contents 레코드 수
    int count_by_cateno = this.contentsProc.count_by_cateno(cateno);
    model.addAttribute("count_by_cateno", count_by_cateno);
    // System.out.println("-> count_by_cateno: " + count_by_cateno);
    
    return "cate/delete"; // /templates/cate/delete.html
  }
  
  /**
   * 삭제 처리
   * @param model
   * @return
   */
  @PostMapping(value="/delete/{cateno}")
  public String delete_process(Model model, 
                                         @PathVariable("cateno") Integer cateno,
                                         @RequestParam(name="word", defaultValue = "") String word,
                                         RedirectAttributes ra,
                                         @RequestParam(name="now_page", defaultValue="1") int now_page) {    
    CateVO cateVO = this.cateProc.read(cateno); // 삭제 정보 출력용으로 사전에 읽음
    model.addAttribute("cateVO", cateVO);
    
    int cnt = this.cateProc.delete(cateno); // 삭제 처리
    // System.out.println("-> cnt: " + cnt);
    
    if (cnt == 1) {
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
      ra.addAttribute("word", word);
      ra.addAttribute("now_page", now_page);      

      return "redirect:/cate/list_search"; // @GetMapping(value="/list_search")
    } else {
      model.addAttribute("code", Tool.DELETE_FAIL); 
    }

    model.addAttribute("grp", cateVO.getGrp());
    model.addAttribute("name", cateVO.getName());
    model.addAttribute("cnt", cnt);

    return "cate/msg";  // /templates/cate/msg.html
  }  

  /**
   * 특정 cateno에 해당하는 contents 삭제 후 cate 삭제 처리
   * @param model
   * @return
   */
  @PostMapping(value="/delete_all_by_cateno/{cateno}")
  public String delete_all_by_cateno(Model model, 
                                         @PathVariable("cateno") Integer cateno,
                                         @RequestParam(name="word", defaultValue = "") String word,
                                         RedirectAttributes ra,
                                         @RequestParam(name="now_page", defaultValue="1") int now_page) {    
    CateVO cateVO = this.cateProc.read(cateno); // 삭제 정보 출력용으로 사전에 읽음
    model.addAttribute("cateVO", cateVO);
    
    // int count_by_cateno = this.contentsProc.count_by_cateno(now_page);
    
    // 자식 테이블 삭제
    int count_by_cateno = this.contentsProc.delete_by_cateno(cateno);
    System.out.println("-> count_by_cateno 삭제된 레코드 수: " + count_by_cateno);
    
    // 카테고리 그룹에 등록된 글수 변경
    // .....
    
    int cnt = this.cateProc.delete(cateno); // 삭제 처리
    // System.out.println("-> cnt: " + cnt);
    
    if (cnt == 1) {
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
      ra.addAttribute("word", word);
      ra.addAttribute("now_page", now_page);      

      return "redirect:/cate/list_search"; // @GetMapping(value="/list_search")
    } else {
      model.addAttribute("code", Tool.DELETE_FAIL); 
    }

    model.addAttribute("grp", cateVO.getGrp());
    model.addAttribute("name", cateVO.getName());
    model.addAttribute("cnt", cnt);

    return "cate/msg";  // /templates/cate/msg.html
  }  
  
  /**
   * 우선 순위 높임, 10 등 -> 1 등
   * http://localhost:9091/cate/update_seqno_forward/1
   */
  @GetMapping(value="/update_seqno_forward/{cateno}")
  public String update_seqno_forward(Model model, @PathVariable("cateno") Integer cateno) {
    
    this.cateProc.update_seqno_forward(cateno);
    
    return "redirect:/cate/list_all";  // @GetMapping(value="/list_all")
  }

  /**
   * 우선 순위 낮춤, 1 등 -> 10 등
   * http://localhost:9091/cate/update_seqno_forward/1
   */
  @GetMapping(value="/update_seqno_backward/{cateno}")
  public String update_seqno_backward(Model model, @PathVariable("cateno") Integer cateno) {
    
    this.cateProc.update_seqno_backward(cateno);
    
    return "redirect:/cate/list_all";  // @GetMapping(value="/list_all")
  }
  
  /**
   * 카테고리 공개 설정
   * http://localhost:9091/cate/update_visible_y/1
   */
  @GetMapping(value="/update_visible_y/{cateno}")
  public String update_visible_y(Model model, @PathVariable("cateno") Integer cateno,
      @RequestParam(name="word", defaultValue = "") String word,
      @RequestParam(name="now_page", defaultValue="1") int now_page,
      RedirectAttributes ra) {
    
    // System.out.println("-> update_visible_y()");
    
    this.cateProc.update_visible_y(cateno);
    
    ra.addAttribute("word", word); // redirect로 데이터 전송, 한글 깨짐 방지
    ra.addAttribute("now_page", now_page); // redirect로 데이터 전송, 한글 깨짐 방지
    
    return "redirect:/cate/list_search";  // @GetMapping(value="/list_search")
  }
  
  /**
   * 카테고리 비공개 설정
   * http://localhost:9091/cate/update_visible_n/1
   */
  @GetMapping(value="/update_visible_n/{cateno}")
  public String update_visible_n(Model model, @PathVariable("cateno") Integer cateno,
                                            @RequestParam(name="word", defaultValue = "") String word,
                                            @RequestParam(name="now_page", defaultValue="1") int now_page,
                                            RedirectAttributes ra) {
    
    // System.out.println("-> update_visible_n()");
    
    this.cateProc.update_visible_n(cateno);
    
    ra.addAttribute("word", word); // redirect로 데이터 전송, 한글 깨짐 방지
    ra.addAttribute("now_page", now_page); // redirect로 데이터 전송, 한글 깨짐 방지
    
    return "redirect:/cate/list_search";  // @GetMapping(value="/list_search")
  }
  
}


