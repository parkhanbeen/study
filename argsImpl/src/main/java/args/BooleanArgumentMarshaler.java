package args;

import java.util.Iterator;

public class BooleanArgumentMarshaler implements ArgumentMarshaler {

    private boolean booleanValue = false;

    @Override
    public void set(Iterator<String> currentArgument) throws ArgsException {
        booleanValue = true;
    }

    public static boolean getValue(ArgumentMarshaler argumentMarshaler) {
        if (argumentMarshaler != null && argumentMarshaler instanceof BooleanArgumentMarshaler) {
            return ((BooleanArgumentMarshaler) argumentMarshaler).booleanValue;
        } else {
            return false;
        }
    }
}
