package com.parkhanbeen.object.chapter01;

/**
 * 관람객.
 */
public class Audience {
    private Bag bag;

    public Audience(Bag bag) {
        this.bag = bag;
    }

    public Long buy(Ticket ticket) {
        if (bag.hasInvitation()) {
            bag.setTicket(ticket);
            return 0L;
        } else {
            bag.minusAmount(ticket.getFee());
            bag.setTicket(ticket);
            return ticket.getFee();
        }
    }

}
