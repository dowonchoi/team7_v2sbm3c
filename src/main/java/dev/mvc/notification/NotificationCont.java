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
  // Notification 처리용 비즈니스 로직 객체 (알림 생성/조회/삭제)

  // ===============================================================
  // [알림 개수 조회]
  // - 헤더나 공통 UI에서 읽지 않은 알림의 수를 표시할 때 사용
  // ===============================================================
  @GetMapping("/count")
  @ResponseBody
  public int countUnread(HttpSession session) {
    Integer membernoObj = (Integer) session.getAttribute("memberno");
    if (membernoObj == null) {
      // 로그인 안 된 경우 알림 없음
      return 0;
    }
    int memberno = membernoObj;
    return notificationProc.countUnreadNotifications(memberno); 
    // DB에서 해당 회원의 읽지 않은 알림 수 반환
  }

  // ===============================================================
  // [알림 목록 페이지]
  // - 알림 전체를 보여주는 페이지
  // ===============================================================
  @GetMapping("/list")
  public String list(Model model, HttpSession session) {
    Integer membernoObj = (Integer) session.getAttribute("memberno");
    if (membernoObj == null) {
      // 로그인 필요
      return "redirect:/member/login";
    }
    int memberno = membernoObj;
    List<NotificationVO> list = notificationProc.selectNotificationsByMemberId(memberno);
    // 회원별 알림 목록 조회
    model.addAttribute("list", list);
    return "notification/list"; 
    // templates/notification/list.html
  }

  // ===============================================================
  // [알림 팝업 목록]
  // - 알림 아이콘 클릭 시 작은 팝업 UI로 알림 목록 표시
  // ===============================================================
  @GetMapping("/list_popup")
  public String listPopup(Model model, HttpSession session) {
    Integer membernoObj = (Integer) session.getAttribute("memberno");
    if (membernoObj == null) {
      // 로그인 안 된 경우 빈 팝업
      return "notification/popup_list_empty";
    }
    int memberno = membernoObj;
    List<NotificationVO> list = notificationProc.selectNotificationsByMemberId(memberno);
    model.addAttribute("list", list);
    return "notification/popup_list"; 
    // templates/notification/popup_list.html
  }

  // ===============================================================
  // [알림 읽음 처리]
  // - 특정 알림을 읽음 상태로 업데이트
  // - AJAX 호출 시 success/fail 반환
  // ===============================================================
  @PostMapping("/read")
  @ResponseBody
  public String read(@RequestParam("notification_id") int notification_id) {
    int cnt = notificationProc.markNotificationAsRead(notification_id); 
    // 알림 상태를 '읽음'으로 변경
    return cnt == 1 ? "success" : "fail";
  }

  // ===============================================================
  // [알림 클릭 후 리디렉션]
  // - 알림 클릭 시 읽음 처리 후 지정된 URL로 이동
  // ===============================================================
  @GetMapping("/read_and_redirect")
  public String readAndRedirect(@RequestParam("notification_id") int notification_id, 
                                 @RequestParam("url") String url) {
    notificationProc.markNotificationAsRead(notification_id);
    return "redirect:" + url; 
    // 알림에 설정된 URL로 이동
  }

  // ===============================================================
  // [알림 생성 공통 메서드]
  // - 다른 컨트롤러에서 공통적으로 호출하여 알림을 생성할 수 있음
  // - VO 객체를 생성 후 DB에 insert
  // ===============================================================
  public void createNotification(int targetMemberno, String message, String url) {
    NotificationVO noti = new NotificationVO();
    noti.setMemberno(targetMemberno); // 알림 대상 회원 번호
    noti.setMessage(message);         // 알림 메시지
    noti.setUrl(url);                 // 클릭 시 이동할 URL
    notificationProc.create(noti);    // 알림 DB 저장
  }

  // ===============================================================
  // [알림 삭제]
  // - 특정 알림을 삭제하고, 알림의 타입(type)에 따라 리다이렉트 경로를 달리함
  // ===============================================================
  @PostMapping("/delete")
  public String delete(@RequestParam("notification_id") int notification_id, HttpSession session) {
    Integer memberno = (Integer) session.getAttribute("memberno");
    if (memberno == null) {
      return "redirect:/member/login";
    }

    // 삭제할 알림 조회
    NotificationVO notificationVO = notificationProc.read(notification_id);

    // 실제 DB에서 알림 삭제
    notificationProc.delete(notification_id);

    Integer grade = (Integer) session.getAttribute("grade");
    if (grade == null) {
      return "redirect:/member/login";
    }

    // 알림 유형(type)에 따라 리다이렉트 페이지 분기
    if ("qna".equals(notificationVO.getType())) {
      // QnA 관련 알림
      if (grade >= 1 && grade <= 4) {
        return "redirect:/notice/list";         // 관리자: 공지/알림 리스트로 이동
      } else if (grade >= 16 && grade <= 39) {
        return "redirect:/qna/list_user";       // 소비자 QnA 목록
      } else if (grade >= 5 && grade <= 15) {
        return "redirect:/qna/list_supplier";   // 공급자 QnA 목록
      }
    } else {
      // Inquiry(1:1 문의) 관련 알림
      if (grade >= 1 && grade <= 4) {
        return "redirect:/inquiry/list_all";    // 관리자용 전체 문의 목록
      } else {
        return "redirect:/inquiry/list_by_member"; // 소비자/공급자 문의 목록
      }
    }

    // 예외 케이스 (기본 리다이렉트)
    return "redirect:/member/login";
  }

}
