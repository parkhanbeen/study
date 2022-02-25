package exception.example.formatter;

import static org.assertj.core.api.Assertions.*;

import exception.example.converter.IpPortToStringConverter;
import exception.example.converter.StringToIpPortConverter;
import exception.example.type.IpPort;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.format.support.DefaultFormattingConversionService;

public class FormattingConversionServiceTest {

  @Test
  void formattingConversionService() {
    DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
    // 컨버터 등록
    conversionService.addConverter(new StringToIpPortConverter());
    conversionService.addConverter(new IpPortToStringConverter());
    // 포메터 등록
    conversionService.addFormatter(new MyNumberFormatter());

    // 컨버터 사용
    IpPort ipPort = conversionService.convert("127.0.0.1:8080", IpPort.class);
    assertThat(ipPort).isEqualTo(new IpPort("127.0.0.1", 8080));
    // 포메터 사용
    assertThat(conversionService.convert("1,000", Long.class)).isEqualTo(1000L);
    assertThat(conversionService.convert(1000, String.class)).isEqualTo("1,000");
  }
}
