package dev.mvc.products;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/*
        productsno                            NUMBER(10)         NOT NULL         PRIMARY KEY,
        memberno                            NUMBER(10)     NOT NULL ,
        cateno                                NUMBER(10)         NOT NULL ,
        title                                 VARCHAR2(300)         NOT NULL,
        content                               CLOB                  NOT NULL,
        recom                                 NUMBER(7)         DEFAULT 0         NOT NULL,
        cnt                                   NUMBER(7)         DEFAULT 0         NOT NULL,
        replycnt                              NUMBER(7)         DEFAULT 0         NOT NULL,
        passwd                                VARCHAR2(15)         NOT NULL,
        word                                  VARCHAR2(300)         NULL ,
        rdate                                 DATE               NOT NULL,
        file1                                   VARCHAR(100)          NULL,
        file1saved                            VARCHAR(100)          NULL,
        thumb1                              VARCHAR(100)          NULL,
        size1                                 NUMBER(10)      DEFAULT 0 NULL,  
        price                                 NUMBER(10)      DEFAULT 0 NULL,  
        dc                                    NUMBER(10)      DEFAULT 0 NULL,  
        saleprice                            NUMBER(10)      DEFAULT 0 NULL,  
        point                                 NUMBER(10)      DEFAULT 0 NULL,  
        salecnt                               NUMBER(10)      DEFAULT 0 NULL,  
        map                                  VARCHAR2(1000)            NULL,
        youtube                             VARCHAR2(1000)            NULL,   
        mp4                                   VARCHAR2(100)              NULL,       
        price_before                         NUMBER(10)      DEFAULT 0 NOT NULL,
        price_now                            NUMBER(10)     DEFAULT 0 NOT NULL,
        discount                              NUMBER(3)       DEFAULT 0 NOT NULL,
 */

@Getter @Setter @ToString
public class ProductsVO {
    /** 컨텐츠 번호 */
    private int productsno;
    /** 관리자 권한의 회원 번호 */
    private int memberno;
    /** 카테고리 번호 */
    private int cateno;
    /** 제목 */
    private String title = "";
    /** 내용 */
    private String content = "";
    /** 추천수 */
    private int recom;
    /** 조회수 */
    private int cnt = 0;
    /** 댓글수 */
    private int replycnt = 0;
    /** 패스워드 */
    private String passwd = "";
    /** 검색어 */
    private String word = "";
    /** 등록 날짜 */
    private String rdate = "";
    /** 지도 */
    private String map = "";
    /** Youtube */
    private String youtube = "";

    /** mp4 */
    private String mp4 = "";
    
    /*정가*/
    private int price_before;
    /*판매가*/
    private int price_now;
    /*할인율(%)*/
    private int discount;
    
    // 파일 업로드 관련
    // -----------------------------------------------------------------------------------
    /**
    이미지 파일
    <input type='file' class="form-control" name='file1MF' id='file1MF' 
               value='' placeholder="파일 선택">
    */
    private MultipartFile file1MF = null;
    /** 메인 이미지 크기 단위, 파일 크기 */
    private String size1_label = "";
    /** 메인 이미지 */
    private String file1 = "";
    /** 실제 저장된 메인 이미지 */
    private String file1saved = "";
    /** 메인 이미지 preview */
    private String thumb1 = "";
    /** 메인 이미지 크기 */
    private long size1 = 0;
    
    // 추가 이미지 2
    private MultipartFile file2MF = null;
    private String size2_label;
    private String file2 = "";
    private String file2saved = "";
    private String thumb2 = "";
    private long size2 = 0;

    // 추가 이미지 3
    private MultipartFile file3MF = null;
    private String size3_label;
    private String file3 = "";
    private String file3saved = "";
    private String thumb3 = "";
    private long size3 = 0;


    // 쇼핑몰 상품 관련
    // -----------------------------------------------------------------------------------
    /** 정가 */
    private int price = 0;
    /** 할인률 */
    private int dc = 0;
    /** 판매가 */
    private int saleprice = 0;
    /** 포인트 */
    private int point = 0;
    /** 재고 수량 */
    private int salecnt = 0;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")  // ← 추가!
    private Date expdate;

 // 광고 이미지용 추가
    private MultipartFile fileAdMF;
    private String fileAd = "";
    private String fileAdsaved = "";
    private long sizeAd = 0;

    // Getter/Setter 추가
    public MultipartFile getFileAdMF() {
      return fileAdMF;
    }
    public void setFileAdMF(MultipartFile fileAdMF) {
      this.fileAdMF = fileAdMF;
    }
    public String getFileAd() {
      return fileAd;
    }
    public void setFileAd(String fileAd) {
      this.fileAd = fileAd;
    }
    public String getFileAdsaved() {
      return fileAdsaved;
    }
    public void setFileAdsaved(String fileAdsaved) {
      this.fileAdsaved = fileAdsaved;
    }
    public long getSizeAd() {
      return sizeAd;
    }
    public void setSizeAd(long sizeAd) {
      this.sizeAd = sizeAd;
    }
    
 // 상품 상세정보 필드
    private String item;           // 품목 또는 명칭
    private String maker;          // 생산자(수입자)
    private String makedate;       // 제조연월일, 소비기한
    private String importyn;       // 수입식품 문구 여부
    private String keep;           // 보관방법, 취급방법
    private String counsel_tel;    // 소비자상담 전화번호
    private String sizeinfo;       // 용량, 수량, 크기
    private String origin;         // 원산지
    private String detail;         // 세부 품목군별 표시사항
    private String pack;           // 상품 구성
    private String safe;           // 소비자 안전 주의사항

    // Getter/Setter
    public String getItem() {
      return item;
    }
    public void setItem(String item) {
      this.item = item;
    }

    public String getMaker() {
      return maker;
    }
    public void setMaker(String maker) {
      this.maker = maker;
    }

    public String getMakedate() {
      return makedate;
    }
    public void setMakedate(String makedate) {
      this.makedate = makedate;
    }

    public String getImportyn() {
      return importyn;
    }
    public void setImportyn(String importyn) {
      this.importyn = importyn;
    }

    public String getKeep() {
      return keep;
    }
    public void setKeep(String keep) {
      this.keep = keep;
    }

    public String getCounsel_tel() {
      return counsel_tel;
    }
    public void setCounsel_tel(String counsel_tel) {
      this.counsel_tel = counsel_tel;
    }

    public String getSizeinfo() {
      return sizeinfo;
    }
    public void setSizeinfo(String sizeinfo) {
      this.sizeinfo = sizeinfo;
    }

    public String getOrigin() {
      return origin;
    }
    public void setOrigin(String origin) {
      this.origin = origin;
    }

    public String getDetail() {
      return detail;
    }
    public void setDetail(String detail) {
      this.detail = detail;
    }

    public String getPack() {
      return pack;
    }
    public void setPack(String pack) {
      this.pack = pack;
    }

    public String getSafe() {
      return safe;
    }
    public void setSafe(String safe) {
      this.safe = safe;
    }

    private int qty; // 수량

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    private String country;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    private String imported;

    public String getImported() {
        return imported;
    }

    public void setImported(String imported) {
        this.imported = imported;
    }

    private String components;

    public String getComponents() {
        return components;
    }

    public void setComponents(String components) {
        this.components = components;
    }

    private String storage;

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }
    
    

}
