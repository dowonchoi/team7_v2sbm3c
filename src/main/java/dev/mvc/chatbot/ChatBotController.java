package dev.mvc.chatbot;

import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/chatbot")
public class ChatBotController {

  @PostMapping
  public String chatbotResponse(@RequestBody ChatRequest request, HttpSession session) {
      String userMessage = request.getMessage();
      String state = (String) session.getAttribute("chatState");

      if ("주문 조회".equals(userMessage)) {
          session.setAttribute("chatState", "awaiting_order_number");
          return "주문 번호를 입력해주세요.";
      } else if ("배송 조회".equals(userMessage)) {
          session.setAttribute("chatState", "awaiting_tracking_number");
          return "운송장 번호를 입력해주세요.";
      } else if (state != null) {
          if ("awaiting_order_number".equals(state)) {
              session.removeAttribute("chatState");
              return "입력하신 주문번호 [" + userMessage + "]의 내역입니다.";
          } else if ("awaiting_tracking_number".equals(state)) {
              session.removeAttribute("chatState");
              return "운송장 번호 [" + userMessage + "]의 배송 현황입니다.";
          }
      }

      // 기본 처리
      return switch (userMessage) {
          case "FAQ" -> "자주 묻는 질문입니다.";
          case "상담 연결" -> "상담사 연결을 준비 중입니다.";
          default -> "죄송합니다, 이해하지 못했습니다.";
      };
  }
  
  //✅ 반드시 아래 클래스 포함 (네 오류 해결 핵심)
  static class ChatRequest {
      private String message;

      public String getMessage() {
          return message;
      }

      public void setMessage(String message) {
          this.message = message;
      }
  }
}
