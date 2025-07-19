package dev.mvc.mms_img;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dev.mvc.mms_img.MmsImgProc")
public class MmsImgProc implements MmsImgProcInter {

    @Autowired
    private MmsImgDAOInter mmsImgDAO;

    @Override
    public int create(MmsImgVO vo) {
        return mmsImgDAO.create(vo);
    }

    @Override
    public MmsImgVO read(int mimgno) {
        return mmsImgDAO.read(mimgno);
    }

    @Override
    public List<MmsImgVO> list() {
        return mmsImgDAO.list();
    }

    @Override
    public int updateTextAndFinalImage(MmsImgVO vo) {
        return this.mmsImgDAO.updateTextAndFinalImage(vo);
    }


    @Override
    public int delete(int mimgno) {
        return mmsImgDAO.delete(mimgno);
    }
    
    
}
