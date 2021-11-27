package args;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoubleArgumentMarshalerTest {

    @Test
    void testInvalidDouble() throws Exception {
        try {
            new Args("x##", new String[]{"-x", "Forty two"});
            fail();
        } catch (ArgsException e) {
            Assertions.assertThat(e.getErrorArgumentId()).isEqualTo('x');
            Assertions.assertThat(e.getErrorParameter()).isEqualTo("Forty two");
        }
    }

}