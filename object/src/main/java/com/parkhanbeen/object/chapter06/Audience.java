package com.parkhanbeen.object.chapter06;

/**
 * 관람객.
 */
public class Audience {
    private Bag bag;

    public Audience(Bag bag) {
        this.bag = bag;
    }

    /**
     * 티켓을 사다.
     */
    public Long buy(Ticket ticket) {
        return bag.hold(ticket);
    }

}
