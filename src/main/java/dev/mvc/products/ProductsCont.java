package dev.mvc.products;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.Cookie;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dev.mvc.cate.CateProcInter;
import dev.mvc.cate.CateVO;
import dev.mvc.cate.CateVOMenu;
import dev.mvc.productsgood.ProductsgoodVO;
import dev.mvc.review.ReviewMemberVO;
import dev.mvc.review.ReviewProcInter;
import dev.mvc.review.ReviewVO;
import dev.mvc.productsgood.ProductsgoodProcInter;
import dev.mvc.member.MemberProcInter;
import dev.mvc.tool.LLMKey;
import dev.mvc.tool.Tool;
import dev.mvc.tool.Upload;

@RequestMapping(value = "/products")
@Controller
public class ProductsCont {
  @Autowired
  @Qualifier("dev.mvc.member.MemberProc") // @Service("dev.mvc.member.MemberProc")
  private MemberProcInter memberProc;

  @Autowired
  @Qualifier("dev.mvc.cate.CateProc") // @Component("dev.mvc.cate.CateProc")
  private CateProcInter cateProc;

  @Autowired
  @Qualifier("dev.mvc.products.ProductsProc") // @Component("dev.mvc.products.ProductsProc")
  private ProductsProcInter productsProc;

  @Autowired
  @Qualifier("dev.mvc.review.ReviewProc")
  private ReviewProcInter reviewProc;

  
  @Autowired
  @Qualifier("dev.mvc.productsgood.ProductsgoodProc") // @Component("dev.mvc.productsgood.ProductsgoodProc")
  ProductsgoodProcInter productsgoodProc;
  //외부 API 요청을 위한 RestTemplate 주입
  private final RestTemplate restTemplate;
  //생성자 주입 방식으로 RestTemplate 초기화
  public ProductsCont(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
    System.out.println("-> this.restTemplate hashCode: " + this.restTemplate.hashCode());
    System.out.println("-> ProductsCont created.");
  }

  /*
   * 20250619 추가 
   */
  @PostConstruct
  public void autoCntUpdate() {
    cateProc.updateMidCnt();   // 중분류 cnt 갱신
    cateProc.updateMainCnt();  // 대분류 cnt 갱신
    System.out.println("→ 자동 카테고리 cnt 동기화 완료");
  }
  
  /**
   *  POST 요청 후 새로고침 시 중복 등록 방지를 위한 GET 중간 경유지
   * POST 요청 처리 → redirect → url → GET → forward -> html 데이터
   * 메뉴 목록을 모델에 담고, 지정된 url로 forward
   * 전송
   * 
   * @return
   */
  @GetMapping(value = "/post2get")
  public String post2get(Model model, @RequestParam(name="url", defaultValue = "") String url) {
    ArrayList<CateVOMenu> menu = this.cateProc.menu();  // 카테고리 메뉴 가져오기
    model.addAttribute("menu", menu);

    return url; // forward 방식으로 지정된 페이지로 이동, /templates/products/msg.html
  }

  
  // 등록 폼, products 테이블은 FK로 cateno를 사용함.
  // http://localhost:9093/products/create X
  // http://localhost:9093/products/create?cateno=1 // cateno 변수값을 보내는 목적
  // http://localhost:9093/products/create?cateno=2
  // http://localhost:9093/products/create?cateno=5
  @GetMapping(value = "/create")
  public String create(HttpSession session, Model model, 
                            @ModelAttribute("productsVO") ProductsVO productsVO, 
                            @RequestParam(name="cateno", defaultValue="0") int cateno,
                            RedirectAttributes ra) {
    // 등급 체크
    String grade = (String) session.getAttribute("grade");
    if (!"admin".equals(grade) && !"supplier".equals(grade)) {
      ra.addFlashAttribute("code", "no_permission");
      ra.addFlashAttribute("url", "/products/create?cateno=" + cateno);
      return "redirect:/member/login_cookie_need"; // msg.html 또는 권한 요청 페이지로
    }
    
    // 상단 카테고리 메뉴 출력용 데이터
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);

    // 현재 선택한 카테고리의 정보를 화면에 출력하기 위해 가져옴
    CateVO cateVO = this.cateProc.read(cateno); // 카테고리 정보를 출력하기위한 목적
    model.addAttribute("cateVO", cateVO);

