package dev.mvc.member;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 이메일 관련 서비스를 처리하는 클래스
 */
@Service
public class MailService {

  //Spring에서 제공하는 메일 발송 객체 자동 주입
  @Autowired
  private final JavaMailSender mailSender;

  //application.properties에 등록된 발신자 이메일 정보
  @Value("${spring.mail.username}")
  private String fromEmail;

  //생성자 방식 주입
  public MailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  /**
   * 이메일 형식 검증 메서드
   */
  private boolean isValidEmail(String email) {
      return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
  }

  /**
   * 일반 텍스트/HTML 메일 전송
   * @param to 수신자 이메일
   * @param subject 제목
   * @param content 본문 (HTML 가능)
   * @return 전송 성공 여부
   */
  public boolean sendMail(String to, String subject, String content) {
      if (!isValidEmail(to)) {
          System.out.println("❌ 이메일 주소가 유효하지 않습니다: " + to);
          return false;
      }

      try {
          MimeMessage message = mailSender.createMimeMessage();
          MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

          helper.setTo(to);
          helper.setSubject(subject);
          helper.setText(content, true);
          helper.setFrom(new InternetAddress(fromEmail, "tteoliMall", "UTF-8"));

          mailSender.send(message);
          return true;
      } catch (Exception e) {
          e.printStackTrace();
          return false;
      }
    } 

  /**
   * 첨부파일 포함 메일 전송
   * @param to 수신자
   * @param subject 제목
   * @param content 본문
   * @param files 첨부파일 배열
   * @param savePath 저장 경로 (임시)
   */
  public void sendMailWithAttachment(String to, String subject, String content, MultipartFile[] files, String savePath) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(content, true);
      helper.setFrom(new InternetAddress(fromEmail, "tteoliMall", "UTF-8")); // ✅ 수정된 부분

      for (MultipartFile file : files) {
        if (!file.isEmpty()) {
          File savedFile = new File(savePath + file.getOriginalFilename());
          file.transferTo(savedFile);
          helper.addAttachment(file.getOriginalFilename(), savedFile);
        }
      }

      mailSender.send(message);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 비밀번호 재설정 메일 전송 (템플릿 사용)
   * @param toEmail 수신 이메일
   * @param resetLink 재설정 링크
   * @return 성공 여부
   */
  public boolean sendResetPasswordMail(String toEmail, String resetLink) {
    if (!isValidEmail(toEmail)) {
        System.out.println("❌ 이메일 형식 오류: " + toEmail);
        return false;
    }

    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(new InternetAddress(fromEmail, "tteoliMall", "UTF-8"));
        helper.setTo(toEmail);
        helper.setSubject("[tteoliMall] 비밀번호 재설정 링크입니다.");

        String html = Files.readString(new ClassPathResource("templates/reset_password_template.html")
                .getFile().toPath(), StandardCharsets.UTF_8);
        html = html.replace("${resetLink}", resetLink);

        helper.setText(html, true);

        mailSender.send(message);
        return true;
    } catch (IOException | jakarta.mail.MessagingException e) {
        e.printStackTrace();
        return false;
    }
  }
  
  /**
   * 이메일 인증 코드 메일 전송
   * @param email 수신자
   * @param code 인증 코드
   * @return 전송 성공 여부
   */
  public boolean sendVerificationMail(String email, String code) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(email);
      helper.setSubject("[tteoliMall] 이메일 인증 코드입니다");
      helper.setText("<p>인증 코드는 <b>" + code + "</b> 입니다.<br>1분 안에 입력해주세요.</p>", true);
      helper.setFrom(new InternetAddress(fromEmail, "tteoliMall", "UTF-8"));

      mailSender.send(message);
      return true;
    } catch (Exception e) {
      e.printStackTrace();  // 콘솔에 오류 출력
      return false;
    }
  }

}
