package dev.mvc.member;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {

  @Autowired
  private JavaMailSender mailSender;

  public void sendMail(String to, String subject, String content) {
      try {
          MimeMessage message = mailSender.createMimeMessage();
          MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
          helper.setTo(to);
          helper.setSubject(subject);
          helper.setText(content, true);
          helper.setFrom("nayung030703@gmail.com");
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
          helper.setFrom("nayung030703@gmail.com");

          for (MultipartFile file : files) {
              if (!file.isEmpty()) {
                  File savedFile = new File(savePath + file.getOriginalFilename());
                  file.transferTo(savedFile);  // 임시로 저장해야 첨부 가능
                  helper.addAttachment(file.getOriginalFilename(), savedFile);
              }
          }

          mailSender.send(message);
      } catch (Exception e) {
          e.printStackTrace();
      }
  }
}
