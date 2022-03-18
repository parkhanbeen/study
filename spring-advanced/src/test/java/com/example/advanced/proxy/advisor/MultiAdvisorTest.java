package com.example.advanced.proxy.advisor;

import com.example.advanced.proxy.common.advice.TimeAdvice;
import com.example.advanced.proxy.common.service.ServiceImpl;
import com.example.advanced.proxy.common.service.ServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;

@Slf4j
public class MultiAdvisorTest {

  @Test
  @DisplayName("여러 프록시")
  void multiAdvisorTest1() {
    ServiceInterface target = new ServiceImpl();
    ProxyFactory proxyFactory1 = new ProxyFactory(target);
    DefaultPointcutAdvisor advisor1 = new DefaultPointcutAdvisor(Pointcut.TRUE, new Advice1());
    proxyFactory1.addAdvisor(advisor1);
    ServiceInterface proxy1 = (ServiceInterface) proxyFactory1.getProxy();

    ProxyFactory proxyFactory2 = new ProxyFactory(proxy1);
    DefaultPointcutAdvisor advisor2 = new DefaultPointcutAdvisor(Pointcut.TRUE, new Advice2());
    proxyFactory2.addAdvisor(advisor2);
    ServiceInterface proxy2 = (ServiceInterface) proxyFactory2.getProxy();

    proxy2.save();
  }

  @Test
  @DisplayName("하나의 프록시 여러 어드바이저")
  void multiAdvisorTest2() {
    DefaultPointcutAdvisor advisor1 = new DefaultPointcutAdvisor(Pointcut.TRUE, new Advice1());
    DefaultPointcutAdvisor advisor2 = new DefaultPointcutAdvisor(Pointcut.TRUE, new Advice2());

    ServiceInterface target = new ServiceImpl();
    ProxyFactory proxyFactory = new ProxyFactory(target);

    proxyFactory.addAdvisor(advisor2);
    proxyFactory.addAdvisor(advisor1);
    ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();

    proxy.save();

  }

  static class Advice1 implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
      log.info("advice1 호출");
      return invocation.proceed();
    }
  }

  static class Advice2 implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
      log.info("advice1 호출");
      return invocation.proceed();
    }
  }

}
