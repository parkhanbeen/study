package com.example.advanced.trace;

import lombok.Getter;

@Getter
public class TraceStatus {
  private final TraceId traceId;
  private final Long startTimeMillisecond;
  private final String message;

  public TraceStatus(TraceId traceId, Long startTimeMillisecond, String message) {
    this.traceId = traceId;
    this.startTimeMillisecond = startTimeMillisecond;
    this.message = message;
  }
}
