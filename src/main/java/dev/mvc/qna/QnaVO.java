package dev.mvc.qna;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class QnaVO {
    
    /** Q&A 번호 */
    private int qna_id;
    
    private int memberno;
    
    /** 제목 */
    private String title;
    
    /** 내용 */
    private String content;
    
    /** 작성자 ID */
    private String writer_id;
    
    /** 작성자 이름 */
    private String writer_name;
    
    /** 사용자 유형 (user: 소비자 / supplier: 공급자) */
    private String user_type;
    
    /** 등록일 */
    private Timestamp reg_date;
    
    /** 댓글 수 */
    private int comment_count;
    
    /** ✅ 카테고리 */
    private String cate;
    
    /** 댓글 내용 (※ 현재 테이블에 있으나 사실상 안 써도 되는 컬럼임 → qna_comment로 관리 권장) */
    private String comment;
    
    /** ✅ 공식 답변 (reply) 컬럼 추가 */
    private String reply;
    
    private int view_count;
    
    private String reply_writer; // 답변 작성자
    
    /** 기타 필요한 컬럼 추가 가능 */
}
