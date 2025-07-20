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
import java.util.Iterator;

import org.springframework.stereotype.Service;

@Service
public class MMSImageService {

    /**
     * ✅ 옵션형 텍스트 합성 (중앙 정렬 + 줄바꿈 지원 + 그림자 + 압축)
     *
     * @param inputPath    원본 이미지 경로
     * @param messageText  합성할 텍스트 (줄바꿈 "\n")
     * @param fontName     폰트명 (예: "Malgun Gothic")
     * @param fontSize     글자 크기 (예: 48)
     * @param textColor    텍스트 색상 (예: "#FFFFFF")
     * @param shadowColor  그림자 색상 (예: "#000000")
     * @return 저장된 파일명
     */
    public String addTextToImage(String inputPath, String messageText,
                                 String fontName, int fontSize,
                                 String textColor, String shadowColor) throws IOException {
        File input = new File(inputPath);
        BufferedImage originalImage = ImageIO.read(input);

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        Graphics2D g2d = originalImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ✅ 폰트 및 색상
        g2d.setFont(new Font(fontName, Font.BOLD, fontSize));
        Color mainColor = Color.decode(textColor);
        Color shadow = Color.decode(shadowColor);

        FontMetrics fm = g2d.getFontMetrics();
        String[] lines = messageText.split("\n");
        int lineHeight = fm.getHeight();
        int totalTextHeight = lineHeight * lines.length;
        int startY = (height - totalTextHeight) / 2 + fm.getAscent();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int textWidth = fm.stringWidth(line);
            int x = (width - textWidth) / 2;
            int y = startY + (i * lineHeight);

            // ✅ 그림자 (사방)
            g2d.setColor(shadow);
            g2d.drawString(line, x - 2, y - 2);
            g2d.drawString(line, x + 2, y - 2);
            g2d.drawString(line, x - 2, y + 2);
            g2d.drawString(line, x + 2, y + 2);

            // ✅ 본문
            g2d.setColor(mainColor);
            g2d.drawString(line, x, y);
        }

        g2d.dispose();

        // ✅ 압축 저장
        String outputFileName = "final_" + System.currentTimeMillis() + ".jpg";
        String outputPath = "C:/kd/deploy/mms/storage/" + outputFileName;

        try (FileOutputStream fos = new FileOutputStream(outputPath);
             ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            ImageWriter writer = writers.next();
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.7f);

            writer.write(null, new IIOImage(originalImage, null, null), param);
            writer.dispose();
        }

        return outputFileName;
    }
}
