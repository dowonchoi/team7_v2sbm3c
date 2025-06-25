package dev.mvc.member;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AuthInfo {
  private String code;
  private long expire;
  private int attemptCount = 0;

  public AuthInfo(String code, long expire) {
      this.code = code;
      this.expire = expire;
  }

  public String getCode() { return code; }
  public long getExpire() { return expire; }
  public int getAttemptCount() { return attemptCount; }
  public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }

  public boolean isExpired() {
      return System.currentTimeMillis() > expire;
  }
}