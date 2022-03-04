package com.example.advanced.trace;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TraceV1 {

  private static final String START_PREFIX = "-->";
  private static final String COMPLETE_PREFIX = "<--";
  private static final String EX_PREFIX = "<X-";

  public TraceStatus begin(String message) {
    var traceId = new TraceId();
    Long startTimeMs = System.currentTimeMillis();
    log.info("[{}] {}{}", traceId.getId(), addSpace(START_PREFIX,
        traceId.getLevel()), message);

    return new TraceStatus(traceId, startTimeMs, message);
  }

  public void end(TraceStatus traceStatus) {
    complete(traceStatus, null);
  }

  public void exception(TraceStatus traceStatus, Exception e) {
    complete(traceStatus, e);
  }

  private void complete(TraceStatus traceStatus, Exception e) {
    Long stopTimeMs = System.currentTimeMillis();
    long resultTimeMs = stopTimeMs - traceStatus.getStartTimeMillisecond();
    TraceId traceId = traceStatus.getTraceId();

    if (e == null) {
      log.info("[{}] {}{} time={}ms", traceId.getId(),
          addSpace(COMPLETE_PREFIX, traceId.getLevel()), traceStatus.getMessage(),
          resultTimeMs);
    } else {
      log.info("[{}] {}{} time={}ms ex={}", traceId.getId(),
          addSpace(EX_PREFIX, traceId.getLevel()), traceStatus.getMessage(), resultTimeMs,
          e.toString());
    }
  }

  private static String addSpace(String prefix, int level) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < level; i++) {
      sb.append((i == level - 1) ? "|" + prefix : "| ");
    }
    return sb.toString();
  }
}
