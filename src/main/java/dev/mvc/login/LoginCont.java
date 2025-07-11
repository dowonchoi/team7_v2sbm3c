package dev.mvc.login;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import dev.mvc.member.MemberProcInter;
import dev.mvc.member.MemberVO;
import dev.mvc.tool.Security;
import jakarta.servlet.http.HttpSession;

@RequestMapping("/login")
@Controller
public class LoginCont {

    @Autowired
    @Qualifier("dev.mvc.login.LoginProc")
    private LoginProcInter loginProc;
    
    @Autowired
    @Qualifier("dev.mvc.member.MemberProc")
    private MemberProcInter memberProc;
    
    // 필드 추가
    @Autowired
    private Security security;
    
    // 로그인 내역 목록
    @GetMapping("/mylist")
    public String myList(HttpSession session, Model model) {
        String id = (String) session.getAttribute("id");

        if (id == null) {
            return "redirect:/member/login";
        }

        List<LoginVO> list = loginProc.mylist(id);
        model.addAttribute("list", list);

        return "/login/mylist";
    }

    // 삭제 확인 화면
    @GetMapping("/delete")
    public String deleteForm(@RequestParam("loginno") int loginno, 
                             HttpSession session,
                             Model model) {
        String id = (String) session.getAttribute("id");
        if (id == null) {
            return "redirect:/member/login";
        }

        LoginVO log = loginProc.read(loginno);
        model.addAttribute("loginVO", log);
        return "login/delete";
    }

    // 삭제 처리
    @PostMapping("/delete_proc")
    public String deleteProc(@RequestParam("loginno") int loginno, 
                             HttpSession session) {
        String id = (String) session.getAttribute("id");
        if (id == null) {
            return "redirect:/member/login";
        }

        loginProc.delete(loginno);
        return "redirect:/login/mylist";
    }
    
    // 카트 처리하며 로그인 처리 메서드 추가
    @PostMapping("/login_proc")
    public String loginProc(@RequestParam("id") String id,
                            @RequestParam("passwd") String passwd,
                            HttpSession session) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        
        // ✅ 비밀번호 암호화 추가 (중요)
        String encryptedPasswd = security.aesEncode(passwd);
        map.put("passwd", encryptedPasswd);
        
        MemberVO memberVO = memberProc.login(map); // 로그인 인증
        
        if (memberVO != null) {
            session.setAttribute("id", id);
            session.setAttribute("memberno", memberVO.getMemberno());  // 이제 정상 저장됨
            String gradeStr = convertGradeToString(memberVO.getGrade());
            session.setAttribute("grade", gradeStr);
            return "redirect:/";
        } else {
            return "/member/login_fail";
        }
    }
    
    // 카트 처리하며 로그인 처리 메서드 추가2 
    private String convertGradeToString(int grade) {
      if (grade >= 1 && grade <= 4) return "admin";
      else if (grade >= 5 && grade <= 15) return "supplier";
      else if (grade >= 16 && grade <= 39) return "user";
      else return "withdrawn";
  }

}
