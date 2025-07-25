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

  // 회원 관련 비즈니스 로직 (권한 확인 등) 처리하는 Service 주입
  @Autowired
  @Qualifier("dev.mvc.member.MemberProc")
  private MemberProc memberProc;

  // 상품 관련 기능(카테고리 연동)을 위해 ProductsProc 주입
  @Autowired
  @Qualifier("dev.mvc.products.ProductsProc")
  private ProductsProcInter productsProc;

  public CateCont() {
    System.out.println("-> CateCont created.");
  }

  /** 서버 기동 시 카테고리 건수(cnt) 동기화 */
  @PostConstruct
  public void init() {
    System.out.println("-> CateCont created.");
    cateProc.updateMidCnt(); // 중분류 cnt 갱신
    cateProc.updateMainCnt(); // 대분류 cnt 갱신
    System.out.println("→ 자동 카테고리 cnt 동기화 완료");
  }

  /** 아래 셋은 페이징 기본값 */

  /** 한 페이지당 표시할 카테고리 수 (페이징에 사용됨) */
  /** 페이지당 출력할 레코드 갯수, nowPage는 1부터 시작 */
  public int record_per_page = 7;

  /** 한 번에 보여줄 페이징 블록 수 */
  /** 블럭당 페이지 수, 하나의 블럭은 10개의 페이지로 구성됨 */
  public int page_per_block = 10;

  /** 페이징이 적용된 목록 페이지 URL */
  /** 페이징 목록 주소, @GetMapping(value="/list_search") */
  private String list_url = "/cate/list_search";

  /**
   * 관리자 권한 확인 메서드 (숫자 기반) grade: 1~4 → 관리자
   */
  private boolean isAdmin(HttpSession session) {
    Object gradeObj = session.getAttribute("grade");
    if (gradeObj instanceof Integer) {
      int grade = (Integer) gradeObj;
      return grade >= 1 && grade <= 4;
    }
    return false;
  }

  /**
   * [GET] 카테고리 등록 폼 URL: /cate/create 기본값(grp, name) 설정 후 등록 화면 출력
   */
  @GetMapping(value = "/create")
  public String create(@ModelAttribute("cateVO") CateVO cateVO) {
    // 폼 초기값 설정 (placeholder 느낌)
    cateVO.setGrp("제철/신선식품/가공식품/즉석조리...");
    cateVO.setName("채소");

    return "cate/create"; // /templates/cate/create.html
  }

  /**
   * [POST] 카테고리 등록 처리
   * 
   * @Valid + BindingResult → 입력 검증 후 등록 성공 시 list_search 페이지로 리다이렉트
   */
  @PostMapping(value = "/create")
  public String create(Model model, @Valid CateVO cateVO, BindingResult bindingResult,
      @RequestParam(name = "word", defaultValue = "") String word, RedirectAttributes ra) {
    // System.out.println("-> create post");
    // 유효성 검사 실패 시 등록폼 다시 보여줌
    if (bindingResult.hasErrors() == true) {
      return "cate/create"; // 유효성 실패 → /templates/cate/create.html
    }
    // DB에 카테고리 등록 요청
    int cnt = this.cateProc.create(cateVO);
    // System.out.println("-> cnt: " + cnt);

    if (cnt == 1) {
      // 성공 시 검색어 유지하며 목록 페이지로 이동
      ra.addAttribute("word", word); // 검색어 유지
      return "redirect:/cate/list_search"; // @GetMapping(value="/list_search") 호출
    } else {
      model.addAttribute("code", Tool.CREATE_FAIL);
    }
    model.addAttribute("cnt", cnt);
    return "cate/msg"; // /templates/cate/msg.html
  }

  /**
   * [GET] 카테고리 전체 목록 URL: /cate/list_all 모든 카테고리 출력
   */
  @GetMapping(value = "/list_all")
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
    cateVO.setGrp(String.join("/", grpset)); // 슬래시로 구분된 문자열로 만들어 보여줌
    // System.out.println("-> cateVO.getGrp(): " + cateVO.getGrp());

    return "cate/list_all"; // /templates/cate/list_all.html
  }

  /**
   * [GET] 카테고리 검색 + 페이징 목록 URL: /cate/list_search?word=검색어&now_page=1 접근 권한: 관리자만
   * (1~4) 처리 순서: 1. 권한 체크 → 관리자 아니면 로그인 페이지로 리다이렉트 2. 등록폼 초기 데이터 생성 (그룹 목록) 3.
   * 검색어 null 처리 4. 검색 결과 조회 (페이징) 5. 메뉴(대/중분류) 정보 조회 6. 검색결과 개수 및 페이징 데이터 생성 7.
   * cate/list_search.html 뷰로 전달
   */
  @GetMapping(value = "/list_search")
  public String list_search_paging(HttpSession session, Model model,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {

    // 관리자만 접근 가능하도록 제한
    if (!memberProc.isAdmin(session)) {
      return "redirect:/member/login_cookie_need?url=/cate/list_search";
    }
    // 1. 등록 폼에서 사용할 기본값 준비
    CateVO cateVO = new CateVO(); // form 초기값 전달
    // 카테고리 그룹 목록
    // 2. 등록 폼에 보여줄 그룹 목록 생성 → "과일/채소/음료" 형태로 표시
    ArrayList<String> list_grp = this.cateProc.grpset(); // 중복 없이 그룹 목록만 가져오기
    cateVO.setGrp(String.join("/", list_grp)); // 슬래시로 구분된 문자열로 변환, (과일/채소/음료" 형태)
    model.addAttribute("cateVO", cateVO); // 등록폼 카테고리 그룹 초기값

    // 3. 검색어 null 방지 처리
    word = Tool.checkNull(word); // null -> ""

    // 4. 해당 페이지의 검색 결과 목록 가져오기
    ArrayList<CateVO> list = this.cateProc.list_search_paging(word, now_page, this.record_per_page);
    model.addAttribute("list", list); // 템플릿에서 반복 출력용

    // 5. 메뉴 구성 (2단 메뉴용, 대분류 + 중분류 형태)
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);

    // 6. 검색 결과 개수 표시
    int search_cnt = list.size(); // 현재 페이지에 검색된 카테고리 수
    model.addAttribute("search_cnt", search_cnt);

    // 7. 검색어 유지 (뷰에서 다시 표시하기 위해)
    model.addAttribute("word", word);

    // 8. 페이지 번호 목록 생성
    int search_count = this.cateProc.list_search_count(word); // 전체 검색 결과 수
    // 페이징 HTML 코드 생성 ( 1 2 3 )
    model.addAttribute("list_search_count", search_count); // 총 검색 개수
    String paging = this.cateProc.pagingBox(now_page, word, this.list_url, search_count, this.record_per_page,
        this.page_per_block);
    model.addAttribute("paging", paging); // 템플릿에 전달
    model.addAttribute("now_page", now_page); // 현재 페이지 번호 유지
    // 일련 변호 생성: 레코드 갯수 - ((현재 페이지수 -1) * 페이지당 레코드 수)
    int no = search_count - ((now_page - 1) * this.record_per_page);
    model.addAttribute("no", no); // 예: 총 35건 중 1페이지 시작이면 35 → 29 → 28 ...

    // 7. 검색 페이지 이동
    return "cate/list_search"; // /templates/cate/list_search.html
  }

  /**
   * [GET] 카테고리 상세 조회 + 검색 결과 목록 URL: /cate/read/{cateno}?word=검색어&now_page=1 처리
   * 순서: 1. 카테고리 상세정보 조회 2. 메뉴 정보, 그룹 목록 생성 3. 검색어, 페이징 데이터 유지 4. cate/read.html로
   * 전달
   */
  @GetMapping(value = "/read/{cateno}")
  public String read(Model model, @PathVariable("cateno") Integer cateno,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {

    // 1. cateno에 해당하는 카테고리 정보 1건 조회
    CateVO cateVO = this.cateProc.read(cateno);
    model.addAttribute("cateVO", cateVO); // 뷰에 전달 (상세정보 출력용)

    // 2. 2단 메뉴 구성 (대분류-중분류 구조)
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);

    // 카테고리 그룹 목록
    ArrayList<String> grpset = this.cateProc.grpset();
    cateVO.setGrp(String.join("/", grpset));
    // System.out.println("-> cateVO.getGrp(): " + cateVO.getGrp());

    // 3. 검색어 유지 처리 (null 방지)
    model.addAttribute("word", word);
    // System.out.println("-> word null 체크: " + word);

    // 4. 현재 검색어로 페이징된 카테고리 목록 가져오기
    // ArrayList<CateVO> list = this.cateProc.list_search(word); // 검색 목록
    ArrayList<CateVO> list = this.cateProc.list_search_paging(word, now_page, this.record_per_page); // 검색 목록 + 페이징
    model.addAttribute("list", list);

    // 5. 검색된 카테고리 개수 (총 몇 건 검색되었는지)
    int list_search_count = this.cateProc.list_search_count(word); // 검색된 레코드 갯수
    model.addAttribute("list_search_count", list_search_count); // 템플릿에서 "총 n건" 표시용

    // 6. 페이지 번호 목록 생성
    int search_count = this.cateProc.list_search_count(word); // 위와 동일한 값
    String paging = this.cateProc.pagingBox(now_page, word, this.list_url, search_count, this.record_per_page,
        this.page_per_block);
    model.addAttribute("paging", paging); // 페이지 번호 출력용 HTML
    model.addAttribute("now_page", now_page); // 현재 페이지 번호 전달

    // 7. 현재 페이지에서 시작하는 번호 계산
    // 일련 변호 생성: 레코드 갯수 - ((현재 페이지수 -1) * 페이지당 레코드 수)
    int no = search_count - ((now_page - 1) * this.record_per_page);
    model.addAttribute("no", no); // 템플릿에서 일련번호로 사용
    // System.out.println("-> no: " + no);

    // 8. 최종적으로 read.html 템플릿을 열어 모든 데이터 전달
    return "cate/read"; // /templates/cate/read.html
  }

  /**
   * [GET] 카테고리 수정 폼 URL: /cate/update/{cateno}?word=검색어&now_page=페이지번호 접근 권한:
   * 관리자\ 처리 순서: 1. 관리자 권한 확인 → 미승인 시 로그인 필요 페이지로 이동 2. 수정 대상 카테고리 조회 (cateVO) 3.
   * 메뉴 구조 및 검색 조건 데이터 준비 4. 검색 결과 목록 + 페이징 데이터 전달 5. cate/update.html로 이동
   */
  @GetMapping(value = "/update/{cateno}")
  public String update(Model model, HttpSession session, @PathVariable("cateno") Integer cateno,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {
    // System.out.println("-> read cateno: " + cateno);

    if (!this.memberProc.isAdmin(session)) {
      return "redirect:/member/login_cookie_need";
    }
    // 1. 수정 대상 카테고리 정보 불러오기
    CateVO cateVO = this.cateProc.read(cateno);
    model.addAttribute("cateVO", cateVO);

    // 2단 메뉴 대분류-중분류 메뉴 구조 불러오기
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);

    // 3. 검색어 및 현재 페이지 유지
    model.addAttribute("word", word);
    // System.out.println("-> word null 체크: " + word);

    // 4. 검색 결과 목록 불러오기 (페이징 포함)
    ArrayList<CateVO> list = this.cateProc.list_search_paging(word, now_page, this.record_per_page); // 검색 목록 + 페이징
    model.addAttribute("list", list);

    // 5. 검색 결과 개수
    int list_search_count = this.cateProc.list_search_count(word); // 검색된 레코드 갯수
    model.addAttribute("list_search_count", list_search_count);

    // 6. 페이지 번호 목록 생성
    int search_count = this.cateProc.list_search_count(word);
    String paging = this.cateProc.pagingBox(now_page, word, this.list_url, search_count, this.record_per_page,
        this.page_per_block);
    model.addAttribute("paging", paging);
    model.addAttribute("now_page", now_page);

    // 7. 목록 번호 계산
    // 일련 변호 생성: 레코드 갯수 - ((현재 페이지수 -1) * 페이지당 레코드 수)
    int no = search_count - ((now_page - 1) * this.record_per_page);
    model.addAttribute("no", no);
    // System.out.println("-> no: " + no);

    // 8. 수정 페이지 이동
    return "cate/update"; // /templates/cate/update.html
  }

  /**
   * [POST] 카테고리 수정 처리 URL: /cate/update 처리 순서: 1. 입력값 유효성 검사 (@Valid +
   * BindingResult) 2. DB Update 실행 3. 성공 → 수정 폼으로 리다이렉트 (POST-Redirect-GET) 4. 실패
   * → msg.html로 이동
   */
  @PostMapping(value = "/update")
  public String update_process(Model model, @Valid CateVO cateVO, BindingResult bindingResult,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page, RedirectAttributes ra) {
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

    if (cnt == 1) { // 성공 → 수정 폼으로 리다이렉트 (검색어 유지)
      ra.addAttribute("word", word); // redirect로 데이터 전송, 한글 깨짐 방지, redirect 시 검색어 유지 (URL에 자동 포함됨)
      return "redirect:/cate/update/" + cateVO.getCateno(); // 다시 수정폼으로 이동
    } else {
      // 4. 실패 → 메시지 페이지로 이동
      model.addAttribute("code", Tool.UPDATE_FAIL);
    }
    model.addAttribute("cnt", cnt);

    return "cate/msg"; // /templates/cate/msg.html
  }

  /**
   * [GET] 카테고리 삭제 폼 URL: /cate/delete/{cateno}?word=검색어&now_page=1 처리 순서: 1. 삭제
   * 대상 카테고리 조회 2. 검색 결과 목록 + 페이징 3. 해당 카테고리와 연결된 상품 수 조회 4. cate/delete.html 이동
   */
  @GetMapping(value = "/delete/{cateno}")
  public String delete(Model model, @PathVariable("cateno") Integer cateno,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {
    // System.out.println("-> read cateno: " + cateno);
    // 1. 삭제할 카테고리 정보 조회 (삭제 확인용)
    CateVO cateVO = this.cateProc.read(cateno);
    model.addAttribute("cateVO", cateVO);

    // 2. 검색된 목록 출력 (삭제폼에서도 목록 보여줌)
    ArrayList<CateVO> list = this.cateProc.list_search_paging(word, now_page, this.record_per_page);
    // System.out.println("-> delete form list.size(): " + list.size());
    model.addAttribute("list", list);

    // 3. 2단 메뉴 (대분류/중분류)
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);

    // 4. 검색어 및 페이징 정보 전달
    model.addAttribute("word", word);

    // 5. 페이지 번호 목록 생성
    int search_count = this.cateProc.list_search_count(word);
    String paging = this.cateProc.pagingBox(now_page, word, this.list_url, search_count, this.record_per_page,
        this.page_per_block);
    model.addAttribute("paging", paging);
    model.addAttribute("now_page", now_page);

    // 6. 목록 번호 계산
    // 일련 변호 생성: 레코드 갯수 - ((현재 페이지수 -1) * 페이지당 레코드 수)
    int no = search_count - ((now_page - 1) * this.record_per_page);
    model.addAttribute("no", no);
    // System.out.println("-> no: " + no);

    // 7. 특정 cateno에 해당하는 products 레코드 수
    int count_by_cateno = this.productsProc.count_by_cateno(cateno);
    model.addAttribute("count_by_cateno", count_by_cateno);
    // System.out.println("-> count_by_cateno: " + count_by_cateno);

    return "cate/delete"; // /templates/cate/delete.html
  }

  /**
   * [POST] 카테고리 삭제 처리 URL: /cate/delete/{cateno} 처리 순서: 1. 삭제 대상 카테고리 정보 확인 2. DB
   * 삭제 실행 3. 성공 → list_search 페이지로 리다이렉트 4. 실패 → msg.html
   */
  @PostMapping(value = "/delete/{cateno}")
  public String delete_process(Model model, @PathVariable("cateno") Integer cateno,
      @RequestParam(name = "word", defaultValue = "") String word, RedirectAttributes ra,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {
    // 1. 삭제 전 정보를 미리 조회 (실패 시 메시지 등에 활용)
    CateVO cateVO = this.cateProc.read(cateno); // 삭제 정보 출력용으로 사전에 읽음
    model.addAttribute("cateVO", cateVO);

    // 2. 삭제 실행
    int cnt = this.cateProc.delete(cateno); // 삭제 처리
    // System.out.println("-> cnt: " + cnt);

    if (cnt == 1) {
      // 삭제 성공 처리
      // 마지막 페이지에서 모든 레코드가 삭제되면 페이지수를 1 감소 시켜야함.
      int search_cnt = this.cateProc.list_search_count(word);
      if (search_cnt % this.record_per_page == 0) {
        now_page = now_page - 1;
        if (now_page < 1) {
          now_page = 1; // 최소 시작 페이지
        }
      }
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

    return "cate/msg"; // /templates/cate/msg.html
  }

  /**
   * [POST] 카테고리와 해당 상품 전체 삭제
   * URL: /cate/delete_all_by_cateno/{cateno}
   * 처리 순서:
   *   1. 삭제 전 카테고리 정보 조회
   *   2. 연관 상품 삭제 (products)
   *   3. 카테고리 삭제
   *   4. 성공 → list_search 리다이렉트
   */
  @PostMapping(value = "/delete_all_by_cateno/{cateno}")
  public String delete_all_by_cateno(Model model, @PathVariable("cateno") Integer cateno,
      @RequestParam(name = "word", defaultValue = "") String word, RedirectAttributes ra,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {
    // 1. 삭제 전 카테고리 정보 읽어오기 (뷰에 정보 전달용)
    CateVO cateVO = this.cateProc.read(cateno); // 삭제 정보 출력용으로 사전에 읽음
    model.addAttribute("cateVO", cateVO);

    // 2. 자식 테이블 삭제. 해당 카테고리에 속한 상품들을 모두 삭제 (자식 테이블 먼저)
    int count_by_cateno = this.productsProc.delete_by_cateno(cateno);
    System.out.println("-> count_by_cateno 삭제된 레코드 수: " + count_by_cateno);

    // 3. 자식 삭제 후, 부모 카테고리 삭제
    int cnt = this.cateProc.delete(cateno); // 삭제 처리
    // System.out.println("-> cnt: " + cnt);

    if (cnt == 1) {
      // 삭제 성공 시
      // 마지막 페이지에서 모든 레코드가 삭제되면 페이지수를 1 감소 시켜야함.
      // 현재 페이지 모든 항목 삭제 시 이전 페이지로 이동
      int search_cnt = this.cateProc.list_search_count(word);
      if (search_cnt % this.record_per_page == 0) {
        now_page = now_page - 1;
        if (now_page < 1) {
          now_page = 1; // 최소 시작 페이지
        }
      }

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

    return "cate/msg"; // /templates/cate/msg.html
  }

  /**
   * [GET] 우선 순위 높임, 10 등 -> 1 등 내부적으로 seqno 값을 줄임
   * http://localhost:9093/cate/update_seqno_forward/1
   */
  @GetMapping(value = "/update_seqno_forward/{cateno}")
  public String update_seqno_forward(Model model, @PathVariable("cateno") Integer cateno,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page, RedirectAttributes ra) {

    this.cateProc.update_seqno_forward(cateno); // 서비스 로직에서 seqno 감소

    ra.addAttribute("word", word);
    ra.addAttribute("now_page", now_page);

    return "redirect:/cate/list_search"; // @GetMapping
  }

  /**
   * [GET] 우선 순위 낮춤, 1 등 -> 10 등 내부적으로 seqno 값을 증가시킴
   * http://localhost:9093/cate/update_seqno_forward/1
   */
  @GetMapping(value = "/update_seqno_backward/{cateno}")
  public String update_seqno_backward(Model model, @PathVariable("cateno") Integer cateno,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page, RedirectAttributes ra) {

    this.cateProc.update_seqno_backward(cateno); // 서비스 로직에서 seqno 증가

    ra.addAttribute("word", word);
    ra.addAttribute("now_page", now_page);

    return "redirect:/cate/list_search"; // @GetMapping
  }

  /**
   * [GET] 카테고리 공개 설정 (visible = 'Y') 일반 사용자에게도 보여지도록 설정 검색어 및 현재 페이지 정보 유지하며 목록으로 리다이렉트
   * http://localhost:9093/cate/update_visible_y/1
   */
  @GetMapping(value = "/update_visible_y/{cateno}")
  public String update_visible_y(Model model, @PathVariable("cateno") Integer cateno,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page, RedirectAttributes ra) {
    // System.out.println("-> update_visible_y()");

    // 검색어, 페이지 유지
    this.cateProc.update_visible_y(cateno); // visible 컬럼을 'Y'로 변경 (공개 처리)

    ra.addAttribute("word", word); // redirect로 데이터 전송, 한글 깨짐 방지
    ra.addAttribute("now_page", now_page); // redirect로 데이터 전송, 한글 깨짐 방지

    return "redirect:/cate/list_search"; // @GetMapping(value="/list_search")
    // 목록 페이지 새로고침
  }

  /**
   * [GET] 카테고리 비공개 설정 (visible = 'N') 관리자에게만 보이고 일반 사용자에겐 숨김 검색어 및 현재 페이지 정보 유지하며 목록으로
   * 리다이렉트 http://localhost:9093/cate/update_visible_n/1
   */
  @GetMapping(value = "/update_visible_n/{cateno}")
  public String update_visible_n(Model model, @PathVariable("cateno") Integer cateno,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page, RedirectAttributes ra) {

    // System.out.println("-> update_visible_n()");

    this.cateProc.update_visible_n(cateno); // visible 컬럼을 'N'으로 변경 (비공개 처리)

    // 검색어, 페이지 유지
    ra.addAttribute("word", word); // redirect로 데이터 전송, 한글 깨짐 방지
    ra.addAttribute("now_page", now_page); // redirect로 데이터 전송, 한글 깨짐 방지

    return "redirect:/cate/list_search"; // @GetMapping(value="/list_search")
    // 목록 페이지 새로고침
  }

  /**
   * [GET] 카테고리 cnt 동기화
   * URL: /cate/update_cnt
   * 중분류 자료수 갱신
   * 대분류 자료수 갱신
  */
  @GetMapping("/update_cnt")
  @ResponseBody
  public String updateCnt() {
    cateProc.updateMidCnt(); // 중분류 cnt 갱신
    cateProc.updateMainCnt(); // 대분류 cnt 갱신
    return "카테고리 cnt 동기화 완료"; // 수정해야함
  }

}
