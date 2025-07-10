package dev.mvc.chatbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
public class ChatBotCont {

    @Autowired
    private ChatBotDAOInter chatBotDAO;

    @PostMapping
    public String chatbotResponse(@RequestBody ChatRequest request, HttpSession session) {
        String userMessage = request.getMessage();
        String state = (String) session.getAttribute("chatState");

        if ("주문 조회".equals(userMessage)) {
            session.setAttribute("chatState", "awaiting_orderno");
            return "주문 번호를 입력해주세요.";
        } else if ("결제 수단".equals(userMessage)) {
            List<String> methods = chatBotDAO.getAllPaymentMethods();
            List<String> readableMethods = methods.stream()
                .map(this::convertPaymentName)
                .toList();

            return "사용 가능한 결제 수단:\n" + String.join(", ", readableMethods);
        } else if ("FAQ".equals(userMessage)) {
            List<String> questions = chatBotDAO.getAllFaqQuestions();
            session.setAttribute("chatState", "awaiting_faq_selection");
            return "궁금한 질문을 선택해주세요:\n" + String.join("\n", questions);
        } else if ("상담 연결".equals(userMessage)) {
            return "상담사 연결을 준비 중입니다. 잠시만 기다려주세요.";
        } else if (state != null) {
            switch (state) {
                case "awaiting_orderno" -> {
                    String orderDetails = chatBotDAO.getOrderDetails(userMessage);
                    if (orderDetails != null) {
                        session.setAttribute("chatState", "awaiting_order_confirmation");
                        session.setAttribute("orderno", userMessage);
                        return "주문번호 [" + userMessage + "]의 주문 내역:\n" + orderDetails + "\n주문을 확정하시겠습니까? (네/아니요)";
                    } else {
                        return "해당 주문번호를 찾을 수 없습니다.";
                    }
                }
                case "awaiting_order_confirmation" -> {
                    String orderNumber = (String) session.getAttribute("orderno");
                    if ("네".equalsIgnoreCase(userMessage)) {
                        session.removeAttribute("chatState");
                        session.removeAttribute("orderno");
                        return "주문 [" + orderNumber + "]이 확정되었습니다. 감사합니다!";
                    } else if ("아니요".equalsIgnoreCase(userMessage)) {
                        session.removeAttribute("chatState");
                        session.removeAttribute("orderno");
                        return "주문을 취소했습니다.";
                    } else {
                        return "네 또는 아니요로 답변해주세요.";
                    }
                }
                case "awaiting_faq_selection" -> {
                    String answer = chatBotDAO.getFaqAnswer(userMessage);
                    if (answer != null) {
                        session.removeAttribute("chatState");
                        return "답변:\n" + answer;
                    } else {
                        return "해당 질문을 찾을 수 없습니다. 정확히 입력해주세요.";
                    }
                }
            }
        }

        return "죄송합니다, 이해하지 못했습니다.";
    }

    /** ✅ 결제 수단 코드 → 한글 명칭 변환 */
    private String convertPaymentName(String paymentCode) {
        return switch (paymentCode) {
            case "CARD" -> "신용/체크 카드";
            case "BANK" -> "무통장 입금";
            case "KAKAO" -> "카카오페이";
            default -> paymentCode;
        };
    }

    /** ✅ 메시지 요청용 내부 클래스 */
    static class ChatRequest {
        private String message;
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
