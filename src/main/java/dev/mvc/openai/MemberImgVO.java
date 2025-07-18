package dev.mvc.openai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberImgVO {
    private int member_imgno;
    private int memberno;
    private String prompt;
    private String filename;
    private String rdate;
}
