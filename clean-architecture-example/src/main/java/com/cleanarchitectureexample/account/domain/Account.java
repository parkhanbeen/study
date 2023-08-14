package com.cleanarchitectureexample.account.domain;

import java.time.LocalDateTime;

public record Account(AccountId id, Money baselineBalance, ActivityWindow activityWindow) {

    public Money calculateBalance() {
        return Money.add(
            this.baselineBalance,
            this.activityWindow.calculateBalance(this.id)
        );
    }

    public static Account withId(
        AccountId accountId,
        Money baselineBalance,
        ActivityWindow activityWindow) {
        return new Account(accountId, baselineBalance, activityWindow);
    }

    private boolean mayWithdraw(Money money) {
        return Money.add(
                this.calculateBalance(),
                money.negate())
            .isPositive();
    }

    public ActivityWindow getActivityWindow() {
        return activityWindow;
    }
}
