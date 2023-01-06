package com.parkhanbeen.object.chapter06.querycommand;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 이벤트.
 */
public class Event {

    /**
     * 주제.
     */
    private String subject;

    /**
     * 시작 일시.
     */
    private LocalDateTime from;

    /**
     * 소요 시간.
     */
    private Duration duration;

    public Event(String subject, LocalDateTime from, Duration duration) {
        this.subject = subject;
        this.from = from;
        this.duration = duration;
    }

    /**
     * 명령과 쿼리 두 가지 역할을 수행 버그를 유발.
     */
//    public boolean isSatisfied(RecurringSchedule schedule) {
//        if (from.getDayOfWeek() != schedule.getDayOfWeek() ||
//            !from.toLocalTime().equals(schedule.getFrom()) ||
//            !duration.equals(schedule.getDuration())) {
//            reschedule(schedule);
//            return false;
//        }
//
//        return true;
//    }

    public boolean isSatisfied(RecurringSchedule schedule) {
        if (from.getDayOfWeek() != schedule.getDayOfWeek() ||
                !from.toLocalTime().equals(schedule.getFrom()) ||
                !duration.equals(schedule.getDuration())) {
            return false;
        }

        return true;
    }

    public void reschedule(RecurringSchedule schedule) {
        from = LocalDateTime.of(from.toLocalDate().plusDays(dayDistance(schedule)), schedule.getFrom());
        duration = schedule.getDuration();
    }

    private long dayDistance(RecurringSchedule schedule) {
        return schedule.getDayOfWeek().getValue() - from.getDayOfWeek().getValue();
    }
}
