package com.example.advanced.proxy.jdkdynamic;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ReflectionTest {

  @Test
  void reflection0() {
    Hello target = new Hello();

    log.info("start");
    String result1 = target.callA();
    log.info("result1 = {}", result1);

    log.info("start");
    String result2 = target.callB();
    log.info("result2 = {}", result2);
  }

  @Test
  void reflection1() throws Exception {
    Class classHello = Class.forName("com.example.advanced.proxy.jdkdynamic.ReflectionTest$Hello");

    Hello target = new Hello();

    Method methodCallA = classHello.getMethod("callA");
    Object result1 = methodCallA.invoke(target);  // target 인스턴스에 있는 메서드 호출
    log.info("result1 = {}", result1);

    Method methodCallB = classHello.getMethod("callB");
    Object result2 = methodCallB.invoke(target);  // target 인스턴스에 있는 메서드 호출
    log.info("result2 = {}", result2);
  }

  @Test
  void reflection2() throws Exception {
    Class classHello = Class.forName("com.example.advanced.proxy.jdkdynamic.ReflectionTest$Hello");

    Hello target = new Hello();

    Method methodCallA = classHello.getMethod("callA");
    Method methodCallB = classHello.getMethod("callB");
    dynamicCall(methodCallA, target);
    dynamicCall(methodCallB, target);
  }

  private void dynamicCall(Method method, Object target) throws Exception {
    log.info("start");
    Object result = method.invoke(target);
    log.info("result = {}", result);
  }

  @Slf4j
  static class Hello {
    public String callA() {
      log.info("callA");
      return "A";
    }
    public String callB() {
      log.info("callB");
      return "B";
    }
  }
}
