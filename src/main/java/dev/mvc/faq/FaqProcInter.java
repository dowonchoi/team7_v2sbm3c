package dev.mvc.faq;

import java.util.List;

public interface FaqProcInter {
  
    public int create(FaqVO faqVO);
    
    public List<FaqVO> list();
    
    public List<FaqVO> list_by_cate(String cate);
    
    public FaqVO read(int faq_id);
    
    public int update(FaqVO faqVO);
    
    public int delete(int faq_id);
    
    public List<FaqVO> search(String search);

}
