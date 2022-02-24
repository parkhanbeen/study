package exception.example.converter;

import exception.example.type.IpPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;

@Slf4j
public class StringToIpPortConverter implements Converter<String, IpPort> {
  @Override
  public IpPort convert(String source) {

    log.info("convert source {}", source);
    var split = source.split(":");
    var ip = split[0];
    var port = Integer.valueOf(split[1]);
    return new IpPort(ip, port);
  }
}
