package args;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DoubleArgumentMarshaler implements ArgumentMarshaler {

    private double doubleValue = 0;

    @Override
    public void set(Iterator<String> currentArgument) throws ArgsException {
        String parameter = null;
        try {
            parameter = currentArgument.next();
            doubleValue = Integer.parseInt(parameter);
        } catch (NoSuchElementException e) {
            throw new ArgsException(ErrorCode.MISSING_INTEGER);
        } catch (NumberFormatException e) {
            throw new ArgsException(ErrorCode.INVALID_INTEGER, parameter);
        }
    }

    public static double getValue(ArgumentMarshaler argumentMarshaler) {
        if (argumentMarshaler instanceof DoubleArgumentMarshaler) {
            return ((DoubleArgumentMarshaler) argumentMarshaler).doubleValue;
        } else {
            return 0;
        }
    }
}
