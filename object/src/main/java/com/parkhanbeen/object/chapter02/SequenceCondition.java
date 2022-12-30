package com.parkhanbeen.object.chapter02;

/**
 * 순번 조건.
 */
public class SequenceCondition implements DiscountCondition {

    /**
     * 순번.
     */
    private int sequence;

    public SequenceCondition(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public boolean isSatisfiedBy(Screening screening) {
        return screening.isSequence(sequence);
    }
}
