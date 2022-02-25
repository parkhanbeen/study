package exception.example.formatter;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.util.Locale;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class MyNumberFormatterTest {

  MyNumberFormatter myNumberFormatter = new MyNumberFormatter();

  @Test
  void parse() throws ParseException {
    Number result = myNumberFormatter.parse("1,000", Locale.KOREA);
    assertThat(result).isEqualTo(1000L);
  }

  @Test
  void print() {
    String result = myNumberFormatter.print(1000, Locale.KOREA);
    assertThat(result).isEqualTo("1,000");
  }
}