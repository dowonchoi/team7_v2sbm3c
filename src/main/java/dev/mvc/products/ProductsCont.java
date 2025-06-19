package dev.mvc.products;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

// =========================================================
// member 수정 전
//  // 등록 폼, products 테이블은 FK로 cateno를 사용함.
//  // http://localhost:9093/products/create X
//  // http://localhost:9093/products/create?cateno=1 // cateno 변수값을 보내는 목적
//  // http://localhost:9093/products/create?cateno=2
//  // http://localhost:9093/products/create?cateno=5
//  @GetMapping(value = "/create")
//  public String create(HttpSession session,
//                             Model model, 
//                             @ModelAttribute("productsVO") ProductsVO productsVO, 
//                             @RequestParam(name="cateno", defaultValue="0") int cateno,
//                             RedirectAttributes ra) {
//    // 로그인 및 등급 체크
//    Integer memberno = (Integer) session.getAttribute("memberno");
//    String grade = (String) session.getAttribute("grade");
//    
//    if (memberno == null || grade == null || 
//        (!grade.equals("admin") && !grade.equals("member"))) {
//      ra.addFlashAttribute("code", "no_permission");
//      ra.addFlashAttribute("url", "/products/msg");
//      return "redirect:/products/post2get";
//    }
//    
//    ArrayList<CateVOMenu> menu = this.cateProc.menu();
//    model.addAttribute("menu", menu);
//
//    CateVO cateVO = this.cateProc.read(cateno); // 카테고리 정보를 출력하기위한 목적
//    model.addAttribute("cateVO", cateVO);
//
//    return "products/create"; // /templates/products/create.html
//    //return "products/create_ai"; // /templates/products/create_ai.html
//  }
//member 수정 전
//=========================================================
  
  // 등록 폼, products 테이블은 FK로 cateno를 사용함.
  // http://localhost:9093/products/create X
  // http://localhost:9093/products/create?cateno=1 // cateno 변수값을 보내는 목적
  // http://localhost:9093/products/create?cateno=2
  // http://localhost:9093/products/create?cateno=5
  @GetMapping(value = "/create")
  public String create(Model model, 
      @ModelAttribute("productsVO") ProductsVO productsVO, 
      @RequestParam(name="cateno", defaultValue="0") int cateno) {
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

    // 현재: 관리자 권한이 있는 경우에만 상품 등록 가능
    // member(공급자)일 경우에도 (not guest(일반회원=소비자)) 있는 경우에만 상품 등록 가능
    if (memberProc.isAdmin(session)) { // 관리자로 로그인한경우
      // ------------------------------------------------------------------------------
      // 파일 전송 코드 시작
      // =============== (1) 파일 업로드 처리 시작 ===============
      // ------------------------------------------------------------------------------
      String file1 = ""; // 원본 파일명 image
      String file1saved = ""; // 저장된 파일명, image
      String thumb1 = ""; // preview image(썸네일 파일명)

      // 파일이 저장될 서버 경로
      String upDir = Products.getUploadDir(); // 파일을 업로드할 폴더 준비
      // upDir = upDir + "/" + 한글을 제외한 카테고리 이름
      System.out.println("-> upDir: " + upDir);

      // 전송 파일이 없어도 file1MF 객체가 생성됨.
      // <input type='file' class="form-control" name='file1MF' id='file1MF'
      // value='' placeholder="파일 선택">
      MultipartFile mf = productsVO.getFile1MF();// 업로드된 파일 받아오기
      file1 = mf.getOriginalFilename(); //  원본 파일명 확인, 01.jpg
//      if (file1.toLowerCase().endsWith("jpeg")) {
//        file1 = file1.substring(0, file1.indexOf(".")) + ".jpg";
//      }
      System.out.println("-> 원본 파일명 산출 file1: " + file1);

      long size1 = mf.getSize(); // 파일 크기
      
      if (size1 > 0) { //  *파일이 존재할 경우*, 파일 크기 체크, 파일을 올리는 경우
        if (Tool.checkUploadFile(file1) == true) { // 업로드 가능한 파일인지 검사
          // 파일 저장 후 업로드된 파일명이 리턴됨, spring.jsp, spring_1.jpg, spring_2.jpg...
          file1saved = Upload.saveFileSpring(mf, upDir);// 서버에 파일 저장

          // 이미지일 경우 썸네일 이미지 생성
          if (Tool.isImage(file1saved)) { // 이미지인지 검사
            // thumb 이미지 생성후 파일명 리턴됨, width: 200, height: 150
            thumb1 = Tool.preview(upDir, file1saved, 200, 150); // 썸네일 크기
          }

          // VO에 파일 관련 정보 저장
          productsVO.setFile1(file1); // 순수 원본 파일명
          productsVO.setFile1saved(file1saved); // 저장된 파일명(파일명 중복 처리)
          productsVO.setThumb1(thumb1); // 원본이미지 축소판
          productsVO.setSize1(size1); // 파일 크기

        } else { // 전송 못하는 파일 형식
          // 업로드 허용되지 않는 파일일 경우 메시지 전달 후 중단
          ra.addFlashAttribute("code", Tool.UPLOAD_FILE_CHECK_FAIL); // 업로드 할 수 없는 파일
          ra.addFlashAttribute("cnt", 0); // 업로드 실패
          ra.addFlashAttribute("url", "/products/msg"); // msg.html, redirect parameter 적용
          return "redirect:/products/post2get"; // Post -> Get -> /products/msg.html
        }
      } else { // 글만 등록하는 경우
        System.out.println("-> 글만 등록");// 파일 없이 텍스트만 등록하는 경우
      }
      // ------------------------------------------------------------------------------
      // 파일 전송 코드 종료
      // ------------------------------------------------------------------------------
      
      // =============== (2) 상품 DB 등록 처리 ===============
      // 로그인한 사용자의 memberno를 설정 (작성자 정보)
      // Call By Reference: 메모리 공유, Hashcode 전달
      int memberno = (int) session.getAttribute("memberno"); // memberno FK
      productsVO.setMemberno(memberno);
      // DB에 상품 등록
      int cnt = this.productsProc.create(productsVO);
      // ------------------------------------------------------------------------------
      // PK의 return
      // ------------------------------------------------------------------------------
      // System.out.println("--> productsno: " + productsVO.getProductsno());
      // mav.addObject("productsno", productsVO.getProductsno()); // redirect
      // parameter 적용
      // ------------------------------------------------------------------------------
      // =============== (2) DB 등록 처리 종료 ===============
      
      // =============== (3) 결과 처리 ===============
      if (cnt == 1) { // 등록 성공
        // type 1, 재업로드 발생
        // return "<h1>파일 업로드 성공</h1>"; // 연속 파일 업로드 발생

        // type 2, 재업로드 발생
        // model.addAttribute("cnt", cnt);
        // model.addAttribute("code", "create_success");
        // return "products/msg";

        // type 3 권장
        // return "redirect:/products/list_all"; // /templates/products/list_all.html

        // System.out.println("-> productsVO.getCateno(): " + productsVO.getCateno());
        // ra.addFlashAttribute("cateno", productsVO.getCateno()); // controller ->
        // controller: X

        // return "redirect:/products/list_all"; // /templates/products/list_all.html
        
        // 등록한 카테고리로 다시 목록 페이지 이동
        ra.addAttribute("cateno", productsVO.getCateno()); // controller -> controller: O
        return "redirect:/products/list_by_cateno";

        // return "redirect:/products/list_by_cateno?cateno=" + productsVO.getCateno();
        // // /templates/products/list_by_cateno.html
      } else { // 등록 실패
        ra.addFlashAttribute("code", Tool.CREATE_FAIL); // DBMS 등록 실패
        ra.addFlashAttribute("cnt", 0); // 업로드 실패
        ra.addFlashAttribute("url", "/products/msg"); // msg.html, redirect parameter 적용
        return "redirect:/products/msg"; // Post -> Get - param...
        // =============== (3) 결과 처리 종료 ===============
      }
    } else { // 로그인 실패 한 경우
      // /member/login_cookie_need.html
      // 로그인하지 않았거나 관리자가 아닌 경우 로그인 요청 페이지로 리다이렉트
      // 이부분 역시 수정해야 함.
      return "redirect:/member/login_cookie_need?url=/products/create?cateno=" + productsVO.getCateno(); 
    }
  }  
  
  
  
