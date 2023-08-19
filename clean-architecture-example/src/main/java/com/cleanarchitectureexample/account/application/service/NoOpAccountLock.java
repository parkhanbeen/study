package com.cleanarchitectureexample.account.application.service;

import com.cleanarchitectureexample.account.domain.AccountId;
import com.cleanarchitectureexample.account.domain.AccountLock;

public class NoOpAccountLock implements AccountLock {

    @Override
    public void lockAccount(AccountId accountId) {

    }

    @Override
    public void releaseAccount(AccountId accountId) {

    }
}
