package dev.mvc.faq;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("dev.mvc.faq.FaqProc")
public class FaqProc implements FaqProcInter {
    @Autowired
    private FaqDAOInter faqDAO;

    @Override
    public int create(FaqVO faqVO) {
        return faqDAO.create(faqVO);
    }

    @Override
    public List<FaqVO> list() {
        return faqDAO.list();
    }

    @Override
    public List<FaqVO> list_by_cate(String cate) {
        return faqDAO.list_by_cate(cate);
    }

    @Override
    public FaqVO read(int faq_id) {
        return faqDAO.read(faq_id);
    }

    @Override
    public int update(FaqVO faqVO) {
        return faqDAO.update(faqVO);
    }

    @Override
    public int delete(int faq_id) {
        return faqDAO.delete(faq_id);
    }
    
    @Override
    public List<FaqVO> search(String search) {
        return faqDAO.search(search);
    }

}
