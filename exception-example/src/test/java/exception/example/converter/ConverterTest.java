package exception.example.converter;

import static org.assertj.core.api.Assertions.*;

import exception.example.type.IpPort;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConverterTest {

  @Test
  void StringToInteger() {
    var stringToIntegerConverter = new StringToIntegerConverter();
    Integer result = stringToIntegerConverter.convert("10");

    assertThat(result).isEqualTo(10);

  }

  @Test
  void IntegerToString() {
    var integerToString = new IntegerToStringConverter();
    String result = integerToString.convert(10);
    assertThat(result).isEqualTo("10");
  }

  @Test
  void IpPortToStringConverter() {
    var ipPortToStringConverter = new IpPortToStringConverter();
    var ipPort = new IpPort("127.0.0.1", 8080);
    String result = ipPortToStringConverter.convert(ipPort);
    assertThat(result).isEqualTo("127.0.0.1:8080");
  }

  @Test
  void StringToIpPortConverter() {
    var stringToIpPortConverter = new StringToIpPortConverter();
    String source = "127.0.0.1:8080";
    IpPort ipPort = stringToIpPortConverter.convert(source);
    assertThat(ipPort).isEqualTo(new IpPort("127.0.0.1", 8080));
  }
}