    return "products/create"; // /templates/products/create.html
    //return "products/create_ai"; // /templates/products/create_ai.html
  }
  
  /**
   * 등록 처리 http://localhost:9093/products/create
   * 
   * @return
   */
  @PostMapping(value = "/create")
  public String create_proc(HttpServletRequest request, 
                                    HttpSession session, 
                                    Model model, 
                                    @ModelAttribute("productsVO") ProductsVO productsVO,
                                    RedirectAttributes ra) {

    String grade = (String) session.getAttribute("grade");
    if ("admin".equals(grade) || "supplier".equals(grade)) { // 관리자로 로그인한경우
      // ------------------------------------------------------------------------------
      // 파일 전송 코드 시작
      // =============== (1) 파일 업로드 처리 시작 ===============
      // ------------------------------------------------------------------------------

      String upDir = Products.getUploadDir(); 
      System.out.println("-> upDir: " + upDir);

      // ---------------------- file1 처리 ----------------------
      MultipartFile mf1 = productsVO.getFile1MF();
      String file1 = mf1.getOriginalFilename();
      String file1saved = "", thumb1 = "";
      long size1 = mf1.getSize();

      if (size1 > 0 && Tool.checkUploadFile(file1)) {
        file1saved = Upload.saveFileSpring(mf1, upDir);
        if (Tool.isImage(file1saved)) {
          thumb1 = Tool.preview(upDir, file1saved, 100, 150);
        }
        productsVO.setFile1(file1);
        productsVO.setFile1saved(file1saved);
        productsVO.setThumb1(thumb1);
        productsVO.setSize1(size1);
      } else {
     // ❗ 업로드하지 않은 경우 → 기본 이미지 복사
        String defaultDir = "C:/kd/ws_java/team7_v2sbm3c/src/main/resources/static/products/images/";
        String targetDir = upDir;

        Tool.copyFile(defaultDir + "default.png", targetDir + "default.png");
        Tool.copyFile(defaultDir + "default_thumb.png", targetDir + "default_thumb.png");

        file1 = "default.png";
        file1saved = "default.png";
        thumb1 = "default_thumb.png";
        size1 = 0L;

        productsVO.setFile1(file1);
        productsVO.setFile1saved(file1saved);
        productsVO.setThumb1(thumb1);
        productsVO.setSize1(size1);
      }

      // ---------------------- file2 처리 ----------------------
   // ---------------------- file2 처리 ----------------------
      MultipartFile mf2 = productsVO.getFile2MF();
      String file2 = mf2.getOriginalFilename();
      String file2saved = "";
      long size2 = mf2.getSize();

      if (size2 > 0 && Tool.checkUploadFile(file2)) {
        file2saved = Upload.saveFileSpring(mf2, upDir); // 썸네일 생성 X
        productsVO.setFile2(file2);
        productsVO.setFile2saved(file2saved);
        productsVO.setSize2(size2);
      } else {
        String defaultDir = "C:/kd/ws_java/team7_v2sbm3c/src/main/resources/static/products/images/";
        String targetDir = upDir;

        Tool.copyFile(defaultDir + "default.png", targetDir + "default.png");

        file2 = "default.png";
        file2saved = "default.png";
        size2 = 0L;

        productsVO.setFile2(file2);
        productsVO.setFile2saved(file2saved);
        productsVO.setSize2(size2);
      }
      //---------------------------------------------
   // ---------------------- file3 처리 ----------------------
      MultipartFile mf3 = productsVO.getFile3MF();
      String file3 = mf3.getOriginalFilename();
      String file3saved = "";
      long size3 = mf3.getSize();

      if (size3 > 0 && Tool.checkUploadFile(file3)) {
        file3saved = Upload.saveFileSpring(mf3, upDir); // 썸네일 생성 X
        productsVO.setFile3(file3);
        productsVO.setFile3saved(file3saved);
        productsVO.setSize3(size3);
      } else {
        String defaultDir = "C:/kd/ws_java/team7_v2sbm3c/src/main/resources/static/products/images/";
        String targetDir = upDir;

        Tool.copyFile(defaultDir + "default.png", targetDir + "default.png");

        file3 = "default.png";
        file3saved = "default.png";
        size3 = 0L;

        productsVO.setFile3(file3);
        productsVO.setFile3saved(file3saved);
        productsVO.setSize3(size3);
      }



   // ---------------------- fileAd 처리 ----------------------
      MultipartFile mfAd = productsVO.getFileAdMF();
      String fileAd = mfAd.getOriginalFilename();
      String fileAdsaved = "";
      long sizeAd = mfAd.getSize();

      if (sizeAd > 0 && Tool.checkUploadFile(fileAd)) {
        fileAdsaved = Upload.saveFileSpring(mfAd, upDir);
        // 썸네일은 필요 없다면 생략 가능
        productsVO.setFileAd(fileAd);
        productsVO.setFileAdsaved(fileAdsaved);
        productsVO.setSizeAd(sizeAd);
      } else {
        // ❗ 업로드하지 않은 경우 → 기본 광고 이미지 복사
        String defaultDir = "C:/kd/ws_java/team7_v2sbm3c/src/main/resources/static/products/images/";
        String targetDir = upDir;

        Tool.copyFile(defaultDir + "default_ad.png", targetDir + "default_ad.png");

        fileAd = "default_ad.png";
        fileAdsaved = "default_ad.png";
        sizeAd = 0L;

        productsVO.setFileAd(fileAd);
        productsVO.setFileAdsaved(fileAdsaved);
        productsVO.setSizeAd(sizeAd);
      }
      // ------------------------------------------------------------------------------
      // 파일 전송 코드 종료
      // ------------------------------------------------------------------------------
      
      // =============== (2) 상품 DB 등록 처리 ===============
      // 로그인한 사용자의 memberno를 설정 (작성자 정보)
      // Call By Reference: 메모리 공유, Hashcode 전달
      int memberno = (int) session.getAttribute("memberno"); // memberno FK
      productsVO.setMemberno(memberno);
      // expdate 값 확인
      System.out.println("expdate: " + productsVO.getExpdate());
      // DB에 상품 등록
      int cnt = this.productsProc.create(productsVO);
      
      // 20250619 cnt 갱신 추가
      this.cateProc.updateMidCnt();   // 중분류: products 기준
      this.cateProc.updateMainCnt();  // 대분류: 중분류 합산 기준
      // =============== (2) DB 등록 처리 종료 ===============
      
      // =============== (3) 결과 처리 ===============
      if (cnt == 1) { // 등록 성공
        
        // 등록한 카테고리로 다시 목록 페이지 이동
        ra.addAttribute("cateno", productsVO.getCateno()); // controller -> controller: O
        return "redirect:/products/list_by_cateno_grid";


      } else { // 등록 실패
        ra.addFlashAttribute("code", Tool.CREATE_FAIL); // DBMS 등록 실패
        ra.addFlashAttribute("cnt", 0); // 업로드 실패
        ra.addFlashAttribute("url", "/products/msg"); // msg.html, redirect parameter 적용
        return "redirect:/products/msg"; // Post -> Get - param...
        // =============== (3) 결과 처리 종료 ===============
      }
    } else { // 로그인 실패 한 경우

      return "redirect:/member/login_cookie_need?url=/products/create?cateno=" + productsVO.getCateno(); 
    }
  }  


  /**
   * 0620 수정 후
   * 전체 목록, 관리자만 사용 가능 
   * http://localhost:9093/products/list_all
   */
  @GetMapping(value = "/list_all")
  public String list_all(HttpSession session, Model model) {
    // 상단 메뉴 구성
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);

    // 세션에서 권한 확인
    String grade = (String) session.getAttribute("grade");

    if ("admin".equals(grade)) {
      ArrayList<ProductsVO> list = this.productsProc.list_all();
      model.addAttribute("list", list);
      return "products/list_all"; // 관리자 전용 목록 페이지

    } else {
      // 접근 불가 → 로그인 안내
      return "redirect:/member/login_cookie_need?url=/products/list_all";
    }
  }


  /**
   * 카테고리별 목록 + 검색 + 페이징 http://localhost:9093/products/list_by_cateno?cateno=5
   * http://localhost:9093/products/list_by_cateno?cateno=6
   * 
   * @return
   */
  @GetMapping(value = "/list_by_cateno")
  public String list_by_cateno_search_paging(HttpSession session, Model model, 
      @RequestParam(name = "cateno", defaultValue = "0") int cateno,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {

    // System.out.println("-> cateno: " + cateno);

    // ----------------------------------------------
    // (1) 공통 메뉴 및 카테고리 정보 설정
    // ----------------------------------------------
    ArrayList<CateVOMenu> menu = this.cateProc.menu();  // 상단 카테고리 메뉴
    model.addAttribute("menu", menu);

    CateVO cateVO = this.cateProc.read(cateno);  // 현재 카테고리 정보
    model.addAttribute("cateVO", cateVO);

    // 검색어가 null일 경우를 방지하고 공백 제거
    word = Tool.checkNull(word).trim();

    // ----------------------------------------------
    // (2) 검색 및 페이징용 파라미터를 map으로 전달
    // ----------------------------------------------
    HashMap<String, Object> map = new HashMap<>();
    map.put("cateno", cateno);            // 카테고리 번호
    map.put("word", word);                // 검색어
    map.put("now_page", now_page);   // 현재 페이지

    // ----------------------------------------------
    // (3) DB에서 해당 카테고리 + 검색 조건에 맞는 상품 목록 가져오기
    // ----------------------------------------------
    ArrayList<ProductsVO> list = this.productsProc.list_by_cateno_search_paging(map);
    model.addAttribute("list", list);         // 검색된 상품 목록
    // System.out.println("-> size: " + list.size());
    model.addAttribute("word", word);   // 검색어 유지

    // ----------------------------------------------
    // (4) 전체 검색 결과 수 조회
    // ----------------------------------------------
    int search_count = this.productsProc.list_by_cateno_search_count(map);
    
    // ----------------------------------------------
    // (5) 페이징 HTML 코드 생성
    // 예: 1 2 3
    // ----------------------------------------------
    String paging = this.productsProc.pagingBox(cateno, now_page, word, "/products/list_by_cateno", search_count,
        Products.RECORD_PER_PAGE, Products.PAGE_PER_BLOCK);
    // 필터 조건, 페이징 링크 URL, 전체 검색 결과 수, 페이지당 출력 수, 블록당 페이지 수
    
    model.addAttribute("paging", paging); // 페이징 HTML 전달
    model.addAttribute("now_page", now_page);  // 현재 페이지 번호 전달

    model.addAttribute("search_count", search_count); // 검색 결과 수 전달

    // ----------------------------------------------
    // (6) 일련 변호 생성: 레코드 갯수 - ((현재 페이지수 -1) * 페이지당 레코드 수) (페이지 넘겨도 번호가 이어지도록)
    // ----------------------------------------------
    int no = search_count - ((now_page - 1) * Products.RECORD_PER_PAGE);
    model.addAttribute("no", no); // 시작 번호 전달 (예: 총 25건 중 1페이지 → 25부터 시작)

    // ----------------------------------------------
    // (7) 최종 view 파일 연결
    // ----------------------------------------------
    return "products/list_by_cateno_search_paging_grid"; // /templates/products/list_by_cateno_search_paging.html
  }

  /**
   * 카테고리별 목록 + 검색 + 페이징 + Grid
   * http://localhost:9093/products/list_by_cateno?cateno=5
   * http://localhost:9093/products/list_by_cateno?cateno=6
   * 검색 결과 + 페이징을 포함한 그리드 목록 화면
   * @return
   */
  @GetMapping(value = "/list_by_cateno_grid")
  public String list_by_cateno_search_paging_grid(HttpSession session, Model model, 
      @RequestParam(name = "cateno", defaultValue = "0") int cateno,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {

    // System.out.println("-> cateno: " + cateno);
    // ----------------------------------------------
    // (1) 상단 카테고리 메뉴 + 현재 카테고리 정보
    // ----------------------------------------------
    ArrayList<CateVOMenu> menu = this.cateProc.menu(); // 상단 메뉴 바 구성용 카테고리 목록
    model.addAttribute("menu", menu);

    CateVO cateVO = this.cateProc.read(cateno); // 현재 선택된 카테고리 정보
    model.addAttribute("cateVO", cateVO);

    // 검색어 null 방지 및 공백 제거
    word = Tool.checkNull(word).trim();

    // ----------------------------------------------
    // (2) 검색 + 페이징 조건 Map 구성
    // ----------------------------------------------
    HashMap<String, Object> map = new HashMap<>();
    map.put("cateno", cateno); // 카테고리 조건
    map.put("word", word); // 검색어 조건
    map.put("now_page", now_page);   // 현재 페이지 조건

    // ----------------------------------------------
    // (3) 상품 목록 조회 (검색 + 페이징 적용된 결과)
    // ----------------------------------------------
    ArrayList<ProductsVO> list = this.productsProc.list_by_cateno_search_paging(map);
    model.addAttribute("list", list); // 결과 리스트 전달
    // System.out.println("-> size: " + list.size());
    model.addAttribute("word", word);  // 검색어 전달 (검색창 유지용)

    // ----------------------------------------------
    // (4) 전체 검색 결과 수 및 페이징 처리
    // ----------------------------------------------
    int search_count = this.productsProc.list_by_cateno_search_count(map); // 총 레코드 수

    String paging = this.productsProc.pagingBox(cateno, now_page, word, "/products/list_by_cateno_grid", search_count,
        Products.RECORD_PER_PAGE, Products.PAGE_PER_BLOCK);
    // 필터 조건, 페이징 링크 URL, 전체 검색 결과 수, 페이지당 출력 수, 블록당 페이지 수
    
    model.addAttribute("paging", paging); // 페이징 HTML 전달
    model.addAttribute("now_page", now_page); // 현재 페이지 전달
    model.addAttribute("search_count", search_count); // 총 검색 결과 수 전달

    // ----------------------------------------------
    // (5) 화면에 출력할 시작 일련 변호 생성: 레코드 갯수 - ((현재 페이지수 -1) * 페이지당 레코드 수)
    // ex: 총 25건, 페이지당 10건 → 1페이지: 25~16 / 2페이지: 15~6 ...
    // ----------------------------------------------
    int no = search_count - ((now_page - 1) * Products.RECORD_PER_PAGE);
    model.addAttribute("no", no); // 일련번호 전달

    // ----------------------------------------------
    // (6) 최종 View 반환 (Grid 형태의 템플릿)
    // ----------------------------------------------
    // /templates/products/list_by_cateno_search_paging_grid.html
    return "products/list_by_cateno_search_paging_grid";
  }

  /**
   * 조회 http://localhost:9093/products/read?productsno=17
   * 수업 중 제작 코드
   * @return
   */
  @GetMapping(value = "/read")
  public String read(HttpSession session, Model model, 
      @RequestParam(name="productsno", defaultValue = "0") int productsno, // 조회할 상품 번호
      @RequestParam(name="word", defaultValue = "") String word, // 검색어 유지용
      @RequestParam(name="now_page", defaultValue = "1") int now_page) { // 현재 페이지

    // ---------------------------------------------
    // (1) 상단 메뉴 카테고리 데이터 설정
    // ---------------------------------------------
    ArrayList<CateVOMenu> menu = this.cateProc.menu(); // 상단 네비게이션 메뉴용 카테고리
    model.addAttribute("menu", menu);

    // ---------------------------------------------
    // (2) 상품 정보 가져오기
    // ---------------------------------------------
    ProductsVO productsVO = this.productsProc.read(productsno); // 상품 정보 조회

    // 파일 사이즈 단위 변환 (ex: 103400 → 101KB)
    long size1 = productsVO.getSize1();
    String size1_label = Tool.unit(size1);
    productsVO.setSize1_label(size1_label); // VO에 보기 좋게 가공한 크기 저장
    
    long size2 = productsVO.getSize2();
    String size2_label = Tool.unit(size2);
    productsVO.setSize2_label(size2_label); // VO에 보기 좋게 가공한 크기 저장
    
    long size3 = productsVO.getSize3();
    String size3_label = Tool.unit(size3);
    productsVO.setSize3_label(size3_label); // VO에 보기 좋게 가공한 크기 저장

    model.addAttribute("productsVO", productsVO); // 상품 정보 View로 전달
    model.addAttribute("productsno", productsno); // 리뷰 작성용 hidden 필드용
    

    // ---------------------------------------------
    // (3) 현재 상품이 속한 카테고리 정보 가져오기
    // ---------------------------------------------
    CateVO cateVO = this.cateProc.read(productsVO.getCateno()); // 상품의 카테고리 번호로 조회
    model.addAttribute("cateVO", cateVO); // 카테고리 정보 View에 전달
    
    // (3-1) 관련 상품 리스트
    List<ReviewMemberVO> reviewList = this.reviewProc.list_join_by_productsno(productsno); 
    model.addAttribute("reviewList", reviewList); 
    
    // 세션의 로그인 사용자 번호를 모델에 전달 (Thymeleaf에서 #session 접근 제거 대응)
    Integer sessionMemberno = (Integer) session.getAttribute("memberno");
    model.addAttribute("sessionMemberno", sessionMemberno); 

    // 조회에서 화면 하단에 출력
    // ArrayList<ReplyVO> reply_list = this.replyProc.list_products(productsno);
    // mav.addObject("reply_list", reply_list);
    // ---------------------------------------------
    // (4) 검색 키워드 및 현재 페이지 값 유지 (뒤로가기 편의)
    // ---------------------------------------------
    model.addAttribute("word", word);
    model.addAttribute("now_page", now_page);
    
    // ---------------------------------------------
    // (5) 추천 여부 확인 (하트 아이콘 상태)
    // ---------------------------------------------
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("productsno", productsno);
    
    int hartCnt = 0; // 로그인하지 않음, 비회원, 추천하지 않음 (비회원 또는 추천 안 한 상태가 기본)
    
    if (session.getAttribute("memberno") != null ) { // 회원인 경우만 카운트 처리 (로그인한 경우)
      int memberno = (int)session.getAttribute("memberno");
      map.put("memberno", memberno);
      
      hartCnt = this.productsgoodProc.hartCnt(map); // 추천 여부 조회 (1: 추천함, 0: 아님)
    } 
    
    model.addAttribute("hartCnt", hartCnt); // 뷰에 전달 → 하트 UI 제어에 사용
    // -------------------------------------------------------------------------------------------
    
    // ---------------------------------------------
    // (6) 최종 View 지정
    // ---------------------------------------------
    return "products/read"; // /templates/products/read.html
    // return "products/read_ai";
  }
  
  /*
   * 무한스크롤
   */
  @PostMapping("/related_scroll")
  @ResponseBody
  public ArrayList<ProductsVO> related_scroll(
      @RequestParam("cateno") int cateno,
      @RequestParam("productsno") int productsno,
      @RequestParam("page") int page) {

    int start = (page - 1) * 12 + 1;
    int end = page * 12;

    Map<String, Object> map = new HashMap<>();
    map.put("cateno", cateno);
    map.put("productsno", productsno);
    map.put("start", start);
    map.put("end", end);

    return productsProc.related_scroll(map); // JSON 자동 변환
  }

  
  /**
   * 맵 등록/수정/삭제 폼 http://localhost:9093/products/map?productsno=1
   * @param productsno 조회할 상품 번호
   * @return /templates/products/map.html 화면 반환
   */
  @GetMapping(value = "/map")
  public String map(Model model, 
                            @RequestParam(name="productsno", defaultValue = "0") int productsno,
                            @RequestParam(name="now_page", required = false) Integer now_page,
                            @RequestParam(name="word", required = false) String word) {
    // 아래두 2줄 추가함
    if (now_page == null) now_page = 1;
    if (word == null) word = "";
    // 상단 카테고리 메뉴 구성 (네비게이션 바용)
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);

    // 상품 번호에 해당하는 상품 정보 조회 (map 정보 포함)
    ProductsVO productsVO = this.productsProc.read(productsno); // map 정보 읽어 오기
    model.addAttribute("productsVO", productsVO); // request.setAttribute("productsVO", productsVO);
    // 상품 정보 View로 전달

    // 해당 상품이 속한 카테고리 정보 조회
    CateVO cateVO = this.cateProc.read(productsVO.getCateno()); // 그룹 정보 읽기
    model.addAttribute("cateVO", cateVO);   // 카테고리 정보 View로 전달
    
    // 페이지 정보 및 검색어도 View에 넘김 (링크 유지용) 이것도 2줄 추가함
    model.addAttribute("now_page", now_page);
    model.addAttribute("word", word);
    
    // 지도 편집 화면으로 이동
    return "products/map";
  }

  /**
   * MAP 등록/수정/삭제 처리 http://localhost:9093/products/map
   * 폼에서 입력한 map 내용을 DB에 저장 또는 수정 또는 삭제 처리
   * @param productsVO
   * @return
   */
  @PostMapping(value = "/map")
  public String map_update(Model model, 
                                      @RequestParam(name="productsno", defaultValue = "0") int productsno,
                                      @RequestParam(name="map", defaultValue = "") String map,
                                      @RequestParam(name="now_page", required = false) Integer now_page,
                                      @RequestParam(name="word", required = false) String word,
                                      RedirectAttributes ra) {
    // 기본값 지정 2줄 추가
    if (now_page == null) now_page = 1;
    if (word == null) word = "";
  
    // 파라미터를 Map 형태로 포장 
    HashMap<String, Object> hashMap = new HashMap<String, Object>();
    hashMap.put("productsno", productsno); // 어떤 상품에 대해 적용할지
    hashMap.put("map", map);  // 입력된 지도 HTML 코드

    // DB에 map 컬럼 업데이트
    this.productsProc.map(hashMap); // 실제 등록/수정/삭제 처리 메서드 호출

    // 리디렉션 시 페이지와 검색어 정보 유지 3줄 추가
    ra.addAttribute("productsno", productsno);
    ra.addAttribute("now_page", now_page);
    ra.addAttribute("word", word);
    // 작업 완료 후 다시 상품 상세 페이지로 리디렉션
    return "redirect:/products/read?productsno=" + productsno;
  }

  /**
   * Youtube 등록/수정/삭제 폼 http://localhost:9093/products/youtube?productsno=1
   * 
   * @return
   */
  @GetMapping(value = "/youtube")
  public String youtube(Model model,
      @RequestParam(name="productsno", defaultValue = "0") int productsno, 
      @RequestParam(name="word", defaultValue = "") String word, 
      @RequestParam(name="now_page", defaultValue = "1") int now_page) {
    
    // 상단 메뉴 구성
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);
    
    // 상품 정보 조회 (youtube 포함)
    ProductsVO productsVO = this.productsProc.read(productsno);
    model.addAttribute("productsVO", productsVO); // request.setAttribute("productsVO", productsVO);
    // 상품 정보 전달

    // 해당 상품이 속한 카테고리 정보 조회
    CateVO cateVO = this.cateProc.read(productsVO.getCateno());
    model.addAttribute("cateVO", cateVO); // 카테고리 정보 전달

    // 검색어 및 페이지 정보 유지
    model.addAttribute("word", word);
    model.addAttribute("now_page", now_page);
    
    // 유튜브 등록/수정 폼으로 이동
    return "products/youtube";  // forward
  }

  /**
   * Youtube 등록/수정/삭제 처리 http://localhost:9093/products/youtube
   * 
   * @param productsVO
   * @return
   */
  @PostMapping(value = "/youtube")
  public String youtube_update(Model model, 
                                             RedirectAttributes ra,
                                             @RequestParam(name="productsno", defaultValue = "0") int productsno, 
                                             @RequestParam(name="youtube", defaultValue = "") String youtube, 
                                             @RequestParam(name="word", defaultValue = "") String word, 
                                             @RequestParam(name="now_page", defaultValue = "1") int now_page) {

    // 입력된 유튜브 코드가 있다면 크기 조정
    if (youtube.trim().length() > 0) { // 삭제 중인지 확인, 삭제가 아니면 youtube 크기 변경
      youtube = Tool.youtubeResize(youtube, 640); // youtube 영상의 크기를 width 기준 640 px로 변경
    }

    // DB 업데이트를 위한 Map 구성
    HashMap<String, Object> hashMap = new HashMap<String, Object>();
    hashMap.put("productsno", productsno); // 어떤 상품인지
    hashMap.put("youtube", youtube); // 수정된 유튜브 코드

    // DB 업데이트 실행
    this.productsProc.youtube(hashMap);
    
    // 리디렉션 시 검색어, 페이지 상태 유지
    ra.addAttribute("productsno", productsno);
    ra.addAttribute("word", word);
    ra.addAttribute("now_page", now_page);

    // 등록 후 해당 상품 상세 페이지로 이동
    // return "redirect:/products/read?productsno=" + productsno;
    return "redirect:/products/read";
  }
  
  
  @GetMapping(value = "/update_text")
  public String update_text(HttpSession session, Model model, RedirectAttributes ra,
                                   @RequestParam(name="productsno", defaultValue = "0") int productsno, 
                                   @RequestParam(name="word", defaultValue = "") String word,
                                   @RequestParam(name="now_page", defaultValue = "1") int now_page) {
    
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);

    model.addAttribute("word", word);
    model.addAttribute("now_page", now_page);

    String grade = (String) session.getAttribute("grade");
    Integer memberno = (Integer) session.getAttribute("memberno");

    ProductsVO productsVO = this.productsProc.read(productsno);
    model.addAttribute("productsVO", productsVO);

    CateVO cateVO = this.cateProc.read(productsVO.getCateno());
    model.addAttribute("cateVO", cateVO);

    if ("admin".equals(grade)) {
      return "products/update_text";

    } else if ("supplier".equals(grade)) {
      if (productsVO.getMemberno() == memberno) {
        return "products/update_text";
      } else {
        // 다른 사람이 올린 상품은 수정 불가
        return "redirect:/products/list_by_cateno?cateno=" + productsVO.getCateno();
      }

    } else {
      // 게스트 또는 비로그인 사용자
      return "redirect:/member/login_cookie_need?url=/products/update_text?productsno=" + productsno;
    }
  }
  
  @PostMapping(value = "/update_text")
  public String update_text_proc(HttpSession session, Model model, ProductsVO productsVO, RedirectAttributes ra,
                                          @RequestParam(name="search_word", defaultValue = "") String search_word,
                                          @RequestParam(name="now_page", defaultValue = "1") int now_page) {
    
    ra.addAttribute("word", search_word);
    ra.addAttribute("now_page", now_page);

    String grade = (String) session.getAttribute("grade");
    Integer sessionMemberno = (Integer) session.getAttribute("memberno");

    // 비밀번호 확인
    HashMap<String, Object> map = new HashMap<>();
    map.put("productsno", productsVO.getProductsno());
    map.put("passwd", productsVO.getPasswd());

    if (this.productsProc.password_check(map) != 1) {
      ra.addFlashAttribute("code", Tool.PASSWORD_FAIL);
      ra.addFlashAttribute("cnt", 0);
      model.addAttribute("productsVO", productsVO);
      return "redirect:/products/post2get";
    }

    // 권한 확인
    ProductsVO dbVO = this.productsProc.read(productsVO.getProductsno());

    if ("admin".equals(grade) || ("supplier".equals(grade) && dbVO.getMemberno() == sessionMemberno)) {
      int cnt = this.productsProc.update_text(productsVO);
      ra.addAttribute("productsno", productsVO.getProductsno());
      ra.addAttribute("cateno", productsVO.getCateno());
      return "redirect:/products/read";
    }

    // 그 외는 수정 불가
    return "redirect:/products/list_by_cateno_search_paging?cateno=" + productsVO.getCateno() + "&now_page=" + now_page + "&word=" + search_word;
  }

  
  @GetMapping(value = "/update_file")
  public String update_file(HttpSession session, Model model, 
                                  @RequestParam(name="productsno", defaultValue = "0") int productsno, 
                                  @RequestParam(name="word", defaultValue = "") String word,
                                  @RequestParam(name="now_page", defaultValue = "1") int now_page) {
    
    String grade = (String) session.getAttribute("grade");
    Integer memberno = (Integer) session.getAttribute("memberno");

    // 상단 메뉴
    model.addAttribute("menu", this.cateProc.menu());

    model.addAttribute("word", word);
    model.addAttribute("now_page", now_page);

    ProductsVO productsVO = this.productsProc.read(productsno);
    model.addAttribute("productsVO", productsVO);

    CateVO cateVO = this.cateProc.read(productsVO.getCateno());
    model.addAttribute("cateVO", cateVO);

    if ("admin".equals(grade)) {
      return "products/update_file";

    } else if ("supplier".equals(grade) && memberno != null && memberno.equals(productsVO.getMemberno())) {
      return "products/update_file";

    } else {
      return "redirect:/member/login_cookie_need?url=/products/update_file?productsno=" + productsno;
    }
  }

  @PostMapping(value = "/update_file")
  public String update_file_proc(HttpSession session, Model model, RedirectAttributes ra,
      ProductsVO productsVO,
      @RequestParam(name="word", defaultValue = "") String word,
      @RequestParam(name="now_page", defaultValue = "1") int now_page
  ) {
    String grade = (String) session.getAttribute("grade");
    Integer sessionMemberno = (Integer) session.getAttribute("memberno");

    ProductsVO productsVO_old = this.productsProc.read(productsVO.getProductsno());

    boolean authorized = false;

    if ("admin".equals(grade)) {
      authorized = true;
    } else if ("supplier".equals(grade) && sessionMemberno != null && sessionMemberno.equals(productsVO_old.getMemberno())) {
      authorized = true;
    }

    if (!authorized) {
      ra.addAttribute("url", "/member/login_cookie_need");
      return "redirect:/products/post2get";
    }

    String upDir = Products.getUploadDir();
    
    // 기존 파일 삭제 - file1
    String file1saved = productsVO_old.getFile1saved();
    String thumb1 = productsVO_old.getThumb1();
    Tool.deleteFile(upDir, file1saved);
    Tool.deleteFile(upDir, thumb1);
    
    // 기존 파일 삭제 - file2
    String file2saved = productsVO_old.getFile2saved();
    Tool.deleteFile(upDir, file2saved);

    // 기존 파일 삭제 - file3
    String file3saved = productsVO_old.getFile3saved();
    Tool.deleteFile(upDir, file3saved);

    // 새 파일 업로드
    MultipartFile mf = productsVO.getFile1MF();
    String file1 = mf.getOriginalFilename();
    long size1 = mf.getSize();

    if (size1 > 0) {
      file1saved = Upload.saveFileSpring(mf, upDir);
      if (Tool.isImage(file1saved)) {
        thumb1 = Tool.preview(upDir, file1saved, 250, 200);
      }
    } else {
      file1 = "";
      file1saved = "";
      thumb1 = "";
      size1 = 0;
    }

    productsVO.setFile1(file1);
    productsVO.setFile1saved(file1saved);
    productsVO.setThumb1(thumb1);
    productsVO.setSize1(size1);
    //-----------------------------------------------
    // 새 파일 업로드 및 설정: file2
    //-----------------------------------------------
    MultipartFile mf2 = productsVO.getFile2MF();
    String file2 = mf2.getOriginalFilename();
    long size2 = mf2.getSize();
  
    if (size2 > 0) {
      file2saved = Upload.saveFileSpring(mf2, upDir);
    } else {
      file2 = "";
      file2saved = "";
      size2 = 0;
    }
    productsVO.setFile2(file2);
    productsVO.setFile2saved(file2saved);
    productsVO.setSize2(size2);  // ✅ thumb2 제거
  
    //-----------------------------------------------
    // 새 파일 업로드 및 설정: file3
    //-----------------------------------------------
    MultipartFile mf3 = productsVO.getFile3MF();
    String file3 = mf3.getOriginalFilename();
    long size3 = mf3.getSize();
  
    if (size3 > 0) {
      file3saved = Upload.saveFileSpring(mf3, upDir);
    } else {
      file3 = "";
      file3saved = "";
      size3 = 0;
    }
    productsVO.setFile3(file3);
    productsVO.setFile3saved(file3saved);
    productsVO.setSize3(size3);  //  thumb3 제거

   
    // 기존 광고 이미지 삭제 - fileAd
    String fileAdsaved = productsVO_old.getFileAdsaved();
    Tool.deleteFile(upDir, fileAdsaved);
    //-----------------------------------------------
    // 새 파일 업로드 및 설정: fileAd (광고 이미지)
    //-----------------------------------------------
    MultipartFile mfAd = productsVO.getFileAdMF();
    String fileAd = mfAd.getOriginalFilename();
    long sizeAd = mfAd.getSize();
    
    if (sizeAd > 0 && Tool.checkUploadFile(fileAd)) {
      fileAdsaved = Upload.saveFileSpring(mfAd, upDir);
    } else {
      fileAd = "";
      fileAdsaved = "";
      sizeAd = 0;
    }
    productsVO.setFileAd(fileAd);
    productsVO.setFileAdsaved(fileAdsaved);
    productsVO.setSizeAd(sizeAd);



    
    this.productsProc.update_file(productsVO);

    ra.addAttribute("productsno", productsVO.getProductsno());
    ra.addAttribute("cateno", productsVO.getCateno());
    ra.addAttribute("word", word);
    ra.addAttribute("now_page", now_page);

    return "redirect:/products/read";
  }


  @GetMapping(value = "/delete")
  public String delete(HttpSession session, Model model, RedirectAttributes ra,
      @RequestParam(name = "cateno", defaultValue = "0") int cateno, 
      @RequestParam(name = "productsno", defaultValue = "0") int productsno, 
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {

    String grade = (String) session.getAttribute("grade");
    Integer sessionMemberno = (Integer) session.getAttribute("memberno");

    ProductsVO productsVO = this.productsProc.read(productsno);
    int ownerMemberno = productsVO.getMemberno();

    boolean authorized = "admin".equals(grade) || 
                         ("supplier".equals(grade) && sessionMemberno != null && sessionMemberno == ownerMemberno);

    if (authorized) {
      model.addAttribute("cateno", cateno);
      model.addAttribute("word", word);
      model.addAttribute("now_page", now_page);
      model.addAttribute("menu", this.cateProc.menu());
      model.addAttribute("productsVO", productsVO);
      model.addAttribute("cateVO", this.cateProc.read(productsVO.getCateno()));

      return "products/delete"; // forward
    } else {
      return "redirect:/member/login_cookie_need?url=/products/delete?productsno=" + productsno;
    }
  }

  @PostMapping(value = "/delete")
  public String delete_proc(HttpSession session, RedirectAttributes ra,
      @RequestParam(name = "cateno", defaultValue = "0") int cateno, 
      @RequestParam(name = "productsno", defaultValue = "0") int productsno, 
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {

    String grade = (String) session.getAttribute("grade");
    Integer sessionMemberno = (Integer) session.getAttribute("memberno");

    ProductsVO productsVO_read = this.productsProc.read(productsno);
    int ownerMemberno = productsVO_read.getMemberno();

    boolean authorized = "admin".equals(grade) || 
                         ("supplier".equals(grade) && sessionMemberno != null && sessionMemberno == ownerMemberno);

    if (!authorized) {
      ra.addAttribute("url", "/member/login_cookie_need"); 
      return "redirect:/products/post2get";
    }
    // 1. 파일 삭제
    String upDir = Products.getUploadDir();
    // 1. 파일 삭제
    Tool.deleteFile(Products.getUploadDir(), productsVO_read.getFile1saved());
    Tool.deleteFile(Products.getUploadDir(), productsVO_read.getThumb1());
    // 이미지 2
    Tool.deleteFile(upDir, productsVO_read.getFile2saved());

    // 이미지 3
    Tool.deleteFile(upDir, productsVO_read.getFile3saved());
    
    // 광고 이미지 삭제
    Tool.deleteFile(upDir, productsVO_read.getFileAdsaved());
    

    // 2. DB 삭제
    this.productsProc.delete(productsno);
    this.cateProc.updateMidCnt();
    this.cateProc.updateMainCnt();

    // 3. 페이지 보정
    HashMap<String, Object> map = new HashMap<>();
    map.put("cateno", cateno);
    map.put("word", word);

    int count = this.productsProc.list_by_cateno_search_count(map);
    if (count % Products.RECORD_PER_PAGE == 0) {
      now_page = Math.max(1, now_page - 1);
    }

    // 4. 목록 페이지 리디렉션
    ra.addAttribute("cateno", cateno);
    ra.addAttribute("word", word);
    ra.addAttribute("now_page", now_page);

    return "redirect:/products/list_by_cateno";
  }

  /**
   * 상품 추천/추천 해제 처리 (AJAX 방식)
   * @param json_src {"productsno": 38}
   * @return JSON 응답 (추천 여부, 총 추천 수)
   */
  @PostMapping(value = "/good")
  @ResponseBody
  public String good(HttpSession session, Model model, @RequestBody String json_src){ 
    System.out.println("-> json_src: " + json_src);
    
    JSONObject src = new JSONObject(json_src);
    int productsno = Integer.parseInt(src.get("productsno").toString()); // 🔥 핵심 수정 포인트
    System.out.println("-> productsno: " + productsno);

    JSONObject json = new JSONObject();
    
    // 세션에 grade 문자열이 저장되어 있음 ("admin", "member", "guest")
    String grade = (String) session.getAttribute("grade");
    Integer memberno = (Integer) session.getAttribute("memberno");

    // 로그인 여부만 판단 (등급 관계없이 모든 회원 허용)
    if (memberno != null && grade != null && 
        (grade.equals("admin") || grade.equals("member") || grade.equals("guest") || grade.equals("supplier") || grade.equals("user"))) {
      
      ProductsgoodVO vo = this.productsgoodProc.readByProductsnoMemberno(productsno, memberno);
      int hartCnt = 0;
      int recom = 0;

      if (vo == null) {
        // 추천 등록
        ProductsgoodVO newVO = new ProductsgoodVO();
        newVO.setProductsno(productsno);
        newVO.setMemberno(memberno);
        this.productsgoodProc.create(newVO);
        this.productsProc.increaseRecom(productsno);
        hartCnt = 1;
      } else {
        // 추천 해제
        this.productsgoodProc.deleteByProductsnoMemberno(productsno, memberno);
        this.productsProc.decreaseRecom(productsno);
      }

      recom = this.productsProc.read(productsno).getRecom();

      json.put("isMember", 1);
      json.put("hartCnt", hartCnt);
      json.put("recom", recom);
    } else {
      // 로그인 안 한 경우
      json.put("isMember", 0);
    }

    return json.toString();  // 🔥 필수 리턴
  }
  
  /*
   * 20250619 추가 
   */
  @GetMapping("/update_cnt")
  @ResponseBody
  public String updateCnt() {
    cateProc.updateMidCnt();
    cateProc.updateMainCnt();
    return "카테고리 cnt 동기화 완료";
  }
  
  @ResponseBody
  @GetMapping("/good_ajax")
  public List<ProductsVO> goodAjax(HttpSession session) {
      Integer memberno = (Integer) session.getAttribute("memberno");
      if (memberno == null) {
          return Collections.emptyList();
      }

      return productsgoodProc.getProductsgoodByMember(memberno); // 찜한 상품 목록
  }


  @GetMapping("/detail")
  public String detail(@RequestParam("productsno") int productsno, Model model) {
      ProductsVO productsVO = productsProc.read(productsno);  // 상품 정보 조회
      model.addAttribute("productsVO", productsVO);

      return "/products/detail";  // templates/products/detail.html
  }

}
