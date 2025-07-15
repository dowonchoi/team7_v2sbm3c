package dev.mvc.review;

import java.io.File;

public class Review {
  public static String getUploadDir() {
    String os = System.getProperty("os.name").toLowerCase();
    String path = "";

    if (os.contains("win")) {
      path = "C:\\kd\\deploy\\team\\review\\storage\\";
    } else if (os.contains("mac")) {
      path = "/Users/yourusername/deploy/team/review/storage/";
    } else {
      path = "/home/ubuntu/deploy/team/review/storage/";
    }

    return path;
  }
}
