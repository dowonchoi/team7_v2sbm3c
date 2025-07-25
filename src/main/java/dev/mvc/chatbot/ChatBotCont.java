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
    // ✅ DB 연동용 DAO 인터페이스 (주문 정보, 결제수단, FAQ 데이터 조회 등)

    /**
     * [챗봇 응답 처리 메서드]
     * - 사용자의 입력 메시지를 받아 챗봇 응답을 결정.
     * - 세션 상태(chatState)를 기반으로 대화 흐름(주문조회 → 주문확정 등)을 관리.
     * - FAQ 조회, 결제 수단 조회 등 다양한 기능을 수행.
     *
     * @param request 사용자가 보낸 메시지를 담는 객체 (JSON → ChatRequest)
     * @param session 사용자 세션 (로그인 여부 및 대화 상태 저장)
     * @return 챗봇의 응답 문자열
     */
    @PostMapping
    public String chatbotResponse(@RequestBody ChatRequest request, HttpSession session) {
        // ✅ 로그인 여부 확인: 비로그인 상태면 챗봇 사용 제한
        String id = (String) session.getAttribute("id");
        if (id == null) {
            return "챗봇 기능은 로그인 후 이용 가능합니다. 먼저 로그인해주세요.";
        }
      
        // 사용자가 입력한 메시지
        String userMessage = request.getMessage();
        // 현재 챗봇 대화 상태 (주문조회 진행 중인지, FAQ 선택 중인지 등)
        String state = (String) session.getAttribute("chatState");

        // --- 주요 명령어 분기 처리 ---
        if ("주문 조회".equals(userMessage)) {
            // 주문 조회 요청 시 → 주문번호 입력 상태로 전환
            session.setAttribute("chatState", "awaiting_orderno");
            return "주문 번호를 입력해주세요.";

        } else if ("결제 수단".equals(userMessage)) {
            // 결제 수단 조회
            List<String> methods = chatBotDAO.getAllPaymentMethods(); // DB에서 결제수단 코드 조회
            List<String> readableMethods = methods.stream()
                .map(this::convertPaymentName) // 코드 → 한글명 변환
                .toList();

            return "사용 가능한 결제 수단:\n" + String.join(", ", readableMethods);

        } else if ("FAQ".equals(userMessage)) {
            // FAQ 목록 출력 (질문 리스트 제공)
            List<String> questions = chatBotDAO.getAllFaqQuestions();
            session.setAttribute("chatState", "awaiting_faq_selection"); // FAQ 질문 선택 대기 상태
            return "궁금한 질문을 선택해주세요:\n" + String.join("\n", questions);

        } else if ("상담 연결".equals(userMessage)) {
            // 상담사 연결 요청
            return "상담사 연결을 준비 중입니다. 잠시만 기다려주세요.";

        } else if (state != null) {
            // --- 대화 진행 중 (상태 기반 처리) ---
            switch (state) {

                // 1. 주문번호 입력 대기 상태
                case "awaiting_orderno" -> {
                    String orderDetails = chatBotDAO.getOrderDetails(userMessage); // 입력한 주문번호로 조회
                    if (orderDetails != null) {
                        // 주문번호 유효 → 주문확정 단계로 이동
                        session.setAttribute("chatState", "awaiting_order_confirmation");
                        session.setAttribute("orderno", userMessage);
                        return "주문번호 [" + userMessage + "]의 주문 내역:\n" + orderDetails
                                + "\n주문을 확정하시겠습니까? (네/아니요)";
                    } else {
                        // 잘못된 주문번호
                        return "해당 주문번호를 찾을 수 없습니다.";
                    }
                }

                // 2. 주문 확정 여부 대기 상태
                case "awaiting_order_confirmation" -> {
                    String orderNumber = (String) session.getAttribute("orderno");
                    if ("네".equalsIgnoreCase(userMessage)) {
                        // 주문 확정
                        session.removeAttribute("chatState");
                        session.removeAttribute("orderno");
                        return "주문 [" + orderNumber + "]이 확정되었습니다. 감사합니다!";
                    } else if ("아니요".equalsIgnoreCase(userMessage)) {
                        // 주문 취소
                        session.removeAttribute("chatState");
                        session.removeAttribute("orderno");
                        return "주문을 취소했습니다.";
                    } else {
                        return "네 또는 아니요로 답변해주세요.";
                    }
                }

                // 3. FAQ 질문 선택 대기 상태
                case "awaiting_faq_selection" -> {
                    String answer = chatBotDAO.getFaqAnswer(userMessage); // 해당 질문에 대한 답변 조회
                    if (answer != null) {
                        // 답변 존재 시 FAQ 선택 상태 종료
                        session.removeAttribute("chatState");
                        return "답변:\n" + answer;
                    } else {
                        return "해당 질문을 찾을 수 없습니다. 정확히 입력해주세요.";
                    }
                }
            }
        }

        // 기본 응답 (매칭되는 명령어가 없을 때)
        return "죄송합니다, 이해하지 못했습니다.";
    }

    // ====================== 헬퍼 메서드 ======================

    /**
     * 결제 수단 코드 → 한글명 변환
     * @param paymentCode DB에서 가져온 결제 수단 코드 (CARD, BANK 등)
     * @return 사용자 친화적 결제 수단 명칭
     */
    private String convertPaymentName(String paymentCode) {
        return switch (paymentCode) {
            case "CARD" -> "신용/체크 카드";
            case "BANK" -> "무통장 입금";
            case "KAKAO" -> "카카오페이";
            default -> paymentCode; // 매칭되는 코드가 없으면 그대로 반환
        };
    }

    // ====================== 내부 DTO 클래스 ======================

    /**
     * [챗봇 메시지 요청 객체]
     * - JSON 요청을 받을 때 매핑되는 DTO
     * - 프론트엔드에서 { "message": "텍스트" } 형태로 전달
     */
    static class ChatRequest {
        private String message;
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
