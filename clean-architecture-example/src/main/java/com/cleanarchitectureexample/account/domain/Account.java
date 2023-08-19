package com.cleanarchitectureexample.account.domain;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.Getter;

public record Account(AccountId id, Money baselineBalance, ActivityWindow activityWindow) {

    public Money calculateBalance() {
        return Money.add(
            this.baselineBalance,
            this.activityWindow.calculateBalance(this.id)
        );
    }

    public Optional<AccountId> getId(){
        return Optional.ofNullable(this.id);
    }

    public static Account withId(
        AccountId accountId,
        Money baselineBalance,
        ActivityWindow activityWindow) {
        return new Account(accountId, baselineBalance, activityWindow);
    }

    public boolean withdraw(Money money, AccountId targetAccountId) {

        if (!mayWithdraw(money)) {
            return false;
        }

        Activity withdrawal = new Activity(
            this.id,
            this.id,
            targetAccountId,
            LocalDateTime.now(),
            money);
        this.activityWindow.addActivity(withdrawal);
        return true;
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

    public boolean deposit(Money money, AccountId sourceAccountId) {
        Activity deposit = new Activity(
            this.id,
            sourceAccountId,
            this.id,
            LocalDateTime.now(),
            money);
        this.activityWindow.addActivity(deposit);
        return true;
    }

}
