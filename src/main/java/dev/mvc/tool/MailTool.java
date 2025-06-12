package dev.mvc.tool;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.web.multipart.MultipartFile;

public class MailTool {
    /**
     * 텍스트 메일 전송
     * @param receiver 메일 받을 이메일 주소
     * @param from 보내는 사람 이메일 주소
     * @param title 제목
     * @param content 전송 내용
     */
    public void send(String receiver, String from, String title, String content) {
      Properties props = new Properties();
      props.put("mail.smtp.host", "smtp.gmail.com");
      props.put("mail.smtp.port", "587");
      props.put("mail.smtp.auth", "true");
      props.put("mail.smtp.starttls.enable", "true");
      props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
      
      // 3. SMTP 서버정보와 사용자 정보를 기반으로 Session 클래스의 인스턴스 생성
      Session session = Session.getInstance(props, new javax.mail.Authenticator() {
          protected PasswordAuthentication getPasswordAuthentication() {
              String user="nayung030703@gmail.com";
              String password="nfpu zpkq huph ekug";
              return new PasswordAuthentication(user, password);
          }
      });
    
      Message message = new MimeMessage(session);
      try {
          message.setFrom(new InternetAddress(from));
          message.addRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
          message.setSubject(title);
          message.setContent(content, "text/html; charset=utf-8");

          Transport.send(message);
      } catch (Exception e) {
          e.printStackTrace();
      }    
  }
 
    /**
     * 파일 첨부 메일 전송
     * @param receiver 메일 받을 이메일 주소
     * @param title 제목
     * @param content 전송 내용
     * @param file1MF 전송하려는 파일 목록
     * @param path 서버상에 첨부하려는 파일이 저장되는 폴더
     */
    public void send_file(String receiver, String from, String title, String content,
                                  MultipartFile[] file1MF, String path) {
      Properties props = new Properties();
      props.put("mail.smtp.host", "smtp.gmail.com");
      props.put("mail.smtp.port", "587");
      props.put("mail.smtp.auth", "true");
      props.put("mail.smtp.starttls.enable", "true");
      props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
      
      // 3. SMTP 서버정보와 사용자 정보를 기반으로 Session 클래스의 인스턴스 생성
      Session session = Session.getInstance(props, new javax.mail.Authenticator() {
          protected PasswordAuthentication getPasswordAuthentication() {
              String user="nayung030703@gmail.com";
              String password="nfpu zpkq huph ekug";
              return new PasswordAuthentication(user, password);
          }
      });
    
      Message message = new MimeMessage(session);
      try {
          message.setFrom(new InternetAddress(from));
          message.addRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
          message.setSubject(title);
          
          MimeBodyPart mbp1 = new MimeBodyPart();
          mbp1.setContent(content, "text/html; charset=utf-8"); // 메일 내용
          
          Multipart mp = new MimeMultipart();
          mp.addBodyPart(mbp1);

          // 첨부 파일 처리
          // ---------------------------------------------------------------------------------------
          for (MultipartFile item:file1MF) {
              if (item.getSize() > 0) {
                  MimeBodyPart mbp2 = new MimeBodyPart();
                  
                  String fname=path+item.getOriginalFilename();
                  System.out.println("-> file name: " + fname); 
                  
                  FileDataSource fds = new FileDataSource(fname);
                  
                  mbp2.setDataHandler(new DataHandler(fds));
                  mbp2.setFileName(fds.getName());
                  
                  mp.addBodyPart(mbp2);
              }
          }
          // ---------------------------------------------------------------------------------------
          
          message.setContent(mp);
          
          Transport.send(message);
          
      } catch (Exception e) {
          e.printStackTrace();
      }    
  }
  
}