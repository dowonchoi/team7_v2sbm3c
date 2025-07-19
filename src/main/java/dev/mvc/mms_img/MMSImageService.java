package dev.mvc.mms_img;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class MMSImageService {

    public String addTextToImage(String originalPath, String messageText) throws IOException {
        BufferedImage originalImage = ImageIO.read(new File(originalPath));

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = newImage.createGraphics();

        g2d.drawImage(originalImage, 0, 0, null);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Font font = new Font("Malgun Gothic", Font.BOLD, 30);
        g2d.setFont(font);

        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(messageText);
        int x = (width - textWidth) / 2;
        int y = height - 50;

        g2d.setColor(Color.BLACK);
        g2d.drawString(messageText, x + 2, y + 2);
        g2d.setColor(Color.WHITE);
        g2d.drawString(messageText, x, y);

        g2d.dispose();

        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss_SSS").format(new Date());
        String newFileName = "final_" + timestamp + ".jpg";

        String saveDir = "C:/kd/deploy/mms/storage/";
        File outputFile = new File(saveDir, newFileName);
        ImageIO.write(newImage, "jpg", outputFile);

        return newFileName;
    }
}

    
    
