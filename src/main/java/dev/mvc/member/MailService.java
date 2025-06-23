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

  public void sendMail(String to, String subject, String content) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(content, true);
      helper.setFrom(new InternetAddress(fromEmail, "TteoliMall", "UTF-8")); // ✅ 수정된 부분
      mailSender.send(message);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void sendMailWithAttachment(String to, String subject, String content, MultipartFile[] files, String savePath) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(content, true);
      helper.setFrom(new InternetAddress(fromEmail, "TteoliMall", "UTF-8")); // ✅ 수정된 부분

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

  public void sendResetPasswordMail(String toEmail, String resetLink) throws MessagingException, IOException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    helper.setFrom(new InternetAddress(fromEmail, "TteoliMall", "UTF-8")); // ✅ 수정된 부분
    helper.setTo(toEmail);
    helper.setSubject("【TteoliMall】 비밀번호 재설정 링크입니다.");

    // HTML 템플릿 로딩
    String html = Files.readString(new ClassPathResource("templates/reset_password_template.html").getFile().toPath(), StandardCharsets.UTF_8);
    html = html.replace("${resetLink}", resetLink);

    helper.setText(html, true);

    mailSender.send(message);
  }
  
  public boolean sendVerificationMail(String email, String code) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(email);
      helper.setSubject("[TteoliMall] 이메일 인증 코드입니다");
      helper.setText("<p>인증 코드는 <b>" + code + "</b> 입니다.<br>3분 안에 입력해주세요.</p>", true);
      helper.setFrom(new InternetAddress(fromEmail, "TteoliMall", "UTF-8"));

      mailSender.send(message);
      return true;
    } catch (Exception e) {
      e.printStackTrace();  // 콘솔에 오류 출력
      return false;
    }
  }

}
