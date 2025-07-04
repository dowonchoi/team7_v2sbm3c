package dev.mvc.notice;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class NoticeVO {
  
  /** 공지사항 번호 */
  private int notice_id;
  
  /** 제목 */
  private String title;
  
  /** 내용 */
  private String content;
  
  /** 카테고리 */
  private String cate;
  
  /** 상단 고정 여부 ('Y'/'N') */
  private String is_fixed = "N";
  
  /** 조회수 */
  private int view_count;
  
  /** 작성자 ID */
  private String writer_id;
  
  /** 작성자 이름 */
  private String writer_name;
  
  /** 등록일 */
  private Date reg_date;
  
  /** 수정일 */
  private String upd_date;
  
  private String image;
  
}
