package dev.mvc.openai;

import java.util.List;

public interface MemberImgDAOInter {
    int create(MemberImgVO vo);
    List<MemberImgVO> list_by_member(int memberno);
}
