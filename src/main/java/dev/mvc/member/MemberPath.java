package dev.mvc.member;

import java.io.File;

public class MemberPath {
  public static String getUploadDir() {
    String ci = System.getenv("CI");
    if ("true".equals(ci)) {
      return "build/uploads/member/";
    }

    String osName = System.getProperty("os.name").toLowerCase();
    String path;

    if (osName.contains("win")) {
      path = "C:/kd/deploy/team/member/storage/";
    } else if (osName.contains("mac")) {
      path = System.getProperty("user.home") + "/team/member/storage/";
    } else {
      path = System.getProperty("user.home") + "/team/member/storage/";
    }

    File dir = new File(path);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    return path;
  }
}
