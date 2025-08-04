package dev.mvc.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

//CREATE TABLE calendar (
//    calendarno  NUMBER(10) NOT NULL, -- AUTO_INCREMENT 대체
//    labeldate   VARCHAR2(10)  NOT NULL, -- 출력할 날짜 2013-10-20
//    startdate   VARCHAR2(10)  NOT NULL, -- 일정 시작일
//    enddate     VARCHAR2(10)  NOT NULL, -- 일정 종료일
//    label       VARCHAR2(50)  NOT NULL, -- 달력에 출력될 레이블
//    title       VARCHAR2(100) NOT NULL, -- 제목(*)
//    content     CLOB          NOT NULL, -- 글 내용
//    cnt         NUMBER        DEFAULT 0, -- 조회수
//    seqno       NUMBER(5)     DEFAULT 1 NOT NULL, -- 일정 출력 순서
//    regdate     DATE          NOT NULL, -- 등록 날짜
//    memberno    NUMBER(10)     NOT NULL , -- FK
//    PRIMARY KEY (calendarno),
//    FOREIGN KEY (memberno) REFERENCES member (memberno) -- 일정을 등록한 관리자 
//  );

@Getter @Setter @ToString
public class CalendarVO {

  /** 일정 번호 */
  private int calendarno;

  /** 회원 번호 */
  private int memberno;
  
  /** 카테고리 번호 (✅ 추가) */
  private int cateno;

  /** 출력할 날짜 */
  private LocalDate labeldate;

  /** 시작 날짜 */
  private LocalDate startdate;

  /** 종료 날짜 */
  private LocalDate enddate;

  /** 출력 레이블 */
  private String label;

  /** 제목 */
  private String title;

  /** 내용 */
  private String content;

  /** 조회수 */
  private int cnt;
  
  /** 공개 여부 */
  private String visible;

  /** 등록 날짜 */
  private Date  regdate;
  
  private String regdateStr;
  
  private String id;
  
  //이미지 관련
   private String file1saved;
   private String file1origin;
   private long file1size;
  
   // 파일 업로드용
   private MultipartFile file1MF;
}

 