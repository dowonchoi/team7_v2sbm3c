package dev.mvc.products;

import java.io.File;

/**
 * Products 유틸리티 클래스
 * - 페이지네이션 설정
 * - 이미지 업로드 경로 반환
 * 
 * 운영체제 또는 환경(CI)마다 파일 업로드 경로가 다르게 설정됨
 */
public class Products {

  /** 페이지당 출력할 레코드 갯수 */
  public static int RECORD_PER_PAGE = 10;

  /** 블럭당 페이지 수 (페이징에 표시될 페이지 수) */
  public static int PAGE_PER_BLOCK = 10;

  /**
   * 업로드 디렉토리 경로 반환
   * - GitHub Actions(CI) 빌드시에는 상대 경로 사용 (build/uploads/)
   * - 로컬 개발(Windows): C:/kd/deploy/team/products/storage/
   * - Mac 개발 환경: 사용자 홈 디렉토리 하위
   * - 배포 서버(Linux): /home/ubuntu/team/products/storage/
   */
  public static String getUploadDir() {
    // CI 환경(GitHub Actions) 여부 확인
    String ci = System.getenv("CI");
    if ("true".equals(ci)) {
      return "build/uploads/"; // 빌드 중에만 사용하는 임시 디렉토리
    }

    // 현재 운영체제 확인
    String osName = System.getProperty("os.name").toLowerCase();
    String path = "";

    if (osName.contains("win")) {
      // ✅ 로컬 Windows 개발 환경 (예: C:/kd/deploy/...)
      path = "C:/kd/deploy/team/products/storage/";

    } else if (osName.contains("mac")) {
      // ✅ Mac 개발 환경
      path = System.getProperty("user.home") + "/team/products/storage/";

    } else {
      // ✅ Linux (배포 서버) 환경
      path = System.getProperty("user.home") + "/team/products/storage/";
      // 예: /home/ubuntu/team/products/storage/
    }

    // 업로드 경로가 없다면 자동 생성
    File dir = new File(path);
    if (!dir.exists()) {
      dir.mkdirs(); // 디렉토리 없으면 생성
    }

    return path;
  }

}
