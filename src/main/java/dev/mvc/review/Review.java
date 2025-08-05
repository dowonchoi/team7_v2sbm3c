package dev.mvc.review;

import java.io.File;

public class Review {
  public static String getUploadDir() {
      String os = System.getProperty("os.name").toLowerCase();
      if (os.contains("win")) {
          return "C:/kd/deploy/team/review/storage/";
      } else {
          return "/home/ubuntu/deploy/team/review/storage/";
      }
  }
}
