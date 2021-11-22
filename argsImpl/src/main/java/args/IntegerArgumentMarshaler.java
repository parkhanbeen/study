package args;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IntegerArgumentMarshaler implements ArgumentMarshaler {

    private int intValue = 0;

    @Override
    public void set(Iterator<String> currentArgument) throws ArgsException {
        String parameter = null;
        try {
            parameter = currentArgument.next();
            intValue = Integer.parseInt(parameter);
        } catch (NoSuchElementException e) {
            throw new ArgsException(ErrorCode.MISSING_INTEGER);
        } catch (NumberFormatException e) {
            throw new ArgsException(ErrorCode.INVALID_INTEGER, parameter);
        }
    }

    public static int getValue(ArgumentMarshaler argumentMarshaler) {
        if (argumentMarshaler != null && argumentMarshaler instanceof IntegerArgumentMarshaler) {
            return ((IntegerArgumentMarshaler) argumentMarshaler).intValue;
        } else {
            return 0;
        }
    }
}
