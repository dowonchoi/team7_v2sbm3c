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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;


/**
 * MMSImageService
 * 주요 역할:
 * 1. 기존 이미지에 텍스트(폰트, 색상, 그림자 포함)를 합성
 * 2. 텍스트는 줄바꿈 및 자동 줄바꿈 기능을 제공
 * 3. 최종 이미지는 JPEG로 압축 저장 후 파일명 반환
 *
 * 활용 사례:
 * - 쇼핑몰 이벤트 홍보 이미지 제작
 * - Gabia MMS 발송을 위한 이미지 가공 (크기 & 용량 제한 준수)
 */
@Service
public class MMSImageService {

  /**
   * 이미지에 텍스트를 합성 후 저장
   *
   * @param inputPath   원본 이미지 경로
   * @param messageText 합성할 텍스트 (줄바꿈 허용)
   * @param fontName    폰트명 (TTF → OS → SansSerif 순으로 적용)
   * @param fontSize    폰트 크기(px)
   * @param textColor   텍스트 색상 (Hex, 예: #FFFFFF)
   * @param shadowColor 그림자 색상 (Hex)
   * @return 최종 저장된 이미지 파일명
   * @throws IOException 이미지 파일 처리 중 오류
   */
    public String addTextToImage(String inputPath, String messageText,
                                 String fontName, int fontSize,
                                 String textColor, String shadowColor) throws IOException {

        /** (1) 이미지 로드: 기존 이미지 파일을 BufferedImage로 읽기 */
        BufferedImage originalImage = ImageIO.read(new File(inputPath));
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        /** (2) Graphics2D 객체 생성: 이미지 위에 텍스트를 그리기 위한 설정 */
        Graphics2D g2d = originalImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        /** (3) 폰트 설정: TTF → OS → SansSerif fallback */
        Font font = loadFont(fontName, fontSize);
        g2d.setFont(font);

        /** (4) 색상 설정: 텍스트 본문과 그림자 색상 */
        Color mainColor = Color.decode(textColor);
        Color shadow = Color.decode(shadowColor);

        /** (5) 텍스트 줄바꿈 처리 (사용자 개행 + 자동 줄바꿈)
         * - 가용 폭(maxWidth) 내에서 줄 단위로 텍스트 분할
         */        
        FontMetrics fm = g2d.getFontMetrics();
        int maxWidth = width - 40; // 좌우 여백 20px씩
        List<String> wrappedLines = wrapText(messageText, fm, maxWidth);

        int lineHeight = fm.getHeight();  // 한 줄 높이
        int totalTextHeight = lineHeight * wrappedLines.size(); // 텍스트 전체 높이
        int startY = (height - totalTextHeight) / 2 + fm.getAscent(); // 세로 중앙 정렬

        /** (6) 텍스트 출력: 각 줄마다 그림자 + 본문 순서로 렌더링 */
        for (int i = 0; i < wrappedLines.size(); i++) {
            String line = wrappedLines.get(i);
            int textWidth = fm.stringWidth(line);
            int x = (width - textWidth) / 2;   // 가로 중앙 정렬
            int y = startY + (i * lineHeight);

            // 그림자 효과 (4방향)
            g2d.setColor(shadow);
            g2d.drawString(line, x - 2, y - 2);
            g2d.drawString(line, x + 2, y - 2);
            g2d.drawString(line, x - 2, y + 2);
            g2d.drawString(line, x + 2, y + 2);

            // 본문 텍스트
            g2d.setColor(mainColor);
            g2d.drawString(line, x, y);
        }
        g2d.dispose(); // 리소스 해제

        /** (7) JPEG 저장: 압축 품질(0.7f)로 최적화 저장 */
        String outputFileName = "final_" + System.currentTimeMillis() + ".jpg";
        String outputPath = "C:/kd/deploy/mms/storage/" + outputFileName;

        try (FileOutputStream fos = new FileOutputStream(outputPath);
             ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            ImageWriter writer = writers.next();
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.7f); // 품질 70% (파일 크기 절감)

            writer.write(null, new IIOImage(originalImage, null, null), param);
            writer.dispose();
        }

        return outputFileName;
    }

    /// ---------------------------------------------------------
    // 폰트 관련 유틸
    // ---------------------------------------------------------

    /**
     * 폰트 로드 로직
     * 1. ClassPath에서 TTF 파일 로드 (예: /resources/fonts/)
     * 2. OS에 설치된 폰트 확인 후 적용
     * 3. 실패 시 SansSerif로 대체
     */
    private Font loadFont(String fontName, int fontSize) {
        try {
          // 1. 프로젝트 리소스(fonts/)에서 TTF 폰트 로드
            String fontFileName = null;
            if ("Nanum Gothic".equalsIgnoreCase(fontName)) {
                fontFileName = "fonts/NanumGothic.ttf";
            } else if ("Malgun Gothic".equalsIgnoreCase(fontName)) {
                fontFileName = "fonts/MalgunGothic.ttf";
            }

            if (fontFileName != null) {
                InputStream is = new ClassPathResource(fontFileName).getInputStream();
                Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
                System.out.println("[INFO] TTF 폰트 로드 성공: " + fontFileName);
                return customFont.deriveFont(Font.BOLD, fontSize);
            }

            // 2. OS 폰트 사용 가능 여부 체크
            if (isFontAvailable(fontName)) {
                System.out.println("[INFO] OS 폰트 사용: " + fontName);
                return new Font(fontName, Font.BOLD, fontSize);
            }

        } catch (Exception e) {
            System.err.println("[WARN] TTF 로드 실패: " + e.getMessage());
        }

        // 3. fallback → SansSerif
        System.err.println("[WARN] 폰트 '" + fontName + "'를 사용할 수 없음 → SansSerif 대체");
        return new Font("SansSerif", Font.BOLD, fontSize);
    }

    /**
     * OS에 폰트 설치 여부 확인
     */
    private boolean isFontAvailable(String fontName) {
        String[] availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String name : availableFonts) {
            if (name.equalsIgnoreCase(fontName)) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------
    // 텍스트 줄바꿈 유틸
    // ---------------------------------------------------------

    /**
     * 자동 줄바꿈 처리
     * - 기존 개행 문자(\n) 우선 처리
     * - 단어 단위로 maxWidth 초과 시 다음 줄로 이동
     */
    private List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> wrapped = new ArrayList<>();
        for (String paragraph : text.split("\n")) { // 기존 줄바꿈(\n) 처리
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.split(" ")) {  // 공백 단위로 단어 분리
                String testLine = line.length() == 0 ? word : line + " " + word;
                if (fm.stringWidth(testLine) <= maxWidth) {
                    line = new StringBuilder(testLine);
                } else {
                    wrapped.add(line.toString());
                    line = new StringBuilder(word);
                }
            }
            if (line.length() > 0) {
                wrapped.add(line.toString());
            }
        }
        return wrapped;
    }
}
