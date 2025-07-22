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
      return 0;
    }
    int memberno = membernoObj;
    return notificationProc.countUnreadNotifications(memberno);
  }

  /** 알림 목록 페이지 */
  @GetMapping("/list")
  public String list(Model model, HttpSession session) {
    Integer membernoObj = (Integer) session.getAttribute("memberno");
    if (membernoObj == null) {
      return "redirect:/member/login";
    }
    int memberno = membernoObj;
    List<NotificationVO> list = notificationProc.selectNotificationsByMemberId(memberno);
    model.addAttribute("list", list);
    return "/notification/list";
  }

  /** 알림 팝업 목록 */
  @GetMapping("/list_popup")
  public String listPopup(Model model, HttpSession session) {
    Integer membernoObj = (Integer) session.getAttribute("memberno");
    if (membernoObj == null) {
      return "notification/popup_list_empty";
    }
    int memberno = membernoObj;
    List<NotificationVO> list = notificationProc.selectNotificationsByMemberId(memberno);
    model.addAttribute("list", list);
    return "notification/popup_list";
  }

  /** 알림 읽음 처리 */
  @PostMapping("/read")
  @ResponseBody
  public String read(@RequestParam("notification_id") int notification_id) {
    int cnt = notificationProc.markNotificationAsRead(notification_id);
    return cnt == 1 ? "success" : "fail";
  }

  /** 알림 클릭 후 리디렉션 */
  @GetMapping("/read_and_redirect")
  public String readAndRedirect(@RequestParam("notification_id") int notification_id, @RequestParam("url") String url) {
    notificationProc.markNotificationAsRead(notification_id);
    return "redirect:" + url;
  }

  // ✅ 알림 생성용 공통 메서드 (컨트롤러에서 직접 호출 가능)
  public void createNotification(int targetMemberno, String message, String url) {
    NotificationVO noti = new NotificationVO();
    noti.setMemberno(targetMemberno);
    noti.setMessage(message);
    noti.setUrl(url);
    notificationProc.create(noti);
  }

  /** 알림 삭제 */
  @PostMapping("/delete")
  public String delete(@RequestParam("notification_id") int notification_id, HttpSession session) {
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return "redirect:/member/login";
    }

    NotificationVO notificationVO = notificationProc.read(notification_id);

    // 알림 삭제
    notificationProc.delete(notification_id);

    Integer grade = (Integer) session.getAttribute("grade");

    if (grade == null) {
      return "redirect:/member/login";
    }

    // 알림 유형에 따라 리다이렉트
    if ("qna".equals(notificationVO.getType())) {
      if (grade >= 1 && grade <= 4) {
        return "redirect:/notice/list"; // 관리자
      } else if (grade >= 16 && grade <= 39) {
        return "redirect:/qna/list_user"; // 소비자
      } else if (grade >= 5 && grade <= 15) {
        return "redirect:/qna/list_supplier"; // 공급자
      }
    } else {
      if (grade >= 1 && grade <= 4) {
        return "redirect:/inquiry/list_all"; // 관리자
      } else {
        return "redirect:/inquiry/list_by_member"; // 소비자/공급자
      }
    }

    return "redirect:/member/login";
  }

}
