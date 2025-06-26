package dev.mvc.login;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@RequestMapping("/login")
@Controller
public class LoginCont {

    @Autowired
    @Qualifier("dev.mvc.login.LoginProc")
    private LoginProcInter loginProc;

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
}
