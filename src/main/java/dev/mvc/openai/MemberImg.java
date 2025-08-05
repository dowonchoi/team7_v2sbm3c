package dev.mvc.openai;

public class MemberImg {
    public static String getUploadDir() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "C:/kd/deploy/team/member_img/storage/";
        } else {
            return "/home/ubuntu/deploy/team/member_img/storage/";
        }
    }
}
