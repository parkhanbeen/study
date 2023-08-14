package com.cleanarchitectureexample.account.application.port.out;

import com.cleanarchitectureexample.account.domain.Account;

public interface UpdateAccountStatePort {

    void updateActivities(Account account);
}