// ===================================================
// member 수정했던것  하지만 미완성
//  /**
//   * 등록 처리 http://localhost:9093/products/create
//   * 
//   * @return
//   */
//  @PostMapping(value = "/create")
//  public String create_proc(HttpServletRequest request, 
//                                    HttpSession session, 
//                                    Model model, 
//                                    @ModelAttribute("productsVO") ProductsVO productsVO,
//                                    RedirectAttributes ra) {
//    // 로그인 및 권한 체크
//    Integer memberno = (Integer) session.getAttribute("memberno");
//    
//    Integer grade = null;
//    try {
//      grade = Integer.valueOf(String.valueOf(session.getAttribute("grade")));
//    } catch (NumberFormatException e) {
//      grade = null;
//    }
//
//    if (memberno != null && grade != null && (grade >= 1 && grade <= 15)) { // admin(1~4), member(5~15)
//      // ------------------------------------------------------------------------------
//      // 파일 전송 코드 시작
//      // ------------------------------------------------------------------------------
//      String file1 = ""; // 원본 파일명 image
//      String file1saved = ""; // 저장된 파일명, image
//      String thumb1 = ""; // preview image
//
//      String upDir = Products.getUploadDir(); // 파일을 업로드할 폴더 준비
//      // upDir = upDir + "/" + 한글을 제외한 카테고리 이름
//      System.out.println("-> upDir: " + upDir);
//
//      // 전송 파일이 없어도 file1MF 객체가 생성됨.
//      // <input type='file' class="form-control" name='file1MF' id='file1MF'
//      // value='' placeholder="파일 선택">
//      MultipartFile mf = productsVO.getFile1MF();
//
//      file1 = mf.getOriginalFilename(); // 원본 파일명 산출, 01.jpg
//      
////      if (file1.toLowerCase().endsWith("jpeg")) {
////        file1 = file1.substring(0, file1.indexOf(".")) + ".jpg";
////      }
//      
//      System.out.println("-> 원본 파일명 산출 file1: " + file1);
//
//      long size1 = mf.getSize(); // 파일 크기
//      
//      if (size1 > 0) { // 파일 크기 체크, 파일을 올리는 경우
//        if (Tool.checkUploadFile(file1) == true) { // 업로드 가능한 파일인지 검사
//          // 파일 저장 후 업로드된 파일명이 리턴됨, spring.jsp, spring_1.jpg, spring_2.jpg...
//          file1saved = Upload.saveFileSpring(mf, upDir);
//
//          if (Tool.isImage(file1saved)) { // 이미지인지 검사
//            // thumb 이미지 생성후 파일명 리턴됨, width: 200, height: 150
//            thumb1 = Tool.preview(upDir, file1saved, 200, 150);
//          }
//
//          productsVO.setFile1(file1); // 순수 원본 파일명
//          productsVO.setFile1saved(file1saved); // 저장된 파일명(파일명 중복 처리)
//          productsVO.setThumb1(thumb1); // 원본이미지 축소판
//          productsVO.setSize1(size1); // 파일 크기
//
//        } else { // 전송 못하는 파일 형식
//          ra.addFlashAttribute("code", Tool.UPLOAD_FILE_CHECK_FAIL); // 업로드 할 수 없는 파일
//          ra.addFlashAttribute("cnt", 0); // 업로드 실패
//          ra.addFlashAttribute("url", "/products/msg"); // msg.html, redirect parameter 적용
//          
//          return "redirect:/products/post2get"; // Post -> Get -> /products/msg.html
//        }
//      }
//      
//      
//      
////      } else { // 글만 등록하는 경우
////        System.out.println("-> 글만 등록");
////      }
//
//      // ------------------------------------------------------------------------------
//      // 파일 전송 코드 종료
//      // ------------------------------------------------------------------------------
//
//      // Call By Reference: 메모리 공유, Hashcode 전달
//      //int memberno = (int) session.getAttribute("memberno"); // memberno FK
//      productsVO.setMemberno(memberno);
//      
//      int cnt = this.productsProc.create(productsVO);
//
//      // ------------------------------------------------------------------------------
//      // PK의 return
//      // ------------------------------------------------------------------------------
//      // System.out.println("--> productsno: " + productsVO.getProductsno());
//      // mav.addObject("productsno", productsVO.getProductsno()); // redirect
//      // parameter 적용
//      // ------------------------------------------------------------------------------
//
//      if (cnt == 1) {
//        // type 1, 재업로드 발생
//        // return "<h1>파일 업로드 성공</h1>"; // 연속 파일 업로드 발생
//
//        // type 2, 재업로드 발생
//        // model.addAttribute("cnt", cnt);
//        // model.addAttribute("code", "create_success");
//        // return "products/msg";
//
//        // type 3 권장
//        // return "redirect:/products/list_all"; // /templates/products/list_all.html
//
//        // System.out.println("-> productsVO.getCateno(): " + productsVO.getCateno());
//        // ra.addFlashAttribute("cateno", productsVO.getCateno()); // controller ->
//        // controller: X
//
//        // return "redirect:/products/list_all"; // /templates/products/list_all.html
//        
//        ra.addAttribute("cateno", productsVO.getCateno()); // controller -> controller: O
//        return "redirect:/products/list_by_cateno";
//
//        // return "redirect:/products/list_by_cateno?cateno=" + productsVO.getCateno();
//        // // /templates/products/list_by_cateno.html
//      } else {
//        ra.addFlashAttribute("code", Tool.CREATE_FAIL); // DBMS 등록 실패
//        ra.addFlashAttribute("cnt", 0); // 업로드 실패
//        ra.addFlashAttribute("url", "/products/msg"); // msg.html, redirect parameter 적용
//        return "redirect:/products/msg"; // Post -> Get - param...
//      }
//    } else { // 로그인 실패 한 경우
//      // /member/login_cookie_need.html
//      return "redirect:/member/login_cookie_need?url=/products/create?cateno=" + productsVO.getCateno(); 
//    }
//  }

  /**
   * 전체 목록, 관리자만 사용 가능 http://localhost:9093/products/list_all
   * 
   * @return
   */
  @GetMapping(value = "/list_all")
  public String list_all(HttpSession session, Model model) {
    // System.out.println("-> list_all");
    // 상단 메뉴(카테고리 목록) 불러오기 → 화면 상단의 메뉴바 등에서 사용됨
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);

    // 관리자 권한이 있는 사용자인지 확인
    if (this.memberProc.isAdmin(session)) { // 관리자만 조회 가능
      // 모든 상품 목록을 가져옴
      ArrayList<ProductsVO> list = this.productsProc.list_all(); // 모든 목록

      // Thymeleaf는 CSRF(크로스사이트) 스크립팅 해킹 방지 자동 지원
      // for문을 사용하여 객체를 추출, Call By Reference 기반의 원본 객체 값 변경
//      for (ProductsVO productsVO : list) {
//        String title = productsVO.getTitle();
//        String content = productsVO.getContent();
//        
//        title = Tool.convertChar(title);  // 특수 문자 처리
//        content = Tool.convertChar(content); 
//        
//        productsVO.setTitle(title);
//        productsVO.setContent(content);  
//
//      }
      // 모델에 상품 목록 데이터를 담아서 View로 전달
      model.addAttribute("list", list);
      
      // /templates/products/list_all.html 파일로 포워딩
      return "products/list_all";

    } else {
      // 관리자 로그인이 안 되어 있으면 로그인 안내 페이지로 이동
      return "redirect:/member/login_cookie_need";

    }

  }

