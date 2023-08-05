package com.pakrhanbeen.modernjavainaction.chapter19;

import java.util.function.Predicate;

/**
 * MyList.
 */
interface MyList<T> {

    T head();

    MyList<T> tail();

    default boolean isEmpty() {
        return true;
    }

}
