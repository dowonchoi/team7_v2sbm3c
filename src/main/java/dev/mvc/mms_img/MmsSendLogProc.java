package dev.mvc.mms_img;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service("dev.mvc.mms_img.MmsSendLogProc")
public class MmsSendLogProc implements MmsSendLogProcInter {

    @Autowired
    private MmsSendLogDAOInter dao;

    @Override
    public int create(MmsSendLogVO vo) {
        return dao.create(vo);
    }

    @Override
    public List<MmsSendLogVO> listByImgno(int mimgno) {
        return dao.listByImgno(mimgno);
    }
}


