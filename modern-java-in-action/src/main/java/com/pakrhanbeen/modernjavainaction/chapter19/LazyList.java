package com.pakrhanbeen.modernjavainaction.chapter19;

import java.util.function.Predicate;
import java.util.function.Supplier;

class LazyList<T> implements MyList<T>{

    final T head;
    final Supplier<MyList<T>> tail;

    public LazyList(T head, Supplier<MyList<T>> tail) {
        this.head = head;
        this.tail = tail;
    }

    @Override
    public T head() {
        return head;
    }

    @Override
    public MyList<T> tail() {
        return tail.get(); // Supplier 로 게으른 동작을 만듬.
    }

    public boolean isEmpty() {
        return false;
    }
}
