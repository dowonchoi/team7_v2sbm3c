package dev.mvc.qna;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class QnaVO {
    
    /** Q&A 번호 */
    private int qna_id;
    
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
    private java.sql.Timestamp reg_date;
    
    /** 댓글 수 */
    private int comment_count;
    
    /** ✅ cate 컬럼 추가 **/
    private String cate;
    
    private String comment;
    
    /** 기타 필요한 컬럼 추가 가능 */
}
