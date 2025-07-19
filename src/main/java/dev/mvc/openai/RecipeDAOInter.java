package dev.mvc.openai;

import java.util.List;

public interface RecipeDAOInter {
    /**
     * 레시피 저장
     * @param vo RecipeVO
     * @return 저장된 행 개수
     */
    public int create(RecipeVO vo);

    /**
     * 특정 회원의 레시피 목록
     */
    public List<RecipeVO> list_by_member(int memberno);

    /**
     * 전체 레시피 목록 (관리자용)
     */
    public List<RecipeVO> list_all();
    
    
}
