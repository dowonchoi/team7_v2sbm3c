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
import org.springframework.web.bind.annotation.CrossOrigin;
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

  // 외부 API 요청을 위한 RestTemplate 주입
  private final RestTemplate restTemplate;

  // 생성자 주입 방식으로 RestTemplate 초기화
  public ProductsCont(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
    System.out.println("-> this.restTemplate hashCode: " + this.restTemplate.hashCode());
    System.out.println("-> ProductsCont created.");
  }

  /*
   * PostConstruct → 카테고리 cnt 동기화
   */
  @PostConstruct
  public void autoCntUpdate() {
    cateProc.updateMidCnt(); // 중분류 cnt 갱신
    cateProc.updateMainCnt(); // 대분류 cnt 갱신
    System.out.println("→ 자동 카테고리 cnt 동기화 완료");
  }

  /**
   * [GET] 등록 폼 POST 요청 후 새로고침 시 중복 등록 방지를 위한 GET 중간 경유지 POST 요청 처리 → redirect →
   * url → GET → forward -> html 데이터 메뉴 목록을 모델에 담고, 지정된 url로 forward 전송
   * 
   * @return
   */
  @GetMapping(value = "/post2get")
  public String post2get(Model model, @RequestParam(name = "url", defaultValue = "/products/msg") String url) {
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);

    return url; // forward 방식으로 지정된 페이지로 이동
  }

  // 등록 폼, products 테이블은 FK로 cateno를 사용함.
  @GetMapping(value = "/create")
  public String create(HttpSession session, Model model, @ModelAttribute("productsVO") ProductsVO productsVO,
      @RequestParam(name = "cateno", defaultValue = "0") int cateno, RedirectAttributes ra) {

    // 로그인 등급 확인
    Object gradeObj = session.getAttribute("grade");
    int grade = (gradeObj != null) ? (Integer) gradeObj : 0;

    if (grade < 1 || grade > 15) { // 관리자/공급자가 아닌 경우 접근 불가
      ra.addFlashAttribute("code", "no_permission");
      ra.addFlashAttribute("url", "/products/create?cateno=" + cateno);
      return "redirect:/member/login_cookie_need";
    }

    // 메뉴/카테고리 데이터 전달
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);
    CateVO cateVO = this.cateProc.read(cateno);
    model.addAttribute("cateVO", cateVO);

    return "products/create";
  }

  /**
   * [POST] 등록 처리 - 파일 업로드 처리 (썸네일 포함) - 카테고리 cnt 자동 갱신 - 성공 시
   * list_by_cateno_grid로 redirect
   * 
   * @return
   */
  @PostMapping(value = "/create")
  public String create_proc(HttpServletRequest request, HttpSession session, Model model,
      @ModelAttribute("productsVO") ProductsVO productsVO, RedirectAttributes ra) {
    // 권한 체크
    Object gradeObj = session.getAttribute("grade");
    int grade = (gradeObj != null) ? (Integer) gradeObj : 0;

    if (grade >= 1 && grade <= 15) { // 관리자 또는 공급자

      // 파일 업로드 경로 설정
      String upDir = Products.getUploadDir(); // 상품 이미지 저장 디렉토리
      System.out.println("-> upDir: " + upDir);

      // file1 처리(대표 이미지)
      MultipartFile mf1 = productsVO.getFile1MF(); // 업로드된 파일 객체
      String file1 = mf1.getOriginalFilename(); // 원본 파일명
      String file1saved = "", thumb1 = ""; // 서버 저장 파일명, 썸네일 파일명
      long size1 = mf1.getSize(); // 파일 크기

      if (size1 > 0 && Tool.checkUploadFile(file1)) {
        // ① 유효한 파일 → 서버에 저장
        file1saved = Upload.saveFileSpring(mf1, upDir);

        // ② 이미지 파일이면 썸네일 생성
        if (Tool.isImage(file1saved)) {
          thumb1 = Tool.preview(upDir, file1saved, 100, 150);
        }
      } else {
        // ③ 업로드 안 한 경우 → 기본 이미지 설정
        String defaultDir = "C:/kd/ws_java/team7_v2sbm3c/src/main/resources/static/products/images/";
        String targetDir = upDir;

        // 기본 이미지 파일 복사
        Tool.copyFile(defaultDir + "default.png", targetDir + "default.png");
        Tool.copyFile(defaultDir + "default_thumb.png", targetDir + "default_thumb.png");

        file1 = "default.png";
        file1saved = "default.png";
        thumb1 = "default_thumb.png";
        size1 = 0L;
      }
      // VO에 세팅
      productsVO.setFile1(file1);
      productsVO.setFile1saved(file1saved);
      productsVO.setThumb1(thumb1);
      productsVO.setSize1(size1);

      // ---------------------- file2 처리 ----------------------
      MultipartFile mf2 = productsVO.getFile2MF();
      String file2 = mf2.getOriginalFilename();
      String file2saved = "";
      long size2 = mf2.getSize();

      if (size2 > 0 && Tool.checkUploadFile(file2)) {
        file2saved = Upload.saveFileSpring(mf2, upDir);
      } else {
        String defaultDir = "C:/kd/ws_java/team7_v2sbm3c/src/main/resources/static/products/images/";
        String targetDir = upDir;
        Tool.copyFile(defaultDir + "default.png", targetDir + "default.png");
        file2 = "default.png";
        file2saved = "default.png";
        size2 = 0L;
      }
      productsVO.setFile2(file2);
      productsVO.setFile2saved(file2saved);
      productsVO.setSize2(size2);

      // ---------------------- file3 처리 ----------------------
      MultipartFile mf3 = productsVO.getFile3MF();
      String file3 = mf3.getOriginalFilename();
      String file3saved = "";
      long size3 = mf3.getSize();

      if (size3 > 0 && Tool.checkUploadFile(file3)) {
        file3saved = Upload.saveFileSpring(mf3, upDir);
      } else {
        String defaultDir = "C:/kd/ws_java/team7_v2sbm3c/src/main/resources/static/products/images/";
        String targetDir = upDir;
        Tool.copyFile(defaultDir + "default.png", targetDir + "default.png");
        file3 = "default.png";
        file3saved = "default.png";
        size3 = 0L;
      }
      productsVO.setFile3(file3);
      productsVO.setFile3saved(file3saved);
      productsVO.setSize3(size3);

      // fileAd 처리 (광고 이미지)
      MultipartFile mfAd = productsVO.getFileAdMF(); // 업로드된 광고 이미지 파일
      String fileAd = mfAd.getOriginalFilename(); // 업로드된 원본 파일명
      String fileAdsaved = ""; // 서버에 저장될 파일명
      long sizeAd = mfAd.getSize(); // 파일 크기 (byte 단위)

      // 광고 이미지 업로드 여부 확인
      if (sizeAd > 0 && Tool.checkUploadFile(fileAd)) {
        // 업로드된 광고 이미지가 존재하고 확장자 검증 통과 시 → 저장
        fileAdsaved = Upload.saveFileSpring(mfAd, upDir);
      } else {
        // 업로드하지 않았거나 잘못된 확장자일 경우 → 기본 광고 이미지 사용
        String defaultDir = "C:/kd/ws_java/team7_v2sbm3c/src/main/resources/static/products/images/";
        String targetDir = upDir;
        // 기본 광고 이미지 복사
        Tool.copyFile(defaultDir + "default_ad.png", targetDir + "default_ad.png");
        // DB에 저장될 기본값 설정
        fileAd = "default_ad.png";
        fileAdsaved = "default_ad.png";
        sizeAd = 0L;
      }
      // VO에 광고 이미지 관련 정보 저장
      productsVO.setFileAd(fileAd);
      productsVO.setFileAdsaved(fileAdsaved);
      productsVO.setSizeAd(sizeAd);

      // 기타 데이터 세팅 시작

      // 로그인한 회원 번호 설정 (세션에서 가져옴)
      int memberno = (int) session.getAttribute("memberno");
      productsVO.setMemberno(memberno);

      // 체크박스 값이 null이면 기본값 'N'으로 설정
      if (productsVO.getIs_best() == null)
        productsVO.setIs_best("N");
      if (productsVO.getIs_new() == null)
        productsVO.setIs_new("N");
      if (productsVO.getIs_event() == null)
        productsVO.setIs_event("N");

      // DB 등록
      // 상품 정보를 DB에 저장 (products 테이블 INSERT)
      int cnt = this.productsProc.create(productsVO);

      // 카테고리 cnt 자동 갱신 (상품 등록 후 카테고리별 상품 수 동기화)
      this.cateProc.updateMidCnt(); // 중분류 갱신
      this.cateProc.updateMainCnt(); // 대분류 갱신

      // 처리 결과에 따른 응답
      if (cnt == 1) {
        // 등록 성공 → 해당 카테고리의 상품 목록 페이지로 Redirect
        ra.addAttribute("cateno", productsVO.getCateno());
        return "redirect:/products/list_by_cateno_grid";
      } else {
        // 등록 실패 → 메시지 페이지로 Redirect
        ra.addFlashAttribute("code", Tool.CREATE_FAIL);
        ra.addFlashAttribute("cnt", 0);
        ra.addFlashAttribute("url", "/products/msg");
        return "redirect:/products/msg";
      }

      // 권한 없음 → 로그인 필요 페이지로 Redirect
    } else {
      return "redirect:/member/login_cookie_need?url=/products/create?cateno=" + productsVO.getCateno();
    }
  }

  /**
   * [GET] 전체 상품 목록 출력 (관리자 전용) - 관리자 등급(grade 1~4)만 접근 가능 - 모든 상품 데이터를 가져와 리스트
   * 페이지에 출력 - 상단 메뉴(카테고리 메뉴)도 함께 모델에 담음
   * 
   * 요청 URL: http://localhost:9093/products/list_all
   * 
   * @param session 사용자 세션 (로그인 정보 및 권한 확인용)
   * @param model   뷰로 데이터 전달용 객체
   * @return 권한이 있으면 목록 페이지, 없으면 로그인 필요 페이지로 redirect
   */
  @GetMapping(value = "/list_all")
  public String list_all(HttpSession session, Model model) {
    // 상단 메뉴 구성
    // 카테고리 대/중분류 데이터를 가져와서 상단 네비게이션 메뉴로 출력
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);

    // 사용자 권한 확인 (세션에서 grade 값 추출)
    // grade: 1~4 → 관리자, 5~15 → 공급자, 16 이상 → 일반 회원
    Integer gradeObj = (Integer) session.getAttribute("grade");
    int grade = (gradeObj != null) ? gradeObj : 99;

    // 관리자 여부 검사
    if (grade >= 1 && grade <= 4) { // 관리자 권한
      // 관리자 권한일 경우
      // (1) 모든 상품 목록을 DB에서 조회
      ArrayList<ProductsVO> list = this.productsProc.list_all();
      // (2) 조회된 상품 리스트를 뷰에 전달
      model.addAttribute("list", list);

      // (3) 전체 목록 페이지로 forward (템플릿: /templates/products/list_all.html)
      return "products/list_all";
    } else {
      return "redirect:/member/login_cookie_need?url=/products/list_all";
    }
  }

  /**
   * [카테고리별 상품 목록 + 검색 + 페이징] - 카테고리 내 상품을 검색어 조건으로 필터링 - 페이징 처리 및 상단 메뉴, 카테고리 정보
   * 제공
   * 
   * @param cateno   카테고리 번호 (필수)
   * @param word     검색어 (옵션, 공백 허용)
   * @param now_page 현재 페이지 번호 (기본값 1)
   * @return list_by_cateno_search_paging_grid.html (Grid 기반 출력)
   */
  @GetMapping(value = "/list_by_cateno")
  public String list_by_cateno_search_paging(HttpSession session, Model model,
      @RequestParam(name = "cateno", defaultValue = "0") int cateno,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {
    // System.out.println("-> cateno: " + cateno);

    // (1) 공통 메뉴 및 카테고리 정보 설정
    ArrayList<CateVOMenu> menu = this.cateProc.menu(); // 상단 카테고리 메뉴
    model.addAttribute("menu", menu);

    CateVO cateVO = this.cateProc.read(cateno); // 현재 카테고리 정보
    model.addAttribute("cateVO", cateVO);

    // 검색어가 null일 경우를 방지하고 공백 제거
    word = Tool.checkNull(word).trim();

    // (2) 검색 및 페이징용 파라미터를 map으로 전달
    HashMap<String, Object> map = new HashMap<>();
    map.put("cateno", cateno); // 카테고리 번호
    map.put("word", word); // 검색어
    map.put("now_page", now_page); // 현재 페이지

    // (3) DB에서 해당 카테고리 + 검색 조건에 맞는 상품 목록 가져오기
    ArrayList<ProductsVO> list = this.productsProc.list_by_cateno_search_paging(map);
    model.addAttribute("list", list); // 검색된 상품 목록
    // System.out.println("-> size: " + list.size());
    model.addAttribute("word", word); // 검색어 유지

    // (4) 전체 검색 결과 수 조회
    int search_count = this.productsProc.list_by_cateno_search_count(map);

    // (5) 페이징 HTML 코드 생성
    // 예: 1 2 3
    String paging = this.productsProc.pagingBox(cateno, now_page, word, "/products/list_by_cateno", search_count,
        Products.RECORD_PER_PAGE, Products.PAGE_PER_BLOCK);
    // 필터 조건, 페이징 링크 URL, 전체 검색 결과 수, 페이지당 출력 수, 블록당 페이지 수

    model.addAttribute("paging", paging); // 페이징 HTML 전달
    model.addAttribute("now_page", now_page); // 현재 페이지 번호 전달
    model.addAttribute("search_count", search_count); // 검색 결과 수 전달

    // (6) 일련 변호 생성: 레코드 갯수 - ((현재 페이지수 -1) * 페이지당 레코드 수) (페이지 넘겨도 번호가 이어지도록)
    int no = search_count - ((now_page - 1) * Products.RECORD_PER_PAGE);
    model.addAttribute("no", no); // 시작 번호 전달 (예: 총 25건 중 1페이지 → 25부터 시작)

    // (7) 최종 view 파일 연결
    return "products/list_by_cateno_search_paging_grid"; // /templates/products/list_by_cateno_search_paging.html
  }

  /**
   * [카테고리별 상품 목록 + 검색 + 페이징 + Grid 전용] - Grid 레이아웃 전용 목록 페이지 - 페이징 및 검색 로직 동일,
   * URL과 View 용도가 다름 - 호출 URL 예시:
   * http://localhost:9093/products/list_by_cateno_grid?cateno=5
   * 
   * @param cateno   카테고리 번호
   * @param word     검색어 (없으면 전체)
   * @param now_page 현재 페이지 번호
   * @return list_by_cateno_search_paging_grid.html
   */
  @GetMapping(value = "/list_by_cateno_grid")
  public String list_by_cateno_search_paging_grid(HttpSession session, Model model,
      @RequestParam(name = "cateno", defaultValue = "0") int cateno,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {

    // System.out.println("-> cateno: " + cateno);
    // (1) 상단 카테고리 메뉴 + 현재 카테고리 정보
    ArrayList<CateVOMenu> menu = this.cateProc.menu(); // 상단 메뉴 바 구성용 카테고리 목록
    model.addAttribute("menu", menu);

    CateVO cateVO = this.cateProc.read(cateno); // 현재 선택된 카테고리 정보
    model.addAttribute("cateVO", cateVO);

    // 검색어 null 방지 및 공백 제거
    word = Tool.checkNull(word).trim();

    // (2) 검색 + 페이징 조건 Map 구성
    HashMap<String, Object> map = new HashMap<>();
    map.put("cateno", cateno); // 카테고리 조건
    map.put("word", word); // 검색어 조건
    map.put("now_page", now_page); // 현재 페이지 조건

    // (3) 상품 목록 조회 (검색 + 페이징 적용된 결과)
    ArrayList<ProductsVO> list = this.productsProc.list_by_cateno_search_paging(map);
    model.addAttribute("list", list); // 결과 리스트 전달
    // System.out.println("-> size: " + list.size());
    model.addAttribute("word", word); // 검색어 전달 (검색창 유지용)

    // (4) 전체 검색 결과 수(일련번호) 및 페이징 처리
    int search_count = this.productsProc.list_by_cateno_search_count(map); // 총 레코드 수

    String paging = this.productsProc.pagingBox(cateno, now_page, word, "/products/list_by_cateno_grid", search_count,
        Products.RECORD_PER_PAGE, Products.PAGE_PER_BLOCK);
    // 필터 조건, 페이징 링크 URL, 전체 검색 결과 수, 페이지당 출력 수, 블록당 페이지 수

    model.addAttribute("paging", paging); // 페이징 HTML 전달
    model.addAttribute("now_page", now_page); // 현재 페이지 전달
    model.addAttribute("search_count", search_count); // 총 검색 결과 수 전달

    // (5) 화면에 출력할 시작 일련 변호 생성: 레코드 갯수 - ((현재 페이지수 -1) * 페이지당 레코드 수)
    // ex: 총 25건, 페이지당 10건 → 1페이지: 25~16 / 2페이지: 15~6 ...
    int no = search_count - ((now_page - 1) * Products.RECORD_PER_PAGE);
    model.addAttribute("no", no); // 일련번호 전달

    // (6) 최종 View 반환 (Grid 형태의 템플릿)
    // /templates/products/list_by_cateno_search_paging_grid.html
    return "products/list_by_cateno_search_paging_grid";
  }

  /**
   * [상품 상세 조회] 
   * - 특정 상품 번호(productsno)에 해당하는 상세 정보를 출력 
   * - 상단 메뉴(카테고리) 정보 제공 - 상품 정보(파일 사이즈 단위 변환 포함) 
   * - 카테고리 정보 및 관련 리뷰 목록 조회 
   * - 추천 여부(좋아요, 하트 상태) 확인 
   * - 검색어 & 현재
   * 페이지 정보 유지 (목록 화면 복귀 시 편의성 제공)
   * @param session   로그인 여부 및 사용자 권한 확인용 세션
   * @param model     View에 데이터 전달용 객체
   * @param productsno 상세 조회할 상품 번호
   * @param word      검색어 (목록 복귀 시 상태 유지용)
   * @param now_page  현재 페이지 번호 (페이징 상태 유지용)
   * @return /templates/products/read.html (상품 상세 화면)
     */
  @GetMapping(value = "/read")
  public String read(HttpSession session, Model model,
      @RequestParam(name = "productsno", defaultValue = "0") int productsno, // 조회할 상품 번호
      @RequestParam(name = "word", defaultValue = "") String word, // 검색어 유지용
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) { // 현재 페이지

    // (1) 상단 메뉴 카테고리 데이터 설정
    // - 모든 페이지 공통으로 사용되는 메뉴 데이터
    ArrayList<CateVOMenu> menu = this.cateProc.menu(); // 상단 네비게이션 메뉴용 카테고리
    model.addAttribute("menu", menu);

    // (2) 상품 정보 가져오기
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

    // (3) 현재 상품이 속한 카테고리 정보 가져오기
    CateVO cateVO = this.cateProc.read(productsVO.getCateno()); // 상품의 카테고리 번호로 조회
    model.addAttribute("cateVO", cateVO); // 카테고리 정보 View에 전달

    // (3-1) 관련 상품 리스트
    List<ReviewMemberVO> reviewList = this.reviewProc.list_join_by_productsno(productsno);
    model.addAttribute("reviewList", reviewList);

    // 세션의 로그인 사용자 번호를 모델에 전달 (Thymeleaf에서 #session 접근 제거 대응)
    Integer sessionMemberno = (Integer) session.getAttribute("memberno");
    model.addAttribute("sessionMemberno", sessionMemberno);

    // (4) 검색 키워드 및 현재 페이지 값 유지 (뒤로가기 편의)
    model.addAttribute("word", word);
    model.addAttribute("now_page", now_page);

    // (5) 추천 여부 확인 (하트 아이콘 상태)
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("productsno", productsno);

    int hartCnt = 0; // 로그인하지 않음, 비회원, 추천하지 않음 (비회원 또는 추천 안 한 상태가 기본)
    if (session.getAttribute("memberno") != null) { // 회원인 경우만 카운트 처리 (로그인한 경우)
      int memberno = (int) session.getAttribute("memberno");
      map.put("memberno", memberno);
      hartCnt = this.productsgoodProc.hartCnt(map); // 추천 여부 조회 (1: 추천함, 0: 아님)
    }

    model.addAttribute("hartCnt", hartCnt); // 뷰에 전달 → 하트 UI 제어에 사용

    // (6) 최종 View 지정
    return "products/read"; // /templates/products/read.html
  }

  /*
   * [관련 상품 무한 스크롤 로드]
   * - 특정 카테고리(cateno) 내에서 현재 상품(productsno)을 제외한 상품 목록을 페이지 단위로 로드
   * - AJAX 요청으로 호출되며 JSON 데이터를 반환
   * 
   * 요청 URL: 
   *   POST /products/related_scroll
   * 
   * 요청 파라미터:
   *   - cateno (int)       : 현재 카테고리 번호
   *   - productsno (int)   : 기준 상품 번호 (이 상품은 제외)
   *   - page (int)         : 요청 페이지 번호 (1부터 시작)
   * 
   * 반환:
   *   - ArrayList<ProductsVO> (자동 JSON 변환)
   * 
   * 사용 예:
   *   - 상품 상세 화면 하단에서 "관련 상품" 섹션 무한 스크롤 로딩 시 사용
   */
  @PostMapping("/related_scroll")
  @ResponseBody
  public ArrayList<ProductsVO> related_scroll(@RequestParam("cateno") int cateno,
      @RequestParam("productsno") int productsno, @RequestParam("page") int page) {

    // (1) 페이징 범위 계산
    // - 페이지당 12개 출력
    // - start ~ end 범위 생성
    int start = (page - 1) * 12 + 1;   // 시작 인덱스
    int end = page * 12;                // 끝 인덱스

    // (2) 검색 조건 Map 구성
    // - 카테고리 번호, 기준 상품 제외, 페이징 범위
    Map<String, Object> map = new HashMap<>();
    map.put("cateno", cateno);
    map.put("productsno", productsno);
    map.put("start", start);
    map.put("end", end);

    // (3) DB 조회 → JSON 응답 자동 변환
    return productsProc.related_scroll(map);
  }

  /**
   * [지도(Map) 등록/수정 폼]
   * - 상품 상세 페이지에서 지도 등록을 위한 입력 폼 제공
   * - 기존 map 데이터가 있다면 수정 가능
   * 
   * 호출 URL:
   *   GET /products/map?productsno=1
   * 
   * 요청 파라미터:
   *   - productsno (int) : 지도 등록 대상 상품 번호
   *   - now_page (int)   : 현재 페이지 (옵션, null 시 기본값 1)
   *   - word (String)    : 검색어 (옵션, null 시 "")
   * 
   * 반환:
   *   - /templates/products/map.html (지도 등록 화면)
   */
  @GetMapping(value = "/map")
  public String map(Model model, @RequestParam(name = "productsno", defaultValue = "0") int productsno,
      @RequestParam(name = "now_page", required = false) Integer now_page,
      @RequestParam(name = "word", required = false) String word) {
    
    // 파라미터 기본값 처리
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
    model.addAttribute("cateVO", cateVO); // 카테고리 정보 View로 전달

    // 페이지 정보 및 검색어도 View에 넘김 (링크 유지용) 이것도 2줄 추가함
    model.addAttribute("now_page", now_page);
    model.addAttribute("word", word);

    // 지도 편집 화면으로 이동
    return "products/map";
  }

  /**
   * [지도(Map) 등록/수정/삭제 처리]
   * - 상품 상세 화면에서 입력한 지도 HTML 코드(DB map 컬럼)를 업데이트
   * 
   * 호출 URL:
   *   POST /products/map
   * 
   * 요청 파라미터:
   *   - productsno (int) : 지도 등록 대상 상품 번호
   *   - map (String)     : 지도 HTML 코드
   *   - now_page (int)   : 현재 페이지 (옵션)
   *   - word (String)    : 검색어 (옵션)
   * 
   * 처리:
   *   - DB 업데이트 후 상품 상세 화면으로 리디렉션
   */
  @PostMapping(value = "/map")
  public String map_update(Model model, @RequestParam(name = "productsno", defaultValue = "0") int productsno,
      @RequestParam(name = "map", defaultValue = "") String map,
      @RequestParam(name = "now_page", required = false) Integer now_page,
      @RequestParam(name = "word", required = false) String word, RedirectAttributes ra) {
    
    // 파라미터 기본값 처리
    if (now_page == null) now_page = 1;
    if (word == null) word = "";

    // 파라미터를 Map 형태로 포장
    HashMap<String, Object> hashMap = new HashMap<String, Object>();
    hashMap.put("productsno", productsno); // 어떤 상품에 대해 적용할지
    hashMap.put("map", map); // 입력된 지도 HTML 코드

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
   * [YouTube 등록/수정 폼 제공]
   * - 상품 상세 화면에서 YouTube URL 또는 임베드 코드 등록을 위한 입력 폼 제공
   * - 기존 값이 있다면 수정 가능
   * 
   * 호출 URL:
   *   GET /products/youtube?productsno=1
   * 
   * 요청 파라미터:
   *   - productsno (int) : 대상 상품 번호
   *   - word (String)    : 검색어 (상태 유지용)
   *   - now_page (int)   : 현재 페이지 번호 (상태 유지용)
   * 
   * 처리 내용:
   *   - (1) 상단 메뉴 데이터 조회
   *   - (2) 상품 상세 정보 조회 (youtube 코드 포함)
   *   - (3) 카테고리 정보 조회
   *   - (4) 검색어 및 페이지 정보 유지
   * 
   * 반환:
   *   - /templates/products/youtube.html
   */
  @GetMapping(value = "/youtube")
  public String youtube(Model model, @RequestParam(name = "productsno", defaultValue = "0") int productsno,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {

    // 상단 메뉴 구성 (네비게이션 바용 카테고리 목록)
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);

    // 상품 정보 조회 (youtube 코드 포함)
    ProductsVO productsVO = this.productsProc.read(productsno);
    model.addAttribute("productsVO", productsVO); // request.setAttribute("productsVO", productsVO);
    // 상품 정보 전달

    // 해당 상품이 속한 카테고리 정보 조회
    CateVO cateVO = this.cateProc.read(productsVO.getCateno());
    model.addAttribute("cateVO", cateVO); // 카테고리 정보 전달

    // 검색어 및 페이지 정보 유지  (뷰에서 hidden 값으로 활용)
    model.addAttribute("word", word);
    model.addAttribute("now_page", now_page);

    // 유튜브 등록/수정 폼으로 이동
    return "products/youtube"; // forward
  }

  /**
   * [YouTube 등록/수정/삭제 처리]
   * - 입력된 YouTube 코드(iframe embed 등)를 DB에 저장
   * - 삭제 요청인 경우 youtube 값은 빈 문자열
   * - 등록/수정 후 다시 상품 상세 페이지로 이동
   * 
   * 호출 URL:
   *   POST /products/youtube
   * 
   * 요청 파라미터:
   *   - productsno (int) : 대상 상품 번호
   *   - youtube (String) : YouTube embed 코드 또는 iframe
   *   - word (String)    : 검색어 (상태 유지용)
   *   - now_page (int)   : 현재 페이지 (상태 유지용)
   * 
   * 처리 내용:
   *   - (1) YouTube 코드 크기(width) 조정 (Tool.youtubeResize)
   *   - (2) DB에 youtube 컬럼 업데이트
   *   - (3) 검색어/페이지 정보 유지하며 상세 페이지로 redirect
   * 
   * 반환:
   *   - redirect:/products/read (상품 상세 화면)
   */
  @PostMapping(value = "/youtube")
  public String youtube_update(Model model, RedirectAttributes ra,
      @RequestParam(name = "productsno", defaultValue = "0") int productsno,
      @RequestParam(name = "youtube", defaultValue = "") String youtube,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {

    // (1) 입력된 YouTube 코드 크기 조정
    // - width 기준 640px로 변경 (Tool.youtubeResize 활용)
    // - youtube 값이 비어있으면(삭제 요청) 크기 조정 스킵
    if (youtube.trim().length() > 0) { // 삭제 중인지 확인, 삭제가 아니면 youtube 크기 변경
      youtube = Tool.youtubeResize(youtube, 640); // youtube 영상의 크기를 width 기준 640 px로 변경
    }

    // (2) DB 업데이트를 위한 Map 구성
    HashMap<String, Object> hashMap = new HashMap<String, Object>();
    hashMap.put("productsno", productsno); // 어떤 상품인지
    hashMap.put("youtube", youtube);  // 조정된 YouTube 코드 또는 빈 값

    // (3)DB 업데이트 실행
    this.productsProc.youtube(hashMap);

    // (4) 리디렉션 시 검색어, 페이지 상태 유지
    ra.addAttribute("productsno", productsno);
    ra.addAttribute("word", word);
    ra.addAttribute("now_page", now_page);

    //  (5) 등록 후 해당 상품 상세 페이지로 이동
    // return "redirect:/products/read?productsno=" + productsno;
    return "redirect:/products/read";
  }

  /**
   * [상품 텍스트 수정 화면 제공]
   * - 상품명, 설명, 가격 등 텍스트 기반 정보 수정 폼 제공
   * - 권한 체크: 관리자(admin, grade 1~4) 또는 작성자(supplier, grade 5~15)만 접근 가능
   *
   * 호출 URL:
   *   GET /products/update_text?productsno=123
   *
   * 요청 파라미터:
   *   - productsno (int) : 수정할 상품 번호
   *   - word (String)    : 검색어 (상태 유지)
   *   - now_page (int)   : 현재 페이지 번호 (상태 유지)
   *
   * 처리 내용:
   *   (1) 상단 메뉴 및 카테고리 정보 설정
   *   (2) 수정 대상 상품 정보 조회
   *   (3) 권한 체크 후 수정 화면 OR 로그인 유도 페이지 이동
   *
   * 반환:
   *   - /templates/products/update_text.html (forward)
   *   - 또는 redirect:/member/login_cookie_need (권한 없음)
   */
  public String update_text(HttpSession session, Model model, RedirectAttributes ra,
      @RequestParam(name = "productsno", defaultValue = "0") int productsno,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {

    // (1) 공통 데이터: 메뉴 + 검색어/페이지 상태
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);
    model.addAttribute("word", word);
    model.addAttribute("now_page", now_page);

    // (2) 세션에서 로그인 사용자 정보
    Integer gradeObj = (Integer) session.getAttribute("grade");
    int grade = (gradeObj != null) ? gradeObj : 99;
    Integer memberno = (Integer) session.getAttribute("memberno");

    // (3) 수정 대상 상품 + 카테고리 정보 조회
    ProductsVO productsVO = this.productsProc.read(productsno);
    model.addAttribute("productsVO", productsVO);

    CateVO cateVO = this.cateProc.read(productsVO.getCateno());
    model.addAttribute("cateVO", cateVO);

    // (4) 권한 체크
    // - 관리자(1~4) → 허용
    // - 공급자(5~15) + 본인 작성글 → 허용
    // - 그 외 → 로그인 안내 페이지
    if (grade >= 1 && grade <= 4) {
      return "products/update_text";
    } else if (grade >= 5 && grade <= 15 && productsVO.getMemberno() == memberno) {
      return "products/update_text";
    } else {
      return "redirect:/member/login_cookie_need?url=/products/update_text?productsno=" + productsno;
    }
  }

  /**
   * [상품 텍스트 수정 처리]
   * - 상품명, 가격, 설명, 태그 등 텍스트 기반 정보 DB 반영
   * - 비밀번호 확인 후 수정 가능 (본인 인증)
   * - 관리자 또는 작성자만 허용
   *
   * 호출 URL:
   *   POST /products/update_text
   *
   * 요청 파라미터:
   *   - ProductsVO (productsno, passwd, name, price 등)
   *   - search_word (String): 검색어 상태 유지용
   *   - now_page (int): 현재 페이지 유지용
   *
   * 처리 내용:
   *   (1) 비밀번호 확인
   *   (2) 권한 체크
   *   (3) 수정 처리 및 redirect
   *
   * 반환:
   *   - 성공 → redirect:/products/read (상세 보기)
   *   - 실패 → redirect:/products/post2get (비번 불일치)
   */
  @PostMapping(value = "/update_text")
  public String update_text_proc(HttpSession session, Model model, ProductsVO productsVO, RedirectAttributes ra,
      @RequestParam(name = "search_word", defaultValue = "") String search_word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {

    // (1) redirect 시 검색어 + 페이지 유지
    ra.addAttribute("word", search_word);
    ra.addAttribute("now_page", now_page);

    // (2) 세션에서 로그인 사용자 정보
    Integer gradeObj = (Integer) session.getAttribute("grade");
    int grade = (gradeObj != null) ? gradeObj : 99;  // 기본값: 비회원
    Integer sessionMemberno = (Integer) session.getAttribute("memberno");

    // (3) 비밀번호 검증 (productsno + passwd)
    HashMap<String, Object> map = new HashMap<>();
    map.put("productsno", productsVO.getProductsno());
    map.put("passwd", productsVO.getPasswd());

    // 비밀번호 불일치 시
    if (this.productsProc.password_check(map) != 1) {
      ra.addFlashAttribute("code", Tool.PASSWORD_FAIL);
      ra.addFlashAttribute("cnt", 0);
      ra.addAttribute("productsno", productsVO.getProductsno());
      ra.addAttribute("cateno", productsVO.getCateno());
      ra.addAttribute("url", "/products/msg");
      return "redirect:/products/post2get"; // 반드시 return 필요
    }

    // (4) 체크 안 된 경우 기본값 설정
    if (productsVO.getIs_best() == null)
      productsVO.setIs_best("N");
    if (productsVO.getIs_new() == null)
      productsVO.setIs_new("N");
    if (productsVO.getIs_event() == null)
      productsVO.setIs_event("N");

    // (5) DB에 저장된 원본 데이터 조회 → 작성자 확인
    ProductsVO dbVO = this.productsProc.read(productsVO.getProductsno());

    // (6) 권한 체크
    // - 관리자(1~4) OK / 공급자(5~15) + 본인 글 OK
    if ((grade >= 1 && grade <= 4) || (grade >= 5 && grade <= 15 && dbVO.getMemberno() == sessionMemberno)) {
      int cnt = this.productsProc.update_text(productsVO);
      ra.addAttribute("productsno", productsVO.getProductsno());
      ra.addAttribute("cateno", productsVO.getCateno());
      return "redirect:/products/read";
    }

    // (7) 권한 없음 → 목록으로 리다이렉트
    return "redirect:/products/list_by_cateno_search_paging?cateno=" + productsVO.getCateno() + "&now_page=" + now_page
        + "&word=" + search_word;
  }

  /**
   * [상품 이미지 수정 화면 제공]
   * - 기존에 등록된 이미지(대표, 추가, 광고 이미지)를 교체할 수 있는 폼 제공
   * - 권한 체크: 관리자(grade 1~4) 또는 작성자(grade 5~15)만 접근 가능
   *
   * 호출 URL:
   *   GET /products/update_file?productsno={상품번호}
   *
   * 요청 파라미터:
   *   - productsno (int): 수정할 상품 번호
   *   - word (String): 검색어 (목록 복귀 시 유지)
   *   - now_page (int): 현재 페이지 번호
   *
   * 처리 내용:
   *   (1) 상단 메뉴 및 상태 값(검색어, 페이지) View에 전달
   *   (2) 수정 대상 상품 + 카테고리 정보 조회
   *   (3) 권한 체크 후 View 이동 (또는 로그인 안내 페이지)
   *
   * 반환:
   *   - /templates/products/update_file.html (forward)
   *   - 또는 redirect:/member/login_cookie_need (권한 없음)
   */
  @GetMapping(value = "/update_file")
  public String update_file(HttpSession session, Model model,
      @RequestParam(name = "productsno", defaultValue = "0") int productsno,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {

    // (1) 세션에서 사용자 등급과 회원번호 확인
    Integer gradeObj = (Integer) session.getAttribute("grade");
    int grade = (gradeObj != null) ? gradeObj : 99;
    Integer memberno = (Integer) session.getAttribute("memberno");

    // (2) View 렌더링에 필요한 데이터 준비
    model.addAttribute("menu", this.cateProc.menu()); // 상단 메뉴
    model.addAttribute("word", word);                     // 검색어 유지
    model.addAttribute("now_page", now_page);        // 페이지 유지

    ProductsVO productsVO = this.productsProc.read(productsno); // 수정 대상 상품 정보
    model.addAttribute("productsVO", productsVO);

    CateVO cateVO = this.cateProc.read(productsVO.getCateno()); // 해당 상품의 카테고리 정보
    model.addAttribute("cateVO", cateVO);

    // (3) 권한 체크
    // - 관리자(1~4) → OK / 공급자(5~15) + 본인 글 → OK / 그 외 → 로그인 안내 페이지로 redirect
    if (grade >= 1 && grade <= 4) {
      return "products/update_file";
    } else if (grade >= 5 && grade <= 15 && memberno != null && memberno.equals(productsVO.getMemberno())) {
      return "products/update_file";
    } else {
      return "redirect:/member/login_cookie_need?url=/products/update_file?productsno=" + productsno;
    }
  }

  /**
   * [상품 이미지 수정 처리]
   * - 기존 이미지(대표, 추가 이미지, 광고 이미지)를 삭제 후 새로 업로드
   * - 권한 체크 후 DB 업데이트
   *
   * 호출 URL:
   *   POST /products/update_file
   *
   * 요청 파라미터:
   *   - ProductsVO: 수정 대상 상품 정보 + 업로드 파일
   *   - word (String): 검색어 유지
   *   - now_page (int): 페이지 유지
   *
   * 처리 절차:
   *   (1) 권한 체크
   *   (2) 기존 파일 삭제
   *   (3) 새 파일 업로드 (대표 이미지 + 추가 이미지 + 광고 이미지)
   *   (4) DB 업데이트
   *   (5) redirect → 상세 페이지
   *
   * 반환:
   *   - redirect:/products/read (상세 보기)
   */
  @PostMapping(value = "/update_file")
  public String update_file_proc(HttpSession session, Model model, RedirectAttributes ra, ProductsVO productsVO,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {
    
    // (1) 세션에서 권한 정보 확인
    Integer gradeObj = (Integer) session.getAttribute("grade");
    int grade = (gradeObj != null) ? gradeObj : 99;
    Integer sessionMemberno = (Integer) session.getAttribute("memberno");

    // 기존 DB 데이터 (파일 삭제용)
    ProductsVO productsVO_old = this.productsProc.read(productsVO.getProductsno());

    // (2) 권한 체크
    // - 관리자 OR 본인 글 작성자만 허용
    boolean authorized = false;
    if (grade >= 1 && grade <= 4) {
      authorized = true;
    } else if (grade >= 5 && grade <= 15 && sessionMemberno != null
        && sessionMemberno.equals(productsVO_old.getMemberno())) {
      authorized = true;
    }

    if (!authorized) { // 권한 없음 → 로그인 안내 페이지
      ra.addAttribute("url", "/member/login_cookie_need");
      return "redirect:/products/post2get";
    }

    // (3) 기존 파일 삭제 (대표 + 썸네일 + 추가 이미지들 + 광고 이미지)
    String upDir = Products.getUploadDir();
    Tool.deleteFile(upDir, productsVO_old.getFile1saved());
    Tool.deleteFile(upDir, productsVO_old.getThumb1());
    Tool.deleteFile(upDir, productsVO_old.getFile2saved());
    Tool.deleteFile(upDir, productsVO_old.getFile3saved());
    
    // (4) 새 파일 업로드
    // - file1 (대표 이미지) + 썸네일 생성
    MultipartFile mf = productsVO.getFile1MF();
    String file1 = mf.getOriginalFilename();
    long size1 = mf.getSize();
    String file1saved = "";
    String thumb1 = "";

    
    if (size1 > 0) {
      file1saved = Upload.saveFileSpring(mf, upDir);  // 업로드
      if (Tool.isImage(file1saved)) {
        thumb1 = Tool.preview(upDir, file1saved, 250, 200);  // 썸네일 생성
      }
    } else {
      file1 = "";
      size1 = 0;
    }
    productsVO.setFile1(file1);
    productsVO.setFile1saved(file1saved);
    productsVO.setThumb1(thumb1);
    productsVO.setSize1(size1);

    // (5) file2 (추가 이미지)
    MultipartFile mf2 = productsVO.getFile2MF();
    String file2 = mf2.getOriginalFilename();
    long size2 = mf2.getSize();
    String file2saved = "";

    if (size2 > 0) {
      file2saved = Upload.saveFileSpring(mf2, upDir);
    } else {
      file2 = "";
      size2 = 0;
    }
    productsVO.setFile2(file2);
    productsVO.setFile2saved(file2saved);
    productsVO.setSize2(size2);

    // (6) file3 (추가 이미지)
    MultipartFile mf3 = productsVO.getFile3MF();
    String file3 = mf3.getOriginalFilename();
    long size3 = mf3.getSize();
    String file3saved = "";

    if (size3 > 0) {
      file3saved = Upload.saveFileSpring(mf3, upDir);
    } else {
      file3 = "";
      size3 = 0;
    }
    productsVO.setFile3(file3);
    productsVO.setFile3saved(file3saved);
    productsVO.setSize3(size3);

    Tool.deleteFile(upDir, productsVO_old.getFileAdsaved());

    // (7) 광고 이미지(fileAd)
    MultipartFile mfAd = productsVO.getFileAdMF();
    String fileAd = mfAd.getOriginalFilename();
    long sizeAd = mfAd.getSize();
    String fileAdsaved = "";

    if (sizeAd > 0 && Tool.checkUploadFile(fileAd)) {
      fileAdsaved = Upload.saveFileSpring(mfAd, upDir);
    } else {
      fileAd = "";
      sizeAd = 0;
    }
    productsVO.setFileAd(fileAd);
    productsVO.setFileAdsaved(fileAdsaved);
    productsVO.setSizeAd(sizeAd);

    // (8) DB 업데이트
    this.productsProc.update_file(productsVO);

    // (9) redirect 시 검색어/페이지 유지 + 상세 페이지로 이동
    ra.addAttribute("productsno", productsVO.getProductsno());
    ra.addAttribute("cateno", productsVO.getCateno());
    ra.addAttribute("word", word);
    ra.addAttribute("now_page", now_page);

    return "redirect:/products/read";
  }

  /**
   * [상품 삭제 폼 요청]
   * - 삭제 전에 상품 정보 확인용 화면 제공
   * - 관리자 또는 본인 글 작성자만 접근 가능
   *
   * 호출 URL:
   *   GET /products/delete?productsno={상품번호}&cateno={카테고리번호}
   *
   * 요청 파라미터:
   *   - cateno (int): 카테고리 번호
   *   - productsno (int): 삭제할 상품 번호
   *   - word (String): 검색어 (리스트 페이지 복귀 시 유지)
   *   - now_page (int): 현재 페이지 (리스트 복귀 시 유지)
   *
   * 처리 절차:
   *   (1) 세션에서 등급(grade), 회원번호(memberno) 확인
   *   (2) 삭제 대상 상품 정보 조회
   *   (3) 권한 체크:
   *       - 관리자(1~4) → OK
   *       - 공급자(5~15) & 본인 글 작성자 → OK
   *       - 그 외 → 로그인 안내 페이지 redirect
   *   (4) 삭제 확인 페이지로 이동
   *
   * 반환:
   *   - /templates/products/delete.html (forward)
   *   - 또는 redirect:/member/login_cookie_need (권한 없음)
   */
  @GetMapping(value = "/delete")
  public String delete(HttpSession session, Model model, RedirectAttributes ra,
      @RequestParam(name = "cateno", defaultValue = "0") int cateno,
      @RequestParam(name = "productsno", defaultValue = "0") int productsno,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {

    // (1) 세션에서 권한 정보 확인
    Integer gradeObj = (Integer) session.getAttribute("grade");
    int grade = (gradeObj != null) ? gradeObj : 99; // 기본값: 비회원
    Integer sessionMemberno = (Integer) session.getAttribute("memberno");

    // (2) 삭제 대상 상품 정보 조회
    ProductsVO productsVO = this.productsProc.read(productsno);
    int ownerMemberno = productsVO.getMemberno();

    // (3) 권한 체크 / 관리자(1~4) → OK / 공급자(5~15) & 본인 글 → OK
    boolean authorized = (grade >= 1 && grade <= 4)
        || ((grade >= 5 && grade <= 15) && sessionMemberno != null && sessionMemberno == ownerMemberno);

    if (authorized) {
      // 삭제 확인 화면에 필요한 데이터 전달
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

  /**
   * [상품 삭제 처리]
   * - 관리자 또는 본인 글 작성자만 삭제 가능
   * - DB 삭제 전, 서버에 저장된 이미지 파일 제거
   * - 카테고리 cnt 갱신
   * - 페이지 보정 (마지막 페이지 비워질 경우 이전 페이지로 이동)
   *
   * 호출 URL:
   *   POST /products/delete
   *
   * 요청 파라미터:
   *   - cateno (int): 카테고리 번호
   *   - productsno (int): 삭제할 상품 번호
   *   - word (String): 검색어
   *   - now_page (int): 현재 페이지
   *
   * 처리 절차:
   *   (1) 세션에서 권한 확인 (admin / supplier + 본인 글)
   *   (2) 파일 삭제 (대표 이미지, 썸네일, 추가 이미지, 광고 이미지)
   *   (3) 상품 DB 삭제
   *   (4) 카테고리 cnt 자동 갱신
   *   (5) 페이지 보정 (마지막 페이지 비었으면 이전 페이지로 이동)
   *   (6) redirect → 카테고리별 상품 목록
   *
   * 반환:
   *   - redirect:/products/list_by_cateno
   */
  @PostMapping(value = "/delete")
  public String delete_proc(HttpSession session, RedirectAttributes ra,
      @RequestParam(name = "cateno", defaultValue = "0") int cateno,
      @RequestParam(name = "productsno", defaultValue = "0") int productsno,
      @RequestParam(name = "word", defaultValue = "") String word,
      @RequestParam(name = "now_page", defaultValue = "1") int now_page) {

    // (1) 권한 체크
    String grade = (String) session.getAttribute("grade");
    Integer sessionMemberno = (Integer) session.getAttribute("memberno");

    ProductsVO productsVO_read = this.productsProc.read(productsno); // 삭제 대상 조회
    int ownerMemberno = productsVO_read.getMemberno();

    boolean authorized = "admin".equals(grade)
        || ("supplier".equals(grade) && sessionMemberno != null && sessionMemberno == ownerMemberno);

    if (!authorized) { // 권한 없음 → 경유 페이지로 이동
      ra.addAttribute("url", "/member/login_cookie_need");
      return "redirect:/products/post2get";
    }
    
    // (2) 서버에 저장된 이미지 파일 삭제
    String upDir = Products.getUploadDir();
    Tool.deleteFile(Products.getUploadDir(), productsVO_read.getFile1saved()); // 대표 이미지
    Tool.deleteFile(Products.getUploadDir(), productsVO_read.getThumb1());  // 썸네일
    Tool.deleteFile(upDir, productsVO_read.getFile2saved()); // 추가 이미지 2
    Tool.deleteFile(upDir, productsVO_read.getFile3saved()); // 추가 이미지 3
    Tool.deleteFile(upDir, productsVO_read.getFileAdsaved());  // 광고 이미지

    // (3) 상품 DB 삭제
    this.productsProc.delete(productsno);
    // (4) 카테고리 cnt 갱신 (중분류, 대분류)
    this.cateProc.updateMidCnt();
    this.cateProc.updateMainCnt();

    // (5) 페이지 보정 (마지막 페이지에서 데이터 모두 삭제 시)
    HashMap<String, Object> map = new HashMap<>();
    map.put("cateno", cateno);
    map.put("word", word);

    int count = this.productsProc.list_by_cateno_search_count(map);  // 남은 데이터 개수
    if (count % Products.RECORD_PER_PAGE == 0) {
      now_page = Math.max(1, now_page - 1); // 페이지 최소 1 보정
    }

    // (6) redirect → 카테고리별 목록 페이지
    ra.addAttribute("cateno", cateno);
    ra.addAttribute("word", word);
    ra.addAttribute("now_page", now_page);

    return "redirect:/products/list_by_cateno";
  }

  /**
   * ============================================================
   * [POST] 상품 추천 / 추천 해제 처리 (AJAX)
   * ============================================================
   * 요청 예시 (JSON):
   *   {"productsno": 64}
   *
   * 응답 예시 (JSON):
   *   {
   *     "isMember": 1,        // 로그인 여부 (1: 로그인, 0: 비로그인)
   *     "hartCnt": 1,         // 현재 사용자의 추천 상태 (1: 추천 ON, 0: 추천 해제)
   *     "recom": 10           // 상품의 최신 추천수
   *   }
   *
   * 처리 로직:
   *   1. 요청 본문(JSON)에서 productsno 파싱
   *   2. 세션에서 로그인 여부(memberno) 확인
   *      → 비로그인 시 {"isMember":0, "message":"로그인 후 이용 가능합니다."} 반환
   *   3. 추천 여부 체크:
   *      - 기존 추천 기록 없음 → 추천 등록 + 상품 추천수 증가
   *      - 기존 추천 기록 있음 → 추천 해제 + 상품 추천수 감소
   *   4. DB에서 최신 추천수 조회 후 JSON 응답
   *
   * AJAX 호출 시 주의사항:
   *   - 요청 헤더: Content-Type: application/json
   *   - POST 본문: {"productsno":값}
   *
   * URL:
   *   http://localhost:9093/products/good
   */
  @PostMapping(value = "/good")
  @ResponseBody
  public String good(HttpSession session, @RequestBody String json_src) {
    // 응답 JSON 객체 초기화
    JSONObject responseJson = new JSONObject();

    try {
      // (1) 요청 JSON 파싱
      JSONObject src = new JSONObject(json_src); // 요청 본문(JSON) → JSONObject 변환
      int productsno = src.optInt("productsno", 0); // 상품 번호 추출
      System.out.println("-> [GOOD] 요청 productsno: " + productsno);

      // (2) 세션에서 로그인 여부 확인
      Integer memberno = (Integer) session.getAttribute("memberno"); // 로그인 사용자 번호
      if (memberno == null) {
        responseJson.put("isMember", 0); // 로그인 안 함
        responseJson.put("message", "로그인 후 이용 가능합니다.");
        return responseJson.toString();
      }

      // (3) 추천 여부 확인 (productsgood 테이블)
      ProductsgoodVO existingVO = this.productsgoodProc.readByProductsnoMemberno(productsno, memberno);
      int hartCnt = 0; // 추천 상태 (0: 추천 안함, 1: 추천 함)

      if (existingVO == null) {
        // (3-1) 추천 기록 없음 → 새 추천 등록
        ProductsgoodVO newVO = new ProductsgoodVO();
        newVO.setProductsno(productsno);
        newVO.setMemberno(memberno);
        this.productsgoodProc.create(newVO);   // 추천 기록 추가
        this.productsProc.increaseRecom(productsno); // 상품 추천수 +1
        hartCnt = 1; // 추천 ON
      } else {
        // (3-2) 추천 기록 있음 → 추천 해제
        this.productsgoodProc.deleteByProductsnoMemberno(productsno, memberno);
        this.productsProc.decreaseRecom(productsno);
      }

      // (4) 최신 추천 수 조회
      int recom = this.productsProc.read(productsno).getRecom();

      // (5) 응답 JSON 생성
      responseJson.put("isMember", 1); // 로그인 상태
      responseJson.put("hartCnt", hartCnt); // 현재 사용자의 추천 상태
      responseJson.put("recom", recom);  // 최신 추천수

    } catch (Exception e) {
      // (예외 처리) 서버 내부 오류
      e.printStackTrace();
      responseJson.put("isMember", 0);
      responseJson.put("error", "서버 처리 중 오류 발생");
    }
    
    // 최종 응답 JSON 반환
    return responseJson.toString();
  }

  /**
   * ==============================================================
   * [GET] 카테고리 상품 수(cnt) 동기화
   * ==============================================================
   * - DB 내 cate 테이블의 중분류/대분류 상품 개수(cnt)를 동기화
   * - products 테이블의 데이터 기준으로 집계 후 cate 테이블 갱신
   * - 관리자 설정 메뉴에서 동기화 요청 시 사용
   * 
   * URL:
   *   http://localhost:9093/products/update_cnt
   *
   * 응답:
   *   "카테고리 cnt 동기화 완료"
   */
  @GetMapping("/update_cnt")
  @ResponseBody
  public String updateCnt() {
    cateProc.updateMidCnt();     // 1) 중분류 카운트 갱신
    cateProc.updateMainCnt();    // 2) 대분류 카운트 갱신
    return "카테고리 cnt 동기화 완료";     // 3) 처리 완료 메시지 반환 (AJAX 호출 시 문자열 응답)
  }

  
  /**
   * ==============================================================
   * [GET] 로그인 사용자의 "찜한 상품(추천)" 목록 반환 (AJAX)
   * ==============================================================
   * - 찜하기(추천) 기능으로 등록한 상품 목록을 가져옴
   * - 로그인 여부 확인 후, 미로그인 상태면 빈 배열 반환
   * - 주로 마이페이지나 찜목록 화면에서 호출
   * 
   * URL:
   *   http://localhost:9093/products/good_ajax
   *
   * 응답:
   *   JSON 배열 (ProductsVO 리스트)
   */
  @ResponseBody
  @GetMapping("/good_ajax")
  public List<ProductsVO> goodAjax(HttpSession session) {
    
    // (1) 세션에서 로그인 사용자 번호 확인
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return Collections.emptyList();
    }
    // (2) 로그인하지 않은 경우 → 빈 리스트 반환
    return productsgoodProc.getProductsgoodByMember(memberno); // 찜한 상품 목록
  }

  /**
   * ==============================================================
   * [GET] 상품 상세 정보 화면
   * ==============================================================
   * - 특정 상품(productsno)의 상세 정보를 조회 후 화면에 출력
   * - 기본 상품 정보 외 추가 이미지, 옵션은 JSP/Thymeleaf에서 처리
   *
   * URL:
   *   http://localhost:9093/products/detail?productsno=10
   *
   * View:
   *   /templates/products/detail.html
   */
  @GetMapping("/detail")
  public String detail(@RequestParam("productsno") int productsno, Model model) {
    // (1) productsno에 해당하는 상품 정보 조회
    ProductsVO productsVO = productsProc.read(productsno); // 상품 정보 조회
    
    // (2) 조회된 상품 정보를 View에 전달
    model.addAttribute("productsVO", productsVO);
    
    // (3) 상세 화면으로 이동
    return "/products/detail"; // templates/products/detail.html
  }

  /**
   * ==============================================================
   * [GET] 상품 검색
   * ==============================================================
   * - 검색어(keyword)를 이용해 상품명 또는 설명에서 일치하는 상품 조회
   * - 검색 결과 리스트 + 검색어를 모델에 담아 뷰 렌더링
   * - 검색 결과 페이지에서 카테고리 메뉴도 함께 출력
   *
   * URL:
   *   http://localhost:9093/products/search?keyword=딸기
   *
   * View:
   *   /templates/products/list_by_keyword.html
   */
  @GetMapping("/search")
  public String search(@RequestParam("keyword") String keyword, Model model) {
    
    // (1) 검색어로 상품 리스트 조회
    List<ProductsVO> list = productsProc.search(keyword);
    
    // (2) 검색 결과와 검색어를 View로 전달
    model.addAttribute("list", list);
    model.addAttribute("keyword", keyword);

    // (3) 상단 메뉴 출력용 카테고리 목록 추가
    List<CateVOMenu> menu = cateProc.menu();
    model.addAttribute("menu", menu);
    
    // (4) 검색 결과 화면으로 이동
    return "/products/list_by_keyword"; // 검색 결과 페이지
  }

  /**
   * ==============================================================
   * [GET] 베스트 상품 목록
   * ==============================================================
   * - 추천수, 판매량, 평점 등 기준으로 "인기 상품"을 정렬하여 출력
   * - productsProc.listBest() 메서드를 통해 상위 상품 목록을 가져옴
   * - 뷰에서 title을 "베스트 상품"으로 지정하여 표시
   *
   * URL:
   *   http://localhost:9093/products/best
   *
   * View:
   *   /templates/products/list_best.html
   */
  @GetMapping("/best")
  public String best(Model model) {
    // (1) DB에서 베스트 상품 목록 조회
    List<ProductsVO> list = productsProc.listBest();
    
    // (2) 조회 결과 + 화면 타이틀 View로 전달
    model.addAttribute("list", list);
    model.addAttribute("title", "베스트 상품");

    // (3) 상단 메뉴 카테고리 추가 (네비게이션)
    List<CateVOMenu> menu = cateProc.menu();
    model.addAttribute("menu", menu);
    
    // (4) 베스트 상품 화면으로 이동
    return "/products/list_best";
  }

  /**
   * ==============================================================
   * [GET] 신규 등록 상품 목록
   * ==============================================================
   * - 최근 등록된 상품 순으로 출력
   * - 최신 상품 홍보용 섹션에서 주로 사용
   *
   * URL:
   *   http://localhost:9093/products/new
   *
   * View:
   *   /templates/products/list_new.html
   */
  @GetMapping("/new")
  public String newest(Model model) {
    // (1) DB에서 신규 등록 상품 목록 조회
    List<ProductsVO> list = productsProc.listNew();
    
    // (2) 모델에 검색 결과 + 타이틀 전달
    model.addAttribute("list", list);
    model.addAttribute("title", "NEW 상품");

    // (3) 카테고리 메뉴 추가 (전체 네비게이션 메뉴)
    List<CateVOMenu> menu = cateProc.menu();
    model.addAttribute("menu", menu);

    // (4) 신규 상품 목록 화면으로 이동
    return "/products/list_new";
  }

  /**
   * ==============================================================
   * [GET] 임박 특가 상품 목록
   * ==============================================================
   * - 유통기한 임박 또는 이벤트 종료 임박 상품을 출력
   * - 재고 처리 및 할인 프로모션 상품 노출에 사용
   *
   * URL:
   *   http://localhost:9093/products/soon_expire
   *
   * View:
   *   /templates/products/list_soon_expire.html
   */
  @GetMapping("/soon_expire")
  public String soonExpire(Model model) {
    // (1) DB에서 임박 특가 상품 목록 조회
    List<ProductsVO> list = productsProc.listSoonExpire();
    
    // (2) 모델에 리스트 + 타이틀 전달
    model.addAttribute("list", list);
    model.addAttribute("title", "임박 특가");

    // (3) 전체 카테고리 메뉴 추가
    List<CateVOMenu> menu = cateProc.menu();
    model.addAttribute("menu", menu);
    
    // (4) 임박 특가 화면으로 이동
    return "/products/list_soon_expire";
  }

  /**
   * ==============================================================
   * [GET] 기획전 상품 목록
   * ==============================================================
   * - 특정 기획전(프로모션) 상품을 리스트로 출력
   * - 특별 할인, 시즌 이벤트 상품 노출 용도
   *
   * URL:
   *   http://localhost:9093/products/event
   *
   * View:
   *   /templates/products/list_event.html
   */
  @GetMapping("/event")
  public String event(Model model) {
    // (1) DB에서 기획전(이벤트) 상품 목록 조회
    List<ProductsVO> list = productsProc.listEvent();
    
    // (2) 모델에 결과 리스트 및 화면 타이틀 전달
    model.addAttribute("list", list);
    model.addAttribute("title", "기획전");

    // (3) 상단 네비게이션 메뉴용 카테고리 추가
    List<CateVOMenu> menu = cateProc.menu();
    model.addAttribute("menu", menu);

    // (4) 기획전 상품 목록 화면으로 이동
    return "/products/list_event";
  }

  /**
   * ==============================================================
   * [GET] 메인 페이지 (추천 상품 목록)
   * ==============================================================
   * - 메인 화면에서 사용자에게 노출할 추천 상품 리스트 출력
   * - 추천 알고리즘 기반 상품 또는 임의 선정된 상품 노출
   *
   * URL:
   *   http://localhost:9093/products/
   *
   * View:
   *   /templates/index.html
   */
  @GetMapping("/")
  public String main(Model model) {
    // (1) DB에서 추천 상품 목록 가져오기
    List<ProductsVO> productList = productsProc.listRecommend();
    // (2) View에 추천 상품 리스트 전달
    model.addAttribute("productList", productList);
    // (3) 메인 페이지(index.html)로 이동
    return "/index";
  }

  /**
   * ==============================================================
   * [GET] React 전용 추천 상품 API (JSON 응답)
   * ==============================================================
   * - React 프론트엔드에서 비동기로 호출하는 API
   * - 추천 상품 목록을 JSON 형식으로 반환
   *
   * CORS 설정:
   *   @CrossOrigin(origins = "http://localhost:3000")
   *   → React 개발 서버(3000번 포트) 요청 허용
   *
   * URL:
   *   http://localhost:9093/products/json
   *
   * 응답 데이터:
   *   [
   *     { "productsno": 1, "name": "상품명", "price": 10000, ... },
   *     ...
   *   ]
   */  
  @CrossOrigin(origins = "http://localhost:3000") // React용 CORS 허용
  @GetMapping("/json")
  @ResponseBody
  public List<ProductsVO> productsJson() {
    // (1) 추천 상품 목록을 JSON 형태로 반환
    return productsProc.listRecommend(); // 또는 원하는 메서드
  }

}
