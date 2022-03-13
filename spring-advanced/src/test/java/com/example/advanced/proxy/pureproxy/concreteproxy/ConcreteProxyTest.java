package com.example.advanced.proxy.pureproxy.concreteproxy;

import com.example.advanced.proxy.pureproxy.concreteproxy.code.ConcreteClient;
import com.example.advanced.proxy.pureproxy.concreteproxy.code.ConcreteLogic;
import com.example.advanced.proxy.pureproxy.concreteproxy.code.TimeProxy;
import org.junit.jupiter.api.Test;

public class ConcreteProxyTest {

  @Test
  void noProxy() {
    ConcreteLogic concreteLogic = new ConcreteLogic();
    ConcreteClient concreteClient = new ConcreteClient(concreteLogic);
    concreteClient.execute();
  }

  @Test
  void addProxy() {
    ConcreteLogic concreteLogic = new ConcreteLogic();
    TimeProxy timeProxy = new TimeProxy(concreteLogic);
    ConcreteClient concreteClient = new ConcreteClient(timeProxy);
    concreteClient.execute();
  }
}
