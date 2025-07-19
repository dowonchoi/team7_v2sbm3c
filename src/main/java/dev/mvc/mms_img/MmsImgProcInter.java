package dev.mvc.mms_img;

import java.util.List;

public interface MmsImgProcInter {
    /**
     * MMS 이미지 생성 (DB 저장)
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
     * 텍스트와 합성 이미지 업데이트
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
