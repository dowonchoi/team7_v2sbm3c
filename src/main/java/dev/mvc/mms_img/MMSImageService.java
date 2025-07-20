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

    // ✅ 텍스트를 이미지에 합성 후 압축 저장
    public String addTextToImage(String inputPath, String messageText) throws IOException {
        File input = new File(inputPath);
        BufferedImage originalImage = ImageIO.read(input);

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        Graphics2D g2d = originalImage.createGraphics();
        g2d.setFont(new Font("Malgun Gothic", Font.BOLD, 24));
        g2d.setColor(Color.WHITE);

        // ✅ 가운데 정렬
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(messageText);
        int x = (width - textWidth) / 2;
        int y = height - 50;

        // ✅ 그림자
        g2d.setColor(Color.BLACK);
        g2d.drawString(messageText, x + 2, y + 2);
        g2d.setColor(Color.WHITE);
        g2d.drawString(messageText, x, y);
        g2d.dispose();

        // ✅ 압축 후 저장
        String outputFileName = "final_" + System.currentTimeMillis() + ".jpg";
        String outputPath = "C:/kd/deploy/mms/storage/" + outputFileName;

        try (FileOutputStream fos = new FileOutputStream(outputPath);
             ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            ImageWriter writer = writers.next();
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.7f); // 70% 품질

            writer.write(null, new IIOImage(originalImage, null, null), param);
            writer.dispose();
        }

        return outputFileName;
    }
}
