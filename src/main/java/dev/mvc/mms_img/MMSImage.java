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


/**
 * MMS 이미지 처리 클래스
 * - 원본 이미지를 특정 크기로 리사이즈 후 JPEG 압축하여 저장
 * - Gabia MMS 발송을 위해 이미지 용량 및 해상도 최적화
 *
 * <주요 기능>
 * 1) 원본 이미지 로드
 * 2) 지정 크기로 리사이즈 (400x600)
 * 3) JPEG 포맷으로 압축 저장 (품질 설정 가능)
 *
 * 활용 사례:
 * - Gabia MMS API는 첨부 이미지 크기 제한(500KB) 권장 → 이 클래스에서 사전 압축 처리
 */
public class MMSImage {
  /**
   * 이미지 리사이즈 및 압축 실행
   * <처리 순서>
   * 1. 원본 이미지 파일 읽기
   * 2. 지정 크기(400x600)로 리사이즈
   * 3. JPEG 포맷으로 압축 저장 (품질 70%)
   *
   * @throws IOException 이미지 읽기/쓰기 오류 발생 시
   */
  public static void main(String[] args) throws IOException {
    // (1) 원본 이미지 경로
    File input = new File("C:\\kd\\deploy\\mms\\storage\\20250720121106_657.jpg");
    // BufferedImage로 이미지 로드
    BufferedImage originalImage = ImageIO.read(input);

    // (2) 리사이즈 크기 설정 (가로 400px, 세로 600px)
    int newWidth = 400;
    int newHeight = 600;

    // (3) 고품질 리사이즈 처리
    // - Image.SCALE_SMOOTH: 부드러운 스케일링 방식
    Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    
    // 새 BufferedImage에 리사이즈된 이미지 그리기
    BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = resizedImage.createGraphics();
    
    // 고품질 렌더링 옵션 적용 (보간법)
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2d.drawImage(scaledImage, 0, 0, null);
    g2d.dispose();  // 리소스 해제

    // (4) 저장할 압축 파일 경로 지정
    File compressedFile = new File("C:\\kd\\deploy\\mms\\storage\\mms_output.jpg");

    // (5) JPEG 압축 처리
    // - 품질 설정 범위: 0.1f ~ 1.0f (1.0 = 최고 품질)
    try (FileOutputStream fos = new FileOutputStream(compressedFile);
        ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {

      // JPEG Writer 가져오기
      Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
      ImageWriter writer = writers.next();
      writer.setOutput(ios);

      // 압축 설정 파라미터
      ImageWriteParam param = writer.getDefaultWriteParam();
      param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      param.setCompressionQuality(0.7f); // 70% 품질 (0.1~1.0)

      // 이미지 쓰기 (압축 적용)
      writer.write(null, new IIOImage(resizedImage, null, null), param);
      writer.dispose();
    }
    
    // (6) 처리 완료 후 로그 출력
    System.out
        .println("  압축 완료: " + compressedFile.getAbsolutePath() + " (크기: " + compressedFile.length() / 1024 + " KB)");
  }
  
  /** OS에 따라 업로드 경로 리턴 */
  public static String getUploadDir() {
      String os = System.getProperty("os.name").toLowerCase();
      String path;

      if (os.contains("win")) {
          path = "C:/kd/deploy/mms/storage/";
      } else {
          path = "/home/ubuntu/deploy/mms/storage/";
      }

      return path;
  }
}
