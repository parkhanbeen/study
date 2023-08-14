package com.cleanarchitectureexample.account.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;
import com.cleanarchitectureexample.account.application.port.out.LoadAccountPort;
import com.cleanarchitectureexample.account.application.port.out.UpdateAccountStatePort;
import com.cleanarchitectureexample.account.domain.Account;
import com.cleanarchitectureexample.account.domain.AccountId;
import com.cleanarchitectureexample.account.domain.Activity;
import com.cleanarchitectureexample.account.domain.ActivityWindow;
import com.cleanarchitectureexample.account.domain.Money;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
class AccountPersistenceAdapter implements LoadAccountPort, UpdateAccountStatePort {

    private final AccountRepository accountRepository;
    private final ActivityRepository activityRepository;
    private AccountMapper accountMapper;

    @Override
    public Account loadAccount(
        AccountId accountId,
        LocalDateTime baselineDate) {

        AccountJpaEntity account = accountRepository.findById(accountId.getValue())
            .orElseThrow(EntityNotFoundException::new);

        List<ActivityJpaEntity> activities = activityRepository.findByOwnerSince(
            accountId.getValue(),
            baselineDate);

        Long withdrawalBalance = orZero(activityRepository.getWithdrawalBalanceUntil(
            accountId.getValue(),
            baselineDate));

        Long depositBalance = orZero(activityRepository.getDepositBalanceUntil(
            accountId.getValue(),
            baselineDate));

        return accountMapper.mapToDomainEntity(
            account,
            activities,
            withdrawalBalance,
            depositBalance
        );
    }

    private Long orZero(Long value) {
        return value == null ? 0L : value;
    }

    @Override
    public void updateActivities(Account account) {
        for (Activity activity : account.getActivityWindow().getActivities()) {
            if (activity.getId() == null) {
                activityRepository.save(accountMapper.mapToJpaEntity(activity));
            }
        }
    }

    public static Account withoutId(
        Money baselineBalance,
        ActivityWindow activityWindow
    ) {
        return new Account(null, baselineBalance, activityWindow);
    }

    public static Account withId(
        AccountId accountId,
        Money baselineBalance,
        ActivityWindow activityWindow
    ) {
        return new Account(accountId, baselineBalance, activityWindow);
    }

    public Money calculateBalance() {
        // ..
        return null;
    }

    public boolean withdraw(Money money, AccountId targetAccountId) {
        // ..
        return false;
    }

    public boolean deposit(Money money, AccountId sourceAccountId) {
        // ..
        return false;
    }
}
