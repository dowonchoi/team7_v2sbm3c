package dev.mvc.mms_img;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MmsImgDAO implements MmsImgDAOInter {

    @Autowired
    private org.apache.ibatis.session.SqlSession sqlSession;

    private static final String NAMESPACE = "dev.mvc.mms_img.MmsImgMapper";

    @Override
    public int create(MmsImgVO vo) {
        return sqlSession.insert(NAMESPACE + ".create", vo);
    }

    @Override
    public MmsImgVO read(int mimgno) {
        return sqlSession.selectOne(NAMESPACE + ".read", mimgno);
    }

    @Override
    public List<MmsImgVO> list() {
        return sqlSession.selectList(NAMESPACE + ".list");
    }

    @Override
    public int updateTextAndFinalImage(MmsImgVO vo) {
        return sqlSession.update(NAMESPACE + ".updateTextAndFinalImage", vo);
    }



    @Override
    public int delete(int mimgno) {
        return sqlSession.delete(NAMESPACE + ".delete", mimgno);
    }
    
}
