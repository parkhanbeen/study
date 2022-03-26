package com.example.aop.proxyvs;

import static org.junit.jupiter.api.Assertions.*;

import com.example.aop.member.MemberService;
import com.example.aop.member.MemberServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;

@Slf4j
public class ProxyCastingTest {

  @Test
  void jdkProxy() {
    MemberServiceImpl target = new MemberServiceImpl();
    ProxyFactory proxyFactory = new ProxyFactory(target);
    proxyFactory.setProxyTargetClass(false);  // JDK 동적 프록시


    // 프록시를 인터페이스로 캐스팅
    MemberService memberServiceProxy = (MemberService) proxyFactory.getProxy();

    assertThrows(ClassCastException.class,
        () -> {MemberServiceImpl castingMemberService = (MemberServiceImpl) memberServiceProxy;
    });
  }

  @Test
  void cglibProxy() {
    MemberServiceImpl target = new MemberServiceImpl();
    ProxyFactory proxyFactory = new ProxyFactory(target);
    proxyFactory.setProxyTargetClass(true);  // cglib 프록시


    // 프록시를 인터페이스로 캐스팅
    MemberService memberServiceProxy = (MemberService) proxyFactory.getProxy();

    MemberServiceImpl castingMemberService = (MemberServiceImpl) memberServiceProxy;
  }
}
