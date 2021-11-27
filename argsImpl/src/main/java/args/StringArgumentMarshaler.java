package args;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static args.ErrorCode.MISSING_STRING;

public class StringArgumentMarshaler implements ArgumentMarshaler {
    private String stringValue = "";

    @Override
    public void set(Iterator<String> currentArgument) throws ArgsException {
        try {
            stringValue = currentArgument.next();
        } catch (NoSuchElementException e) {
            throw new ArgsException(MISSING_STRING);
        }

    }

    public static String getValue(ArgumentMarshaler argumentMarshaler) {
        if (argumentMarshaler instanceof StringArgumentMarshaler) {
            return ((StringArgumentMarshaler) argumentMarshaler).stringValue;
        } else {
            return "";
        }
    }
}
