package com.example.advanced.trace;

import java.util.UUID;

import lombok.Getter;

@Getter
public class TraceId {
  private final String id;
  private final int level;

  public TraceId() {
    this.id = createId();
    this.level = 0;
  }

  private TraceId(String id, int level) {
    this.id = id;
    this.level = level;
  }

  private String createId() {
    return UUID.randomUUID().toString().split("-")[0];
  }

  public TraceId createNextId() {
    return new TraceId(id, level + 1);
  }

  public TraceId createPreviousLevel() {
    return new TraceId(id, level - 1);
  }

  public boolean isFirstLevel() {
    return level == 0;
  }
}
