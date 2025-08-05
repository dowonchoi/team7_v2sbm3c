package dev.mvc.calendar;

import java.io.File;

public class CalendarPath {

  /**
   * 캘린더 이미지 업로드 디렉토리 반환
   * - CI: build/uploads/calendar/
   * - Windows: C:/kd/deploy/team/calendar/storage/
   * - Mac/Linux: 사용자 홈 디렉토리 기준
   */
  public static String getUploadDir() {
    String ci = System.getenv("CI");
    if ("true".equals(ci)) {
      return "build/uploads/calendar/";
    }

    String osName = System.getProperty("os.name").toLowerCase();
    String path;

    if (osName.contains("win")) {
      path = "C:/kd/deploy/team/calendar/storage/";
    } else if (osName.contains("mac")) {
      path = System.getProperty("user.home") + "/team/calendar/storage/";
    } else {
      path = System.getProperty("user.home") + "/team/calendar/storage/";
    }

    File dir = new File(path);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    return path;
  }
}
