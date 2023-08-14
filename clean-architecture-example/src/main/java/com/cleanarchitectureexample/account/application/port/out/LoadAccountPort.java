package com.cleanarchitectureexample.account.application.port.out;

import java.time.LocalDateTime;

import com.cleanarchitectureexample.account.domain.Account;
import com.cleanarchitectureexample.account.domain.AccountId;

public interface LoadAccountPort {

    Account loadAccount(AccountId accountId, LocalDateTime now);
}
