package com.parkhanbeen.object.chapter06.querycommand;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class CalendarTest {

    @Test
    void 반복_일정_조건_만족() {

        RecurringSchedule schedule = new RecurringSchedule(
                "회의",
                DayOfWeek.WEDNESDAY,
                LocalTime.of(10, 30),
                Duration.ofMinutes(30)
        );
        Event meeting = new Event(
                "회의",
                LocalDateTime.of(2019, 5, 8, 10, 30),
                Duration.ofMinutes(30));

        Assertions.assertTrue(meeting.isSatisfied(schedule));
    }

    @Test
    void 반복_일정_조건_만족_버그() {
        RecurringSchedule schedule = new RecurringSchedule(
                "회의",
                DayOfWeek.WEDNESDAY,
                LocalTime.of(10, 30),
                Duration.ofMinutes(30)
        );

        Event meeting = new Event("회의",
                LocalDateTime.of(2019, 5, 9, 10, 30),
                Duration.ofMinutes(30));


        Assertions.assertFalse(meeting.isSatisfied(schedule));
        Assertions.assertTrue(meeting.isSatisfied(schedule));
    }

}