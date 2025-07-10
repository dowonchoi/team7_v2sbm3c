package dev.mvc.chatbot;

import java.util.List;

public interface ChatBotDAOInter {

  // ✅ 주문 조회 (주문번호로 주문 내역 조회)
  public String getOrderDetails(String orderno);

  // ✅ 결제 수단 조회 (모든 결제 수단)
  public List<String> getAllPaymentMethods();

  public List<String> getAllFaqQuestions();  // ✅ 추가
  
  public String getFaqAnswer(String question);  // ✅ 추가
}
