package dev.mvc.products;

import java.io.File;

// 파일 업로드 경로는 war 외부의 절대경로를 지정해야 파일이 손실되지 않습니다. 
// 만약 이렇게 안하면 war 생성시마다 업로드 경로가 초기화 되어 등록된 모든 파일이 삭제됩니다. ★
public class Products {
  /** 페이지당 출력할 레코드 갯수 */
  public static int RECORD_PER_PAGE = 10;

  /** 블럭당 페이지 수, 하나의 블럭은 10개의 페이지로 구성됨 */
  public static int PAGE_PER_BLOCK = 10;

  public static String getUploadDir() {
    String ci = System.getenv("CI");
    if ("true".equals(ci)) {
      return "build/uploads/";  // GitHub Actions용 상대경로 (쓰기 가능)
    }

    //20250801 배포 test
    // 아직 안나옴

    String osName = System.getProperty("os.name").toLowerCase();
    String path = "";

    if (osName.contains("win")) { // Windows
      path = "C:\\kd\\ws_java\\team7_v2sbm3c\\src\\main\\resources\\static\\storage\\products\\";
      //path = "C:\\kd\\deploy\\team\\products\\storage\\";
      // System.out.println("Windows: " + path);
    } else if (osName.contains("mac")) { // MacOS
      path = "/Users/yourusername/deploy/team/products/storage/";
      // System.out.println("MacOS: " + path);
    } else { // Linux
      path = "/home/ubuntu/ws_java/team7_v2sbm3c/src/main/resources/static/storage/products/";
      //path = "/home/ubuntu/deploy/team/products/storage/";
      // System.out.println("Linux: " + path);
    }

    return path;
  }

//    // Windows, VMWare, AWS cloud 절대 경로 설정
//    public static synchronized String getUploadDir() {
//        String path = "";
//        if (File.separator.equals("\\")) { // windows, 개발 환경의 파일 업로드 폴더
//            // path = "C:/kd/deploy/team7_v2sbm3c/products/storage/";
//            path="C:\\kd\\deploy\\team7_v2sbm3c\\products\\storage\\";
//            // System.out.println("Windows 10: " + path);
//            
//        } else { // Linux, AWS, 서비스용 배치 폴더 
//            // System.out.println("Linux");
//            path = "/home/ubuntu/deploy/team7_v2sbm3c/products/storage/";
//        }
//        
//        return path;
//    }

}