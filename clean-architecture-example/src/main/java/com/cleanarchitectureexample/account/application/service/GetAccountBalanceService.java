package com.cleanarchitectureexample.account.application.service;

import java.time.LocalDateTime;

import com.cleanarchitectureexample.account.application.port.in.GetAccountBalanceQuery;
import com.cleanarchitectureexample.account.application.port.out.LoadAccountPort;
import com.cleanarchitectureexample.account.domain.AccountId;
import com.cleanarchitectureexample.account.domain.Money;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetAccountBalanceService implements GetAccountBalanceQuery {

    private final LoadAccountPort loadAccountPort;

    @Override
    public Money getAccountBalance(AccountId accountId) {
        return loadAccountPort.loadAccount(accountId, LocalDateTime.now())
            .calculateBalance();
    }
}
