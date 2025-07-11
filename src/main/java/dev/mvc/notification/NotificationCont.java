package dev.mvc.notification;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RequestMapping("/notification")
@Controller
public class NotificationCont {

  @Autowired
  @Qualifier("dev.mvc.notification.NotificationProc")
  private NotificationProcInter notificationProc;

  /** 알림 개수 조회 (헤더용) */
  @GetMapping("/count")
  @ResponseBody
  public int countUnread(HttpSession session) {
    Integer membernoObj = (Integer) session.getAttribute("memberno");
    if (membernoObj == null) {
      return 0;  // 로그인 안 했으면 알림 개수 0으로
    }
    int memberno = membernoObj;
    // 1. 로그인한 사용자 번호 출력 (popup_list)
    System.out.println("현재 로그인한 memberno: " + session.getAttribute("memberno"));

    return notificationProc.countUnreadNotifications(memberno);
  }

  /** 알림 목록 페이지 */
  @GetMapping("/list")
  public String list(Model model, HttpSession session) {
    Integer membernoObj = (Integer) session.getAttribute("memberno");
    if (membernoObj == null) {
      return "redirect:/member/login";  // 로그인 안 했으면 로그인 페이지로
    }
    int memberno = membernoObj;
    List<NotificationVO> list = notificationProc.selectNotificationsByMemberId(memberno);
    model.addAttribute("list", list);
    return "/notification/list";
  }
  
  @GetMapping("/list_popup")
  public String listPopup(Model model, HttpSession session) {
    Integer membernoObj = (Integer) session.getAttribute("memberno");
    System.out.println("팝업 알림 조회 - 로그인한 memberno: " + membernoObj); // ✅ 꼭 확인
    if (membernoObj == null) {
        return "notification/popup_list_empty";  // 로그인 안 했을 때
    }
    int memberno = membernoObj;
    List<NotificationVO> list = notificationProc.selectNotificationsByMemberId(memberno);
    model.addAttribute("list", list);
    return "notification/popup_list";  // ✅ popup_list.html로 이동
  }

  /** 알림 읽음 처리 */
  @PostMapping("/read")
  @ResponseBody
  public String read(@RequestParam("notification_id") int notification_id) {
    int cnt = notificationProc.markNotificationAsRead(notification_id);
    return cnt == 1 ? "success" : "fail";
  }
  
  @GetMapping("/read_and_redirect")
  public String readAndRedirect(@RequestParam("notification_id") int notification_id,
                                @RequestParam("url") String url) {
      notificationProc.markNotificationAsRead(notification_id);
      return "redirect:" + url;
  }
}
