package com.example.advanced.proxy.pureproxy.proxy;

import com.example.advanced.proxy.pureproxy.proxy.code.CacheProxy;
import com.example.advanced.proxy.pureproxy.proxy.code.ProxyPatternClient;
import com.example.advanced.proxy.pureproxy.proxy.code.RealSubject;
import org.junit.jupiter.api.Test;

public class ProxyPatternTest {

  @Test
  void noProxyTest() {
    RealSubject realSubject = new RealSubject();
    ProxyPatternClient proxyPatternClient = new ProxyPatternClient(realSubject);
    proxyPatternClient.execute();
    proxyPatternClient.execute();
    proxyPatternClient.execute();
  }

  @Test
  void cacheProxyTest() {
    RealSubject realSubject = new RealSubject();
    CacheProxy cacheProxy = new CacheProxy(realSubject);
    ProxyPatternClient proxyPatternClient = new ProxyPatternClient(cacheProxy);
    proxyPatternClient.execute();
    proxyPatternClient.execute();
    proxyPatternClient.execute();
  }
}
