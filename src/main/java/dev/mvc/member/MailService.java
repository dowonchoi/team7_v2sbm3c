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

@Service
public class MailService {

  @Autowired
  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String fromEmail;

  public MailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  /** 이메일 형식 체크 메서드 */
  private boolean isValidEmail(String email) {
      return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
  }

  /** 일반 메일 전송 */
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

  /** ✔️ 비밀번호 재설정 링크 메일 전송 */
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
