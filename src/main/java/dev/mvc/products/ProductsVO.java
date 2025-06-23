package dev.mvc.products;

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
    
  
}

