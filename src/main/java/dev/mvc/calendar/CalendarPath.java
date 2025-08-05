package dev.mvc.calendar;

import java.io.File;

/**
 * Calendar 유틸리티 클래스
 * - 캘린더 이미지 업로드 경로 반환
 * - 환경별 경로 자동 설정 및 디렉토리 자동 생성
 */
public class CalendarPath {

  /**
   * 캘린더 이미지 업로드 디렉토리 반환
   * - CI 환경 (GitHub Actions 등): build/uploads/calendar/
   * - Windows: C:/kd/deploy/team/calendar/storage/
   * - Mac/Linux: 사용자 홈 디렉토리 하위 (예: /home/ubuntu/team/calendar/storage/)
   */
  public static String getUploadDir() {
    // GitHub Actions 또는 CI 환경 여부 확인
    String ci = System.getenv("CI");
    if ("true".equals(ci)) {
      return "build/uploads/calendar/"; // CI 전용 경로
    }

    // 운영체제 확인
    String osName = System.getProperty("os.name").toLowerCase();
    String path = "";

    if (osName.contains("win")) {
      // ✅ Windows 개발 환경
      path = "C:/kd/deploy/team/calendar/storage/";

    } else if (osName.contains("mac")) {
      // ✅ Mac 개발 환경
      path = System.getProperty("user.home") + "/team/calendar/storage/";

    } else {
      // ✅ Linux (배포 서버)
      path = System.getProperty("user.home") + "/team/calendar/storage/";
      // 예: /home/ubuntu/team/calendar/storage/
    }

    // 디렉토리 자동 생성
    File dir = new File(path);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    return path;
  }
}
