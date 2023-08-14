package com.cleanarchitectureexample.account.application.port.in;

import com.cleanarchitectureexample.account.domain.AccountId;
import com.cleanarchitectureexample.account.domain.Money;

public interface GetAccountBalanceQuery {

    Money getAccountBalance(AccountId accountId);
}
