package com.example.advanced.trace.template;

import com.example.advanced.trace.LogTrace;
import com.example.advanced.trace.TraceStatus;

public abstract class AbstractTemplate<T> {

  private final LogTrace trace;

  public AbstractTemplate(LogTrace trace) {
    this.trace = trace;
  }

  public T execute(String message) {
    TraceStatus status = null;
    try {
      status = trace.begin("orderController.request()");
      T result = call();
      trace.end(status);
      return result;
    } catch (Exception e) {
      trace.exception(status, e);
      throw e;
    }
  }

  protected abstract T call();
}
