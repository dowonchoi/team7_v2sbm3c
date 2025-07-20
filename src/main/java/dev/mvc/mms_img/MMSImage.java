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

public class MMSImage {

    public static void main(String[] args) throws IOException {
        // ✅ 원본 이미지 경로
        File input = new File("C:\\kd\\deploy\\mms\\storage\\20250720121106_657.jpg");
        BufferedImage originalImage = ImageIO.read(input);

        // ✅ 리사이즈 크기 설정
        int newWidth = 400;
        int newHeight = 600;

        // ✅ 이미지 리사이즈
        Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        // ✅ 저장 경로
        File compressedFile = new File("C:\\kd\\deploy\\mms\\storage\\mms_output.jpg");

        // ✅ JPEG 압축 (0.7f → 약 70% 품질)
        try (FileOutputStream fos = new FileOutputStream(compressedFile);
             ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            ImageWriter writer = writers.next();
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.7f); // 70% 품질 (0.1~1.0)

            writer.write(null, new IIOImage(resizedImage, null, null), param);
            writer.dispose();
        }

        System.out.println("✅ 압축 완료: " + compressedFile.getAbsolutePath() + 
                           " (크기: " + compressedFile.length() / 1024 + " KB)");
    }
}
