package dev.mvc.openai;

import java.util.List;

public interface RecipeProcInter {
    int create(RecipeVO vo);
    List<RecipeVO> list_by_member(int memberno);
    List<RecipeVO> list_all();
}
