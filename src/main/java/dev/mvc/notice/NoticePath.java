package dev.mvc.notice;

import java.io.File;

/**
 * Notice 유틸리티 클래스
 * - 공지사항 이미지 업로드 경로 반환
 * - 환경(CI/Windows/Mac/Linux)에 따라 업로드 경로 자동 설정 및 생성
 */
public class NoticePath {

  /**
   * 공지사항 이미지 업로드 디렉토리 반환
   * - CI 환경: build/uploads/notice/
   * - Windows: C:/kd/deploy/team/notice/storage/
   * - Mac/Linux: 사용자 홈 디렉토리 하위 (예: /home/ubuntu/team/notice/storage/)
   */
  public static String getUploadDir() {
    // CI 환경 (예: GitHub Actions 등) 여부 확인
    String ci = System.getenv("CI");
    if ("true".equals(ci)) {
      return "build/uploads/notice/"; // 빌드용 임시 저장 경로
    }

    // 운영체제 확인
    String osName = System.getProperty("os.name").toLowerCase();
    String path = "";

    if (osName.contains("win")) {
      // ✅ Windows 개발 환경
      path = "C:/kd/deploy/team/notice/storage/";

    } else if (osName.contains("mac")) {
      // ✅ Mac 개발 환경
      path = System.getProperty("user.home") + "/team/notice/storage/";

    } else {
      // ✅ Linux 배포 서버
      path = System.getProperty("user.home") + "/team/notice/storage/";
    }

    // 디렉토리 생성
    File dir = new File(path);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    return path;
  }
}
