package dev.mvc.openai;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("dev.mvc.openai.MemberImgProc")
public class MemberImgProc implements MemberImgProcInter {

    @Autowired
    private MemberImgDAOInter memberImgDAO;

    @Override
    public int create(MemberImgVO vo) {
        return memberImgDAO.create(vo);
    }

    @Override
    public List<MemberImgVO> list_by_member(int memberno) {
        return memberImgDAO.list_by_member(memberno);
    }
}
