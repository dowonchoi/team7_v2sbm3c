package dev.mvc.mms_img;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class MMSImageService {

  /**
   * 옵션형 텍스트 합성 + JPEG 압축 저장
   * <기능 상세>
   * - 원본 이미지에 사용자가 입력한 텍스트를 중앙에 배치
   * - 여러 줄 지원 ("\n" 기준)
   * - 그림자 효과 적용 (4방향)
   * - 최종 이미지를 JPEG 포맷으로 압축 (품질: 70%)
   *
   * @param inputPath   원본 이미지 경로
   * @param messageText 합성할 텍스트 (줄바꿈은 "\n" 사용)
   * @param fontName    폰트명 (예: "Malgun Gothic") → 없으면 기본 폰트 대체
   * @param fontSize    글자 크기 (예: 48)
   * @param textColor   텍스트 색상 (예: "#FFFFFF")
   * @param shadowColor 그림자 색상 (예: "#000000")
   * @return 최종 저장된 이미지 파일명 (경로는 고정)
   * @throws IOException 이미지 파일 로드/저장 시 오류
   */
    public String addTextToImage(String inputPath, String messageText,
                                 String fontName, int fontSize,
                                 String textColor, String shadowColor) throws IOException {
      // (1) 이미지 로드
      // - 입력 경로의 이미지를 BufferedImage로 읽어옴
        File input = new File(inputPath);
        BufferedImage originalImage = ImageIO.read(input);

        // 원본 이미지 크기 확인
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // (2) Graphics2D 객체 생성 (텍스트 합성용)
        Graphics2D g2d = originalImage.createGraphics();
        // 텍스트를 부드럽게 출력하기 위해 안티앨리어싱 적용
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // (3) 폰트 설정 (지정 폰트 없으면 기본 폰트로 대체)
        Font font = loadCustomFont("fonts/NanumGothic.ttf", fontSize);
        try {
            font = new Font(fontName, Font.BOLD, fontSize);
        } catch (Exception e) {
            font = new Font("SansSerif", Font.BOLD, fontSize);
        }
        g2d.setFont(font);

        // (4) 텍스트 색상 및 그림자 색상 설정
        Color mainColor = Color.decode(textColor);
        Color shadow = Color.decode(shadowColor);

        // (5) 텍스트 줄바꿈 처리 + 중앙 정렬 계산
        // - 입력 메시지를 "\n" 기준으로 줄바꿈 처리        
        String[] lines = messageText.split("\n");
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight(); // 한 줄 높이
        int totalTextHeight = lineHeight * lines.length;
        // 세로 중앙 정렬: (전체 높이 - 텍스트 전체 높이) / 2
        int startY = (height - totalTextHeight) / 2 + fm.getAscent();

        // (6) 각 줄별 텍스트 그리기 (그림자 + 본문)
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int textWidth = fm.stringWidth(line);
            int x = (width - textWidth) / 2; // 가로 중앙 정렬
            int y = startY + (i * lineHeight);

            // 그림자 효과 (4방향으로 오프셋)
            g2d.setColor(shadow);
            g2d.drawString(line, x - 2, y - 2);
            g2d.drawString(line, x + 2, y - 2);
            g2d.drawString(line, x - 2, y + 2);
            g2d.drawString(line, x + 2, y + 2);

            // 본문
            g2d.setColor(mainColor);
            g2d.drawString(line, x, y);
        }
        // Graphics2D 리소스 해제
        g2d.dispose();

        // (7) 결과 이미지 저장 (JPEG, 압축률 70%)
        // - 출력 파일명: final_타임스탬프.jpg
        String outputFileName = "final_" + System.currentTimeMillis() + ".jpg";
        String outputPath = "C:/kd/deploy/mms/storage/" + outputFileName;

        try (FileOutputStream fos = new FileOutputStream(outputPath);
             ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {

            // JPEG Writer 가져오기
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            ImageWriter writer = writers.next();
            writer.setOutput(ios);

            // 압축 파라미터 설정
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.7f);  // 품질: 70% (0.1~1.0)

            // 최종 이미지 쓰기
            writer.write(null, new IIOImage(originalImage, null, null), param);
            writer.dispose();
        }
        // (8) 최종 파일명 반환 (경로 제외)
        return outputFileName;
    }
    
    /**
     *  ClassPath에서 TTF 폰트 로드
     *
     * @param fontPath fonts/NanumGothic.ttf
     * @param fontSize 크기
     * @return Font 객체 (실패 시 null)
     */
    private Font loadCustomFont(String fontPath, int fontSize) {
      try (InputStream is = new ClassPathResource(fontPath).getInputStream()) {
          Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
          return customFont.deriveFont(Font.BOLD, fontSize);
      } catch (Exception e) {
          System.err.println("[WARN] 폰트 로드 실패: " + e.getMessage());
          return null;
      }
  }
}

