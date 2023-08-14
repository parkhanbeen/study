package com.cleanarchitectureexample.account.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NonNull;

public class Activity {

    @Getter
    private final ActivityId id;


    @Getter
    @NonNull
    private final AccountId ownerAccountId;

    @Getter
    @NonNull
    private final AccountId sourceAccountId;

    @Getter
    @NonNull
    private final AccountId targetAccountId;

    @Getter
    @NonNull
    private final LocalDateTime timestamp;

    @Getter
    @NonNull
    private final Money money;

    public Activity(
        ActivityId activityId, @NonNull AccountId ownerAccountId,
        @NonNull AccountId sourceAccountId,
        @NonNull AccountId targetAccountId,
        @NonNull LocalDateTime timestamp,
        @NonNull Money money) {
        this.id = null;
        this.ownerAccountId = ownerAccountId;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.timestamp = timestamp;
        this.money = money;
    }

}
