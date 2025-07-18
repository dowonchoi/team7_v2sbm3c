package dev.mvc.sms;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MMSImage {

    public static void main(String[] args) throws IOException, FontFormatException {
        // 원본 이미지 불러오기
        File input = new File("C:\\kd\\ws_python\\openai\\static\\member_img\\20250701104746_777.jpg");
        BufferedImage originalImage = ImageIO.read(input);

        // 새로운 크기 설정
        int newWidth = 400;
        int newHeight = 600;

        // 크기 조절된 이미지 생성
        Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = resizedImage.createGraphics();
        // 렌더링 옵션 켜기
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 이미지 그리기
        g2d.drawImage(scaledImage, 0, 0, null);

        // 한글 지원 폰트 설정
        Font font = new Font("Malgun Gothic", Font.BOLD, 20);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();

        // 출력할 멀티라인 문자열
        StringBuilder sb = new StringBuilder();
        sb.append("BBBBBBAAAAABBBBYYY!!!!!");
     // sb.append("여름 계곡 트레킹 안내\n\n");
        
     // sb.append("한여름 무더위를 맞아\n");
     // sb.append("시원한 계곡 트레킹을 준비했습니다.\n");
     // sb.append("많은 참여 부탁드립니다.\n\n");
        
     // sb.append("일시: 2025년 7월 1일 화요일 10시\n");
     // sb.append("장소: 관악산\n");
     // sb.append("준비물: 등산화, 편한 복장\n");
     // sb.append("식사: 삼겹살, 비빔밥\n");
     // sb.append("회비: 10,000 원");
        String[] lines = sb.toString().split("\n");

        // 전체 텍스트 블록 높이 계산
        int lineHeight = fm.getHeight();
        int blockHeight = lineHeight * lines.length;

        // 시작 위치 (중앙 정렬)
        int startY = (newHeight - blockHeight) / 2 + fm.getAscent();

        // 각 라인 그리기 (그림자 + 본문)
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineWidth = fm.stringWidth(line);
            int x = (newWidth - lineWidth) / 2;
            int y = startY + i * lineHeight;

            // 그림자
            g2d.setColor(Color.BLACK);
            g2d.drawString(line, x + 2, y + 2);

            // 본문
            g2d.setColor(Color.WHITE);
            g2d.drawString(line, x, y);
        }

        g2d.dispose();

        // 결과 저장C:\kd\ws_python\openai\static\member_img
        File output = new File("C:\\kd\\deploy\\mms\\storage\\mms_output.jpg");
        ImageIO.write(resizedImage, "jpg", output);
        System.out.println("이미지 저장 완료: " + output.getAbsolutePath());
    }
}