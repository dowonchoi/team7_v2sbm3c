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

    // 🔎 삭제 전 알림 조회
    NotificationVO noti = notificationProc.read(notification_id); // ← 해당 알림 가져오기

    // 🔐 본인 알림인지 확인 로직 추가 가능 (memberno 비교)

    // 삭제 수행
    notificationProc.delete(notification_id);

    // 세션에서 등급 확인
    String grade = (String) session.getAttribute("grade");

    // ✅ 알림 타입에 따라 분기
    if ("qna".equals(noti.getType())) {
      if ("admin".equals(grade)) {
        return "redirect:/notice/list"; // 관리자: 전체 Q&A
      } else if ("user".equals(grade)) {
        return "redirect:/qna/list_user"; // 소비자: 소비자 Q&A
      } else if ("supplier".equals(grade)) {
        return "redirect:/qna/list_supplier"; // 공급자: 공급자 Q&A
      } else {
        return "redirect:/member/login"; // 예외 처리
      }
    } else {
      if ("admin".equals(grade)) {
        return "redirect:/inquiry/list_all"; // 관리자: 전체 문의
      } else {
        return "redirect:/inquiry/list_by_member"; // 소비자/공급자: 본인 문의
      }
    }
  }

}
