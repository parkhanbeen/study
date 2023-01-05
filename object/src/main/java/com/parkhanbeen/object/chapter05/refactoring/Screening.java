package com.parkhanbeen.object.chapter05.refactoring;

import java.time.LocalDateTime;

public class Screening {
    private int sequence;
    private LocalDateTime whenScreened;

    public int getSequence() {
        return sequence;
    }

    public Movie getMovie() {
        return null;
    }

    public LocalDateTime getWhenScreened() {
        return whenScreened;
    }
}
