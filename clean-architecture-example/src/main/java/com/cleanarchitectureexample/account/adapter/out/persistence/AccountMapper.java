package com.cleanarchitectureexample.account.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;

import com.cleanarchitectureexample.account.domain.Account;
import com.cleanarchitectureexample.account.domain.AccountId;
import com.cleanarchitectureexample.account.domain.Activity;
import com.cleanarchitectureexample.account.domain.ActivityId;
import com.cleanarchitectureexample.account.domain.ActivityWindow;
import com.cleanarchitectureexample.account.domain.Money;

class AccountMapper {

    Account mapToDomainEntity(
        AccountJpaEntity account,
        List<ActivityJpaEntity> activities,
        Long withdrawalBalance,
        Long depositBalance) {
        Money baselineBalance = Money.subtract(
            Money.of(depositBalance),
            Money.of(withdrawalBalance));

        return Account.withId(
            new AccountId(account.getId()),
            baselineBalance,
            mapToActivityWindow(activities));
    }

    ActivityWindow mapToActivityWindow(List<ActivityJpaEntity> activities) {
        List<Activity> mappedActivities = new ArrayList<>();

        for (ActivityJpaEntity activity : activities) {
            mappedActivities.add(new Activity(
                new ActivityId(activity.getId()),
                new AccountId(activity.getOwnerAccountId()),
                new AccountId(activity.getSourceAccountId()),
                new AccountId(activity.getTargetAccountId()),
                activity.getTimestamp(),
                Money.of(activity.getAmount())));
        }

        return new ActivityWindow(mappedActivities);
    }

    public ActivityJpaEntity mapToJpaEntity(Activity activity) {
        return new ActivityJpaEntity(
            activity.getId() == null ? null : activity.getId().getValue(),
            activity.getTimestamp(),
            activity.getOwnerAccountId().getValue(),
            activity.getSourceAccountId().getValue(),
            activity.getTargetAccountId().getValue(),
            activity.getMoney().getAmount().longValue());
    }
}
