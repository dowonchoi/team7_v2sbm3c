package dev.mvc.qnacomment;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QnaCommentProc implements QnaCommentProcInter {

    @Autowired
    private QnaCommentDAOInter qnaCommentDAO;

    @Override
    public List<QnaCommentVO> listByQnaId(int qna_id) {
        return qnaCommentDAO.listByQnaId(qna_id);
    }

    @Override
    public int create(QnaCommentVO vo) {
        return qnaCommentDAO.create(vo);
    }

    @Override
    public int update(QnaCommentVO vo) {
        return qnaCommentDAO.update(vo);
    }

    @Override
    public int delete(int comment_id) {
        return qnaCommentDAO.delete(comment_id);
    }
}