//  /**
//   * 유형 1
//   * 카테고리별 목록
//   * http://localhost:9093/products/list_by_cateno?cateno=5
//   * http://localhost:9093/products/list_by_cateno?cateno=6 
//   * @return
//   */
//  @GetMapping(value="/list_by_cateno")
//  public String list_by_cateno(HttpSession session, Model model, 
//                                        @RequestParam(name="cateno", defaultValue = "" ) int cateno) {
//    ArrayList<CateVOMenu> menu = this.cateProc.menu();
//    model.addAttribute("menu", menu);
//    
//     CateVO cateVO = this.cateProc.read(cateno);
//     model.addAttribute("cateVO", cateVO);
//    
//    ArrayList<ProductsVO> list = this.productsProc.list_by_cateno(cateno);
//    model.addAttribute("list", list);
//    
//    // System.out.println("-> size: " + list.size());
//
//    return "products/list_by_cateno";
//  }

//  /**
//   * 유형 2
//   * 카테고리별 목록 + 검색
//   * http://localhost:9093/products/list_by_cateno?cateno=5&word=까페
//   * http://localhost:9093/products/list_by_cateno?cateno=6&word=까페 
//   * @return
//   */
//  @GetMapping(value="/list_by_cateno")
//  public String list_by_cateno_search(HttpSession session, Model model, 
//                                                  @RequestParam(name="cateno", defaultValue = "0")  int cateno, 
//                                                  @RequestParam(name="word", defaultValue = "") String word) {
//    ArrayList<CateVOMenu> menu = this.cateProc.menu();
//    model.addAttribute("menu", menu);
//    
//     CateVO cateVO = this.cateProc.read(cateno);
//     model.addAttribute("cateVO", cateVO);
//    
//     word = Tool.checkNull(word).trim();
//     
//     HashMap<String, Object> map = new HashMap<>();
//     map.put("cateno", cateno);
//     map.put("word", word);
//     
//    ArrayList<ProductsVO> list = this.productsProc.list_by_cateno_search(map);
//    model.addAttribute("list", list);
//    
//    // System.out.println("-> size: " + list.size());
//    model.addAttribute("word", word);
//    
//    int search_count = list.size(); // 검색된 레코드 갯수
//    model.addAttribute("search_count", search_count);
//    
//    return "products/list_by_cateno_search"; // /templates/products/list_by_cateno_search.html
//  }

  /**
   * 유형 3
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
    return "products/list_by_cateno_search_paging"; // /templates/products/list_by_cateno_search_paging.html
  }
  
//  /*
//   * /list_by_memberno는 로그인한 member의 개인 작업(수정/삭제) 전용
//   */
//  @GetMapping("/list_by_memberno")
//  public String list_by_memberno(HttpSession session, Model model) {
//    Integer memberno = (Integer) session.getAttribute("memberno");
//    Integer grade = null;
//    try {
//      grade = Integer.valueOf(String.valueOf(session.getAttribute("grade")));
//    } catch (NumberFormatException e) {
//      grade = null;
//    }
//
//    if (memberno != null && grade != null && grade >= 5 && grade <= 15) {
//      ArrayList<ProductsVO> list = this.productsProc.list_by_memberno(memberno);
//      model.addAttribute("list", list);
//      return "products/list_by_memberno"; // 뷰: 본인 글만 출력
//    } else {
//      return "redirect:/member/login_cookie_need?url=/products/list_by_memberno";
//    }
//  }

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

