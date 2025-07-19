package dev.mvc.openai;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("dev.mvc.openai.RecipeProc")
public class RecipeProc implements RecipeProcInter {

    @Autowired
    private RecipeDAOInter recipeDAO;

    @Override
    public int create(RecipeVO vo) {
        return recipeDAO.create(vo);
    }

    @Override
    public List<RecipeVO> list_by_member(int memberno) {
        return recipeDAO.list_by_member(memberno);
    }

    @Override
    public List<RecipeVO> list_all() {
        return recipeDAO.list_all();
    }
}
