package dev.mvc.mms_img;

import java.util.List;

public interface MmsSendLogProcInter {
    int create(MmsSendLogVO vo); // 로그 추가
    List<MmsSendLogVO> listByImgno(int mimgno); // 특정 이미지 발송 내역
}
