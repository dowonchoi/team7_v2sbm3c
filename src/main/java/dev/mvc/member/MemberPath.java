package dev.mvc.member;

import java.io.File;

/**
 * MemberPath 유틸리티 클래스
 * - 회원 관련 이미지(사업자 인증 등) 업로드 경로 반환
 * - 환경(CI/Windows/Mac/Linux)에 따라 업로드 경로 자동 설정 및 생성
 */
public class MemberPath {

  /**
   * 회원 이미지 업로드 디렉토리 반환
   * - CI 환경: build/uploads/member/
   * - Windows: C:/kd/deploy/team/member/storage/
   * - Mac/Linux: 사용자 홈 디렉토리 기준
   */
  public static String getUploadDir() {
    // CI 환경 여부 확인 (예: GitHub Actions)
    String ci = System.getenv("CI");
    if ("true".equals(ci)) {
      return "build/uploads/member/";
    }

    // 운영체제 확인
    String osName = System.getProperty("os.name").toLowerCase();
    String path;

    if (osName.contains("win")) {
      // ✅ Windows 개발 환경
      path = "C:/kd/deploy/team/member/storage/";

    } else {
      // ✅ Mac 또는 Linux 배포 서버
      path = System.getProperty("user.home") + "/team/member/storage/";
    }

    // 디렉토리 존재 여부 확인 및 생성
    File dir = new File(path);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    return path;
  }
}
