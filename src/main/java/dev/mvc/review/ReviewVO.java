package dev.mvc.review;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class ReviewVO {
  private int reviewno;
  private int productsno;
  private int memberno;
  private String content;
  private Integer emotion;     // 1 or 0
  private String summary;
  private String rdate;
  private String mname;  // 조인용: 회원 이름
  private String title;  // 조인용: 상품명
  
  //---------------------------------------------------------------
   // 업로드된 이미지 파일 정보 (최대 3장)
   // ---------------------------------------------------------------
  
   // 첫 번째 이미지
   private String file1;        // 원래 파일명
   private String file1saved;   // 서버에 저장된 파일명
   private long size1;          // 파일 크기 (bytes)
  
   // 두 번째 이미지
   private String file2;
   private String file2saved;
   private long size2;
  
   // 세 번째 이미지
   private String file3;
   private String file3saved;
   private long size3;
   
  // ✅ 업로드용 필드 (DB 저장 X)
    private MultipartFile file1MF;
    private MultipartFile file2MF;
    private MultipartFile file3MF;

}
