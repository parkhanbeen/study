package args;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static args.ErrorCode.MISSING_STRING;

public class StringArrayArgumentMarshaler implements ArgumentMarshaler {
    private String[] stringArrayValue;

    @Override
    public void set(Iterator<String> currentArgument) throws ArgsException {
        try {
//            for (String s : stringArrayValue) {
//                stringArrayValue
//            }
//            stringArrayValue = (String[])currentArgument;
        } catch (NoSuchElementException e) {
            throw new ArgsException(MISSING_STRING);
        }

    }

    public static String[] getValue(ArgumentMarshaler argumentMarshaler) {
        if (argumentMarshaler != null && argumentMarshaler instanceof StringArrayArgumentMarshaler) {
            return ((StringArrayArgumentMarshaler) argumentMarshaler).stringArrayValue;
        } else {
            return null;
        }
    }
}
