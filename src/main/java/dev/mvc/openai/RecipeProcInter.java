package dev.mvc.openai;

import java.util.List;

public interface RecipeProcInter {
  public int create(RecipeVO recipeVO);
  
  public List<RecipeVO> list_by_member(int memberno);
}
