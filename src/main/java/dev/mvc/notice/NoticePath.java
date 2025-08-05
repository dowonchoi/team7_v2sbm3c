package dev.mvc.notice;

import java.io.File;

public class NoticePath {

  public static String getUploadDir() {
    String ci = System.getenv("CI");
    if ("true".equals(ci)) {
      return "build/uploads/notice/";
    }

    String osName = System.getProperty("os.name").toLowerCase();
    String path;

    if (osName.contains("win")) {
      path = "C:/kd/deploy/team/notice/storage/";
    } else if (osName.contains("mac")) {
      path = System.getProperty("user.home") + "/team/notice/storage/";
    } else {
      path = System.getProperty("user.home") + "/team/notice/storage/";
    }

    File dir = new File(path);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    return path;
  }
}
