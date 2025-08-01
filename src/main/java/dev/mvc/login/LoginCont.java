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

    // AES 암호화 처리를 위한 보안 컴포넌트
    @Autowired
    private Security security;

    // ======================= 로그인 내역 조회 =======================
    /**
     * 로그인 이력 목록 조회
     * @param session 현재 로그인한 세션 정보
     * @param model 화면에 전달할 모델 객체
     * @return 로그인 이력 목록 화면
     */
    @GetMapping("/mylist")
    public String myList(HttpSession session, Model model) {
        String id = (String) session.getAttribute("id");

        if (id == null) {
            return "redirect:/member/login"; // 로그인되지 않은 경우 로그인 페이지로 이동
        }

        List<LoginVO> list = loginProc.mylist(id); // 해당 id의 로그인 이력 조회
        model.addAttribute("list", list);

        return "login/mylist"; // login/mylist.html 렌더링
    }

    // ======================= 로그인 이력 삭제 =======================
    /**
     * 로그인 이력 삭제 확인 폼
     */
    @GetMapping("/delete")
    public String deleteForm(@RequestParam("loginno") int loginno,
                             HttpSession session,
                             Model model) {
        String id = (String) session.getAttribute("id");
        if (id == null) {
            return "redirect:/member/login"; // 로그인 필요
        }

        LoginVO log = loginProc.read(loginno); // 삭제 대상 로그인 이력 조회
        model.addAttribute("loginVO", log);
        return "login/delete"; // login/delete.html 렌더링
    }

    /**
     * 로그인 이력 삭제 처리
     */
    @PostMapping("/delete_proc")
    public String deleteProc(@RequestParam("loginno") int loginno,
                             HttpSession session) {
        String id = (String) session.getAttribute("id");
        if (id == null) {
            return "redirect:/member/login"; // 로그인 필요
        }

        loginProc.delete(loginno); // 이력 삭제
        return "redirect:/login/mylist"; // 목록으로 리다이렉트
    }

    // ======================= 로그인 처리 =======================
    /**
     * 로그인 처리
     * 비밀번호 암호화 후 인증, 세션 정보 설정
     */
    @PostMapping("/login_proc")
    public String loginProc(@RequestParam("id") String id,
                            @RequestParam("passwd") String passwd,
                            HttpSession session) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);

        // 비밀번호 암호화 후 map에 저장
        String encryptedPasswd = security.aesEncode(passwd);
        map.put("passwd", encryptedPasswd);

        MemberVO memberVO = memberProc.login(map); // 로그인 검증

        if (memberVO != null) {
            // 로그인 성공 시 세션 정보 저장
            session.setAttribute("id", id);
            session.setAttribute("memberno", memberVO.getMemberno());
            String gradeStr = convertGradeToString(memberVO.getGrade());
            session.setAttribute("grade", gradeStr);

            return "redirect:/"; // 메인 페이지로 이동
        } else {
            // 로그인 실패
            return "member/login_fail";
        }
    }

    /**
     * 등급 숫자를 문자열로 변환하는 유틸 메서드
     * @param grade 정수 등급 (1~59)
     * @return 등급에 해당하는 문자열 ("admin", "supplier", "user", "withdrawn")
     */
    private String convertGradeToString(int grade) {
        if (grade >= 1 && grade <= 4) return "admin";
        else if (grade >= 5 && grade <= 15) return "supplier";
        else if (grade >= 16 && grade <= 39) return "user";
        else return "withdrawn";
    }
}
