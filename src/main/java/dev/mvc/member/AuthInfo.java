package dev.mvc.member;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AuthInfo {
  private String code;
  private long expireTime;
  private int attemptCount;

  public AuthInfo(String code, long expireTime) {
    this.code = code;
    this.expireTime = expireTime;
    this.attemptCount = 0;
  }

  public boolean isExpired() {
    return System.currentTimeMillis() > expireTime;
  }
}