//  /**
//   * 조회 http://localhost:9093/products/read?productsno=17
//   * 
//   * @return
//   */
//  @GetMapping(value = "/read")
//  public String read(Model model, 
//                            @RequestParam(name="productsno", defaultValue = "0") int productsno, 
//                            @RequestParam(name="word", defaultValue = "") String word, 
//                            @RequestParam(name="now_page", defaultValue = "1") int now_page) { 
//    ArrayList<CateVOMenu> menu = this.cateProc.menu();
//    model.addAttribute("menu", menu);
//
//    ProductsVO productsVO = this.productsProc.read(productsno);
//
////    String title = productsVO.getTitle();
////    String content = productsVO.getContent();
////    
////    title = Tool.convertChar(title);  // 특수 문자 처리
////    content = Tool.convertChar(content); 
////    
////    productsVO.setTitle(title);
////    productsVO.setContent(content);  
//
//    long size1 = productsVO.getSize1();
//    String size1_label = Tool.unit(size1);
//    productsVO.setSize1_label(size1_label);
//
//    model.addAttribute("productsVO", productsVO);
//
//    CateVO cateVO = this.cateProc.read(productsVO.getCateno());
//    model.addAttribute("cateVO", cateVO);
//
//    // 조회에서 화면 하단에 출력
//    // ArrayList<ReplyVO> reply_list = this.replyProc.list_products(productsno);
//    // mav.addObject("reply_list", reply_list);
//
//    model.addAttribute("word", word);
//    model.addAttribute("now_page", now_page);
//
//    return "products/read";
//  }

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

