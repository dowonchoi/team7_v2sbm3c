package dev.mvc.openai;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeVO {
    private int recipeno;
    private int memberno;
    private String foodBinary; 
    private String content;
    private String rdate;
}
