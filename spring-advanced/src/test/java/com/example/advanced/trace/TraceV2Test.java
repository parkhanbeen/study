package com.example.advanced.trace;

import org.junit.jupiter.api.Test;

class TraceV2Test {

  @Test
  void begin_end() {
    TraceV2 traceV2 = new TraceV2();
    TraceStatus status1 = traceV2.begin("hello1");
    TraceStatus status2 = traceV2.beginSync(status1.getTraceId(), "hello2");
    traceV2.end(status2);
    traceV2.end(status1);
  }

  @Test
  void begin_exception() {
    TraceV2 traceV2 = new TraceV2();
    TraceStatus status1 = traceV2.begin("hello1");
    TraceStatus status2 = traceV2.beginSync(status1.getTraceId(), "hello2");
    traceV2.exception(status2, new IllegalStateException("예외 발생!"));
    traceV2.exception(status1, new IllegalStateException("예외 발생!"));
  }

}