//    String title = productsVO.getTitle();
//    String content = productsVO.getContent();
//    
//    title = Tool.convertChar(title);  // 특수 문자 처리
//    content = Tool.convertChar(content); 
//    
//    productsVO.setTitle(title);
//    productsVO.setContent(content);  

    // 파일 사이즈 단위 변환 (ex: 103400 → 101KB)
    long size1 = productsVO.getSize1();
    String size1_label = Tool.unit(size1);
    productsVO.setSize1_label(size1_label); // VO에 보기 좋게 가공한 크기 저장

    model.addAttribute("productsVO", productsVO); // 상품 정보 View로 전달

    // ---------------------------------------------
    // (3) 현재 상품이 속한 카테고리 정보 가져오기
    // ---------------------------------------------
    CateVO cateVO = this.cateProc.read(productsVO.getCateno()); // 상품의 카테고리 번호로 조회
    model.addAttribute("cateVO", cateVO); // 카테고리 정보 View에 전달

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
  
  /**
   * 맵 등록/수정/삭제 폼 http://localhost:9093/products/map?productsno=1
   * @param productsno 조회할 상품 번호
   * @return /templates/products/map.html 화면 반환
   */
  @GetMapping(value = "/map")
  public String map(Model model, 
                            @RequestParam(name="productsno", defaultValue = "0") int productsno) {
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
                                      @RequestParam(name="map", defaultValue = "") String map) {
    // 파라미터를 Map 형태로 포장
    HashMap<String, Object> hashMap = new HashMap<String, Object>();
    hashMap.put("productsno", productsno); // 어떤 상품에 대해 적용할지
    hashMap.put("map", map);  // 입력된 지도 HTML 코드

    // DB에 map 컬럼 업데이트
    this.productsProc.map(hashMap); // 실제 등록/수정/삭제 처리 메서드 호출

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

  /**
   * 텍스트 수정 폼 http:// localhost:9093/products/update_text?productsno=1
   *@return /templates/products/update_text.html 또는 로그인 페이지로 리디렉션
   */
  @GetMapping(value = "/update_text")
  public String update_text(HttpSession session, Model model, 
      RedirectAttributes ra,
      @RequestParam(name="productsno", defaultValue = "0") int productsno, 
      @RequestParam(name="word", defaultValue = "") String word,
      @RequestParam(name="now_page", defaultValue = "1") int now_page
      ) {
    
    // 상단 카테고리 메뉴 전달
    ArrayList<CateVOMenu> menu = this.cateProc.menu();
    model.addAttribute("menu", menu);

    // 검색어 및 현재 페이지 상태 유지
    model.addAttribute("word", word);
    model.addAttribute("now_page", now_page);

    // 관리자 로그인 확인
    if (this.memberProc.isAdmin(session)) { // 관리자로 로그인한경우
      ProductsVO productsVO = this.productsProc.read(productsno);  // 상품 정보 조회
      model.addAttribute("productsVO", productsVO);

      CateVO cateVO = this.cateProc.read(productsVO.getCateno()); // 카테고리 정보 조회
      model.addAttribute("cateVO", cateVO);

      // 텍스트 수정 폼으로 이동
      return "products/update_text"; // /templates/products/update_text.html
      // return "products/update_text_ai"; // /templates/products/update_text_ai.html
      // String content = "장소:\n인원:\n준비물:\n비용:\n기타:\n";
      // model.addAttribute("content", content);

    } else {
      // 로그인후 텍스트 수정폼이 자동으로 열림.
      return "redirect:/member/login_cookie_need?url=/products/update_text?productsno=" + productsno;
    }

  }

  /**
   * 텍스트 수정 처리 http://localhost:9093/products/update_text?productsno=1
   * 
   * @return
   */
  @PostMapping(value = "/update_text")
  public String update_text_proc(HttpSession session, Model model, ProductsVO productsVO, 
           RedirectAttributes ra,
           @RequestParam(name="search_word", defaultValue = "") String search_word, // productsVO.word와 구분 필요
           @RequestParam(name="now_page", defaultValue = "1") int now_page
           ) {
    
    System.out.println(">>> 사용자가 입력한 비밀번호 (암호화 전): " + productsVO.getPasswd()); // ✅ 여기 추가
    
    ra.addAttribute("word", search_word);  // productsVO.word와 구분 필요
    ra.addAttribute("now_page", now_page);

    if (this.memberProc.isAdmin(session)) { // 관리자 로그인 확인
      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("productsno", productsVO.getProductsno());
      map.put("passwd", productsVO.getPasswd());
      
      if (this.productsProc.password_check(map) == 1) { // 패스워드 일치
        // this.productsProc.update_text(productsVO); // 글수정 // 아래 두개 추가하면서 잠시 위에 뺌
        int cnt = this.productsProc.update_text(productsVO);  // 🔹 추가
        System.out.println("→ DB 수정 결과: " + cnt);           // 🔹 추가

        // mav 객체 이용
        ra.addAttribute("productsno", productsVO.getProductsno());
        ra.addAttribute("cateno", productsVO.getCateno()); 
        return "redirect:/products/read"; // @GetMapping(value = "/read")

      } else { // 패스워드 불일치
        ra.addFlashAttribute("code", Tool.PASSWORD_FAIL); // redirect -> forward -> html template에 변수 전달
        ra.addFlashAttribute("cnt", 0);
        model.addAttribute("productsVO", productsVO);
        
        return "redirect:/products/post2get"; // Post -> Get -> /products/msg.html
      }
    } else { // 정상적인 로그인이 아닌 경우 로그인 유도
      // 로그인 안함 -> http://localhost:9093/products/update_text?productsno=32&now_page=1&word=
      return "redirect:/products/list_by_cateno_search_paging?cateno=" + productsVO.getCateno() + "&now_page=" + now_page + "&word=" + search_word;
    }

  }

  /**
   * 파일 수정 폼 http://localhost:9093/products/update_file?productsno=1
   * @return templates/products/update_file.html
   */
  @GetMapping(value = "/update_file")
  public String update_file(HttpSession session, Model model, 
                                    @RequestParam(name="productsno", defaultValue = "0") int productsno, 
                                    @RequestParam(name="word", defaultValue = "") String word,
                                    @RequestParam(name="now_page", defaultValue = "1") int now_page
                                      ) {
    // 관리자로 로그인한경우
    if (this.memberProc.isAdmin(session)) { // 관리자로 로그인한경우
      
      // 상단 메뉴 구성
      ArrayList<CateVOMenu> menu = this.cateProc.menu();
      model.addAttribute("menu", menu);
      
      // 검색어 및 페이지 상태 유지
      model.addAttribute("word", word);
      model.addAttribute("now_page", now_page);
      
      // 기존 상품 정보 불러오기 (기존 파일명 포함)
      ProductsVO productsVO = this.productsProc.read(productsno);
      model.addAttribute("productsVO", productsVO);

      // 해당 상품의 카테고리 정보도 같이 전달
      CateVO cateVO = this.cateProc.read(productsVO.getCateno());
      model.addAttribute("cateVO", cateVO);

      return "products/update_file";   // products/update_file.html  
    } else {
      // 로그인후 파일 수정폼이 자동으로 열림.
      return "redirect:/member/login_cookie_need?url=/products/update_file?productsno=" + productsno;

    }

  }

  /**
   * 파일 수정 처리 http://localhost:9093/products/update_file
   * @return 수정 후 상세 페이지로 리디렉션
   */
  @PostMapping(value = "/update_file")
  public String update_file_proc(HttpSession session, Model model, RedirectAttributes ra,
                                      ProductsVO productsVO,
                                      @RequestParam(name="word", defaultValue = "") String word,
                                      @RequestParam(name="now_page", defaultValue = "1") int now_page
                                      ) {
    
    // 관리자 권한 확인
    if (this.memberProc.isAdmin(session)) {
      // 1. 기존 파일 정보 읽기 (삭제용), 기존에 등록된 레코드 저장용
      ProductsVO productsVO_old = productsProc.read(productsVO.getProductsno());
      
      // 디버깅용 로그
      System.out.println("== 기존 파일 정보 ==");
      System.out.println("file1saved: " + productsVO_old.getFile1saved());
      System.out.println("thumb1: " + productsVO_old.getThumb1());
      
      // -------------------------------------------------------------------
      // 파일 삭제 시작
      // -------------------------------------------------------------------
      String file1saved = productsVO_old.getFile1saved(); // 실제 저장된 파일명
      String thumb1 = productsVO_old.getThumb1(); // 실제 저장된 preview 이미지 파일명
      long size1 = 0;

      // 2. 파일이 저장된 실제 디렉토리 경로
      String upDir = Products.getUploadDir(); // C:/kd/deploy/team/products/storage/
      System.out.println("== 업로드 디렉토리 ==");
      System.out.println("upDir: " + upDir);
      
      // 3. 기존 파일 삭제
      Tool.deleteFile(upDir, file1saved); // 실제 저장된 파일삭제
      Tool.deleteFile(upDir, thumb1); // preview 이미지 삭제
      // -------------------------------------------------------------------
      // 파일 삭제 종료
      // -------------------------------------------------------------------

      // -------------------------------------------------------------------
      // 파일 전송 시작
      // -------------------------------------------------------------------
      // 4. 새 파일 업로드 처리
      String file1 = ""; // 원본 파일명 image
      
      // 전송 파일이 없어도 file1MF 객체가 생성됨.
      // <input type='file' class="form-control" name='file1MF' id='file1MF'
      // value='' placeholder="파일 선택">
      
      MultipartFile mf = productsVO.getFile1MF(); // 전송된 파일 객체
      file1 = mf.getOriginalFilename(); // 원본 파일명
      size1 = mf.getSize(); // 파일 크기
      
      // 디버깅용 로그
      System.out.println("== 업로드된 새 파일 정보 ==");
      System.out.println("원본 파일명 file1: " + file1);
      System.out.println("파일 크기 size1: " + size1);


      if (size1 > 0) { // 새 파일이 존재할 경우, 폼에서 새롭게 올리는 파일이 있는지 파일 크기로 체크 ★
        // 파일 저장 후 업로드된 파일명이 리턴됨, spring.jsp, spring_1.jpg...
        file1saved = Upload.saveFileSpring(mf, upDir); // 실제 서버에 저장
        System.out.println("저장된 파일명 file1saved: " + file1saved);

        if (Tool.isImage(file1saved)) { // 이미지인지 검사
          // thumb 이미지 생성후 파일명 리턴됨, width: 250, height: 200
          thumb1 = Tool.preview(upDir, file1saved, 250, 200); // 썸네일 생성
          System.out.println("생성된 썸네일 thumb1: " + thumb1);
        }

      } else { // 파일이 삭제만 되고 새로 올리지 않는 경우
        file1 = "";
        file1saved = "";
        thumb1 = "";
        size1 = 0;
        System.out.println("※ 새 파일 없음 또는 업로드 실패");
      }

      productsVO.setFile1(file1); // 원본 파일명
      productsVO.setFile1saved(file1saved); // 서버에 저장된 파일명
      productsVO.setThumb1(thumb1); // 썸네일
      productsVO.setSize1(size1); // 파일 크기
      // -------------------------------------------------------------------
      // 파일 전송 코드 종료
      // -------------------------------------------------------------------

      // 6. DB 정보 업데이트 실행
      this.productsProc.update_file(productsVO); // Oracle 처리
      
      // 7. 리디렉션 파라미터 전달
      ra.addAttribute ("productsno", productsVO.getProductsno());
      ra.addAttribute("cateno", productsVO.getCateno());
      ra.addAttribute("word", word);
      ra.addAttribute("now_page", now_page);
      
      return "redirect:/products/read";  // 상세 페이지로 이동
      
    } else {
      // 권한 없음 → 로그인 유도
      ra.addAttribute("url", "/member/login_cookie_need"); 
      return "redirect:/products/post2get"; // GET
    }
  }

  /**
   * 파일 삭제 폼
   * http://localhost:9093/products/delete?productsno=1
   * @return /templates/products/delete.html
   */
  @GetMapping(value = "/delete")
  public String delete(HttpSession session, Model model, RedirectAttributes ra,
                              @RequestParam(name="cateno", defaultValue = "0") int cateno, 
                              @RequestParam(name="productsno", defaultValue = "0") int productsno, 
                              @RequestParam(name="word", defaultValue = "") String word,
                              @RequestParam(name="now_page", defaultValue = "1") int now_page                               
                               ) {
    // 관리자만 삭제 가능
    if (this.memberProc.isAdmin(session)) { // 관리자로 로그인한경우
      
      model.addAttribute("cateno", cateno);
      model.addAttribute("word", word);
      model.addAttribute("now_page", now_page);
      
      // 상단 메뉴
      ArrayList<CateVOMenu> menu = this.cateProc.menu();
      model.addAttribute("menu", menu);
      
      // 삭제할 상품 정보
      ProductsVO productsVO = this.productsProc.read(productsno);
      model.addAttribute("productsVO", productsVO);
      
      // 해당 상품의 카테고리 정보
      CateVO cateVO = this.cateProc.read(productsVO.getCateno());
      model.addAttribute("cateVO", cateVO);
      
      return "products/delete"; // forward
      
    } else {
      // 로그인 안 한 경우, 로그인 후 폼으로 자동 이동
      // 로그인후 파일 수정폼이 자동으로 열림.
      // http://localhost:9093/products/delete?productsno=35&word=&now_page=1&cateno=4
      return "redirect:/member/login_cookie_need?url=/products/delete?productsno=" + productsno;

    }

  }
  
  /**
   * 삭제 처리 http://localhost:9093/products/delete
   * @return 삭제 후 목록 페이지로 리디렉션
   */
  @PostMapping(value = "/delete")
  public String delete_proc(RedirectAttributes ra,
                                    @RequestParam(name="cateno", defaultValue = "0") int cateno, 
                                    @RequestParam(name="productsno", defaultValue = "0") int productsno, 
                                    @RequestParam(name="word", defaultValue = "") String word,
                                    @RequestParam(name="now_page", defaultValue = "1") int now_page   
                                    ) {
    // -------------------------------------------------------------------
    // (1) 삭제할 파일 조회 및 물리적 삭제 시작
    // -------------------------------------------------------------------
    // 삭제할 파일 정보를 읽어옴.
    ProductsVO productsVO_read = productsProc.read(productsno); // DB에서 정보 조회
        
    String file1saved = productsVO_read.getFile1saved(); // 실제 파일명
    String thumb1 = productsVO_read.getThumb1(); // 썸네일 파일명
    
    String uploadDir = Products.getUploadDir(); // 업로드 디렉토리 경로

    Tool.deleteFile(uploadDir, file1saved);  // 실제 저장된 파일삭제
    Tool.deleteFile(uploadDir, thumb1);     // preview 이미지 삭제
    // -------------------------------------------------------------------
    // 파일 삭제 종료
    // -------------------------------------------------------------------
        
    
    // ------------------------------------------------------------
    // (2) DB에서 상품 정보 삭제
    // ------------------------------------------------------------ 
    this.productsProc.delete(productsno); // DBMS 삭제
        
    // -------------------------------------------------------------------------------------
    // (3) 마지막 페이지의 마지막 레코드 삭제시의 페이지 번호 -1 처리
    // -------------------------------------------------------------------------------------    
    // 마지막 페이지의 마지막 10번째 레코드를 삭제후
    // 하나의 페이지가 3개의 레코드로 구성되는 경우 현재 9개의 레코드가 남아 있으면
    // 페이지수를 4 -> 3으로 감소 시켜야함, 마지막 페이지의 마지막 레코드 삭제시 나머지는 0 발생
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("cateno", cateno);
    map.put("word", word);
    
    if (this.productsProc.list_by_cateno_search_count(map) % Products.RECORD_PER_PAGE == 0) { // (예: 10개 단위로 떨어짐)
      now_page = now_page - 1; // 삭제시 DBMS는 바로 적용되나 크롬은 새로고침등의 필요로 단계가 작동 해야함.
      if (now_page < 1) {
        now_page = 1; // 시작 페이지, 최소 페이지 보정
      }
    }
    // -------------------------------------------------------------------------------------

    // ------------------------------------------------------------
    // (4) 목록 페이지로 리디렉션
    // ------------------------------------------------------------
    ra.addAttribute("cateno", cateno);
    ra.addAttribute("word", word);
    ra.addAttribute("now_page", now_page);
    
    return "redirect:/products/list_by_cateno";    
    
  }   

  /**
   * 추천 처리 http://localhost:9093/products/good
   * 상품 추천/추천 해제 처리 (AJAX 방식)
   * @param json_src JSON 형식 문자열 (예: {"productsno": "5"})
   * @return JSON 응답 문자열 (추천 상태 및 추천수 포함)
   */
  @PostMapping(value = "/good")
  @ResponseBody
  public String good(HttpSession session, Model model, @RequestBody String json_src){ 
    System.out.println("-> json_src: " + json_src); // json_src: {"productsno":"5"} // 클라이언트로부터 받은 JSON 문자열
    
    // ---------------------------
    // 1. JSON 파싱 및 상품 번호 추출
    // ---------------------------
    JSONObject src = new JSONObject(json_src); // String -> JSON  (문자열을 JSON 객체로 변환)
    int productsno = (int)src.get("productsno"); // 값 가져오기
    System.out.println("-> productsno: " + productsno);
        
    // ---------------------------
    // 2. 로그인 여부 확인
    // ---------------------------
    if (this.memberProc.isMember(session)) { // 회원 로그인 확인
      // 추천을 한 상태인지 확인
      int memberno = (int)session.getAttribute("memberno");
      
      // ---------------------------
      // 3. 현재 추천 여부 확인
      // ---------------------------
      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("productsno", productsno);
      map.put("memberno", memberno);
      
      int good_cnt = this.productsgoodProc.hartCnt(map); // 추천 여부: 0 or 1
      System.out.println("-> good_cnt: " + good_cnt);
      
      if (good_cnt == 1) { // 이미지 추천을 한 회원인지 검사, (1) -> 이미 추천한 경우 → 추천 해제
        System.out.println("-> 추천 해제: " + productsno + ' ' + memberno);
        
        // 추천 기록 식별
        // Productsgood 테이블에서 추천한 기록을 찾음
        ProductsgoodVO productsgoodVO = this.productsgoodProc.readByProductsnoMemberno(map);
        
        // 추천 기록 삭제 + 추천수 감소
        this.productsgoodProc.delete(productsgoodVO.getProductsgoodno()); // 추천 기록 삭제
        this.productsProc.decreaseRecom(productsno); // 추천 카운트 감소
        
      } else { // 추천하지 않은 경우 → 추천 등록
        System.out.println("-> 추천: " + productsno + ' ' + memberno); 
        
        ProductsgoodVO productsgoodVO_new = new ProductsgoodVO();
        productsgoodVO_new.setProductsno(productsno);
        productsgoodVO_new.setMemberno(memberno);
        
        this.productsgoodProc.create(productsgoodVO_new);
        this.productsProc.increaseRecom(productsno); // 카운트 증가 =  추천수 증가
      }
      
      // ---------------------------
      // 4. 최종 상태 재확인 및 응답 구성
      // ---------------------------
      // 추천 여부가 변경되어 다시 새로운 값을 읽어옴.
      int hartCnt = this.productsgoodProc.hartCnt(map);  // 현재 추천 여부
      int recom = this.productsProc.read(productsno).getRecom();  // 현재 총 추천수
            
      JSONObject result = new JSONObject(); 
      result.put("isMember", 1); // 로그인 상태 -> 로그인: 1, 비회원: 0
      result.put("hartCnt", hartCnt); // 추천 여부, 추천:1, 비추천: 0
      result.put("recom", recom);   // 추천 총계
      
      System.out.println("-> result.toString(): " + result.toString());
      return result.toString();
      
    } else { // 정상적인 로그인이 아닌 경우(비회원이 요청한 경우) 로그인 유도
      JSONObject result = new JSONObject();
      result.put("isMember", 0); // 로그인: 1, 비회원: 0
      
      System.out.println("-> result.toString(): " + result.toString());
      return result.toString();
    }

  }

  
  
}

