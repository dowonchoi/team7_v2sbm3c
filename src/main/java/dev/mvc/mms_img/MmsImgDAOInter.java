package dev.mvc.mms_img;

import java.util.List;

public interface MmsImgDAOInter {
    
    /**
     * MMS 이미지 생성 (OpenAI로 생성된 원본 이미지 저장)
     * @param vo
     * @return 저장된 행 개수
     */
    public int create(MmsImgVO vo);

    /**
     * MMS 이미지 상세 조회
     * @param mimgno
     * @return MmsImgVO
     */
    public MmsImgVO read(int mimgno);

    /**
     * 모든 MMS 이미지 목록
     * @return 이미지 리스트
     */
    public List<MmsImgVO> list();

    /**
     * MMS 이미지 상태 업데이트 (텍스트 합성 후)
     * @param vo
     * @return 수정된 행 개수
     */
    public int updateTextAndFinalImage(MmsImgVO vo);

    /**
     * MMS 이미지 삭제
     * @param mimgno
     * @return 삭제된 행 개수
     */
    public int delete(int mimgno);
    
    
}
