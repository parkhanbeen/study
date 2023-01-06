package com.parkhanbeen.object.chapter06;

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

    /**
     * 티켓을 보관하다.
     */
    public Long hold(Ticket ticket) {
        if (hasInvitation()) {
            this.ticket = ticket;
            return 0L;
        } else {
            this.ticket = ticket;
            minusAmount(ticket.getFee());
            return ticket.getFee();
        }
    }

    public Bag(long amount) {
        this(null, amount);
    }

    public Bag(Invitation invitation, long amount) {
        this.invitation = invitation;
        this.amount = amount;
    }

    private boolean hasInvitation() {
        return invitation != null;
    }

    private void minusAmount(Long amount) {
        this.amount -= amount;
    }

}
