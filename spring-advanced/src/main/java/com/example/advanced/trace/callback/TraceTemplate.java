package com.example.advanced.trace.callback;

import com.example.advanced.trace.LogTrace;
import com.example.advanced.trace.TraceStatus;

public class TraceTemplate {

  private final LogTrace trace;

  public TraceTemplate(LogTrace trace) {
    this.trace = trace;
  }

  public <T> T execute(String message, TraceCallBack<T> callBack) {
    TraceStatus status = null;
    try {
      status = trace.begin("orderController.request()");
      T result = callBack.call();
      trace.end(status);
      return result;
    } catch (Exception e) {
      trace.exception(status, e);
      throw e;
    }

  }
}
