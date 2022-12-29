package com.parkhanbeen.object.chapter01;

/**
 * 가방.
 */
public class Bag {

    /**
     * 현금.
     */
    private Long amount;

    /**
     * 초대장.
     */
    private Invitation invitation;

    /**
     * 티켓.
     */
    private Ticket ticket;

    public Bag(long amount) {
        this(null, amount);
    }

    public Bag(Invitation invitation, long amount) {
        this.invitation = invitation;
        this.amount = amount;
    }

    public boolean hasInvitation() {
        return invitation != null;
    }

    public boolean hasTicket() {
        return ticket != null;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public void minusAmount(Long amount) {
        this.amount -= amount;
    }

    public void plusAmount(Long amount) {
        this.amount += amount;
    }
}
