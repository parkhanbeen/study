package exception.example.controller;

import javax.servlet.http.HttpServletRequest;

import exception.example.type.IpPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HelloController {

  @GetMapping("/v1/hello")
  public String helloV1(HttpServletRequest request) {
    String data = request.getParameter("data");
    Integer integerValue = Integer.valueOf(data);
    log.info("v1 integerValue {}", integerValue);
    return "ok";
  }

  @GetMapping("/v2/hello")
  public String helloV1(@RequestParam Integer data) {
    log.info("v2 integerValue {}", data);
    return "ok";
  }

  @GetMapping("/ip-port")
  public String ipPort(@RequestParam IpPort ipPort) {
    log.info("ipPort.getPort() = {}", ipPort.getPort());
    log.info("ipPort.getIp() = {}", ipPort.getIp());

    return "ok";
  }
}
